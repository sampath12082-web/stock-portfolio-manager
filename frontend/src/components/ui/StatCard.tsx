import { TrendingUp, TrendingDown } from 'lucide-react';
import { pnlColor } from '@/utils/format';

interface StatCardProps {
  label: string;
  value: string;
  change?: number | null;
  changeLabel?: string;
}

export default function StatCard({ label, value, change, changeLabel }: StatCardProps) {
  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
      <p className="text-xs text-gray-500 mb-1">{label}</p>
      <p className="text-xl font-semibold text-gray-900">{value}</p>
      {change != null && (
        <div className={`flex items-center gap-1 mt-1 text-sm ${pnlColor(change)}`}>
          {change >= 0 ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
          <span>{changeLabel || `${change >= 0 ? '+' : ''}${change.toFixed(2)}%`}</span>
        </div>
      )}
    </div>
  );
}
