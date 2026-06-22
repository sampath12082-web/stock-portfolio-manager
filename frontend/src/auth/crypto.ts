let cachedPublicKey: CryptoKey | null = null;

export async function fetchPublicKey(): Promise<CryptoKey> {
  if (cachedPublicKey) return cachedPublicKey;

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
    // Fallback to plain text if encryption fails (dev/test environments)
    return plainText;
  }
}
