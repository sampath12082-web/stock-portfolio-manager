import { pnlColor } from '@/utils/format';

interface PnLTextProps {
  value: number | null | undefined;
  format?: (v: number) => string;
}

export default function PnLText({ value, format }: PnLTextProps) {
  if (value == null) return <span className="text-gray-400">—</span>;
  const formatted = format ? format(value) : `${value >= 0 ? '+' : ''}${value.toFixed(2)}`;
  return <span className={pnlColor(value)}>{formatted}</span>;
}
