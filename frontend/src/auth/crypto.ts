let cachedPublicKey: CryptoKey | null = null;
let cachedAt = 0;
const CACHE_TTL = 5 * 60 * 1000; // 5 minutes

export async function fetchPublicKey(): Promise<CryptoKey> {
  const now = Date.now();
  if (cachedPublicKey && (now - cachedAt) < CACHE_TTL) return cachedPublicKey;

  const resp = await fetch('/api/auth/public-key');
  const { publicKey: pem } = await resp.json();

  const base64 = pem
    .replace('-----BEGIN PUBLIC KEY-----', '')
    .replace('-----END PUBLIC KEY-----', '')
    .replace(/\n/g, '');

  const binaryDer = Uint8Array.from(atob(base64), c => c.charCodeAt(0));

  cachedPublicKey = await window.crypto.subtle.importKey(
    'spki',
    binaryDer.buffer,
    { name: 'RSA-OAEP', hash: 'SHA-256' },
    false,
    ['encrypt']
  );
  cachedAt = now;

  return cachedPublicKey;
}

export async function encryptField(plainText: string): Promise<string> {
  try {
    const key = await fetchPublicKey();
    const encoded = new TextEncoder().encode(plainText);
    const encrypted = await window.crypto.subtle.encrypt(
      { name: 'RSA-OAEP' },
      key,
      encoded
    );
    return btoa(String.fromCharCode(...new Uint8Array(encrypted)));
  } catch {
    cachedPublicKey = null;
    return plainText;
  }
}
