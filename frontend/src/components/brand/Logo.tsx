export default function Logo({ size = 'md' }: { size?: 'sm' | 'md' | 'lg' }) {
  const dim = size === 'sm' ? 28 : size === 'lg' ? 44 : 36;
  const textSize = size === 'sm' ? '13px' : size === 'lg' ? '18px' : '15px';
  const subSize = size === 'sm' ? '7px' : size === 'lg' ? '11px' : '9px';

  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: size === 'sm' ? '6px' : '10px' }}>
      <svg width={dim} height={dim} viewBox="0 0 36 36" fill="none">
        <rect width="36" height="36" rx="8" fill="#D85A30" />
        <circle cx="24" cy="9" r="3.5" fill="#FAEEDA" />
        <line x1="24" y1="12.5" x2="20" y2="22" stroke="#FAEEDA" strokeWidth="2" strokeLinecap="round" />
        <line x1="22" y1="16" x2="27" y2="19" stroke="#FAEEDA" strokeWidth="1.8" strokeLinecap="round" />
        <line x1="22" y1="16" x2="17" y2="14" stroke="#FAEEDA" strokeWidth="1.8" strokeLinecap="round" />
        <line x1="20" y1="22" x2="24" y2="29" stroke="#FAEEDA" strokeWidth="2" strokeLinecap="round" />
        <line x1="20" y1="22" x2="14" y2="27" stroke="#FAEEDA" strokeWidth="2" strokeLinecap="round" />
        <line x1="8" y1="15" x2="14" y2="15" stroke="#FAC775" strokeWidth="1.5" strokeLinecap="round" />
        <line x1="6" y1="19" x2="13" y2="19" stroke="#FAC775" strokeWidth="1.5" strokeLinecap="round" opacity="0.7" />
        <line x1="8" y1="23" x2="13" y2="23" stroke="#FAC775" strokeWidth="1.5" strokeLinecap="round" opacity="0.45" />
      </svg>
      <div>
        <div style={{ fontSize: textSize, fontWeight: 700, lineHeight: 1, color: '#D85A30', letterSpacing: '-0.3px' }}>
          Solo<span style={{ fontWeight: 400, color: '#2C2C2A' }}>Sprint</span>
        </div>
        <div style={{ fontSize: subSize, letterSpacing: '1.5px', color: '#888780', marginTop: '1px' }}>TRADE</div>
      </div>
    </div>
  );
}
