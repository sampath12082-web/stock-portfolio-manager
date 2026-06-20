import { useState } from 'react';
import { Camera } from 'lucide-react';
import { useRecentPerformance, useCaptureSnapshot, useTodaySnapshot } from '@/hooks/usePerformance';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import StatCard from '@/components/ui/StatCard';
import Card from '@/components/ui/Card';
import Badge from '@/components/ui/Badge';
import PnLText from '@/components/ui/PnLText';
import { formatCurrency, formatPercentage, formatDate } from '@/utils/format';
import { ComposedChart, Area, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

const PRESETS = [
  { label: '7D', days: 7 }, { label: '30D', days: 30 },
  { label: '90D', days: 90 }, { label: '1Y', days: 365 },
];

export default function PerformancePage() {
  const [days, setDays] = useState(7);
  const performance = useRecentPerformance(days);
  const today = useTodaySnapshot();
  const captureMut = useCaptureSnapshot();

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Performance</h1>
        <button onClick={() => captureMut.mutate()} disabled={captureMut.isPending}
          className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
          <Camera size={16} /> {captureMut.isPending ? 'Capturing...' : 'Capture Snapshot'}
        </button>
      </div>

      {today.data && (
        <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
          <StatCard label="Investment" value={formatCurrency(today.data.totalInvestment)} />
          <StatCard label="Current Value" value={formatCurrency(today.data.currentValue)} />
          <StatCard label="Total P&L" value={formatCurrency(today.data.totalPnL)} change={today.data.totalPnLPercentage} changeLabel={formatPercentage(today.data.totalPnLPercentage)} />
          <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
            <p className="text-xs text-gray-500 mb-1">Top Gainer / Loser</p>
            <div className="flex gap-2 mt-1">
              {today.data.topGainer && <Badge variant="green">{today.data.topGainer}</Badge>}
              {today.data.topLoser && <Badge variant="red">{today.data.topLoser}</Badge>}
              {!today.data.topGainer && !today.data.topLoser && <span className="text-gray-400 text-sm">—</span>}
            </div>
          </div>
        </div>
      )}

      <div className="flex gap-2">
        {PRESETS.map((p) => (
          <button key={p.days} onClick={() => setDays(p.days)}
            className={`px-3 py-1 rounded-md text-sm font-medium ${days === p.days ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            {p.label}
          </button>
        ))}
      </div>

      <Card title="Portfolio Value Over Time">
        {performance.isLoading ? <LoadingSpinner /> : performance.data && performance.data.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <ComposedChart data={performance.data}>
              <defs><linearGradient id="perfGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#2563eb" stopOpacity={0.2} /><stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
              </linearGradient></defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
              <XAxis dataKey="snapshotDate" tick={{ fill: '#6b7280', fontSize: 12 }} />
              <YAxis tick={{ fill: '#6b7280', fontSize: 12 }} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
              <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }}
                formatter={(value: unknown, name: unknown) => [formatCurrency(Number(value)), String(name) === 'currentValue' ? 'Value' : 'Investment']} />
              <Area type="monotone" dataKey="currentValue" stroke="#2563eb" fill="url(#perfGrad)" strokeWidth={2} />
              <Line type="monotone" dataKey="totalInvestment" stroke="#9ca3af" strokeDasharray="5 5" strokeWidth={1} dot={false} />
            </ComposedChart>
          </ResponsiveContainer>
        ) : <EmptyState message="No snapshots yet. Click 'Capture Snapshot' or wait for the daily 3:30 PM auto-capture." />}
      </Card>

      {performance.data && performance.data.length > 0 && (
        <Card title="Snapshot History">
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-gray-500 text-left border-b border-gray-200">
                <th className="pb-2 px-2">Date</th><th className="pb-2 px-2 text-right">Investment</th><th className="pb-2 px-2 text-right">Value</th>
                <th className="pb-2 px-2 text-right">P&L</th><th className="pb-2 px-2 text-right">P&L%</th><th className="pb-2 px-2">Gainer</th><th className="pb-2 px-2">Loser</th>
              </tr></thead>
              <tbody>
                {[...performance.data].reverse().map((s) => (
                  <tr key={s.id} className="border-b border-gray-100">
                    <td className="py-2 px-2">{formatDate(s.snapshotDate)}</td>
                    <td className="py-2 px-2 text-right">{formatCurrency(s.totalInvestment)}</td>
                    <td className="py-2 px-2 text-right">{formatCurrency(s.currentValue)}</td>
                    <td className="py-2 px-2 text-right"><PnLText value={s.totalPnL} format={formatCurrency} /></td>
                    <td className="py-2 px-2 text-right"><PnLText value={s.totalPnLPercentage} format={formatPercentage} /></td>
                    <td className="py-2 px-2">{s.topGainer && <Badge variant="green">{s.topGainer}</Badge>}</td>
                    <td className="py-2 px-2">{s.topLoser && <Badge variant="red">{s.topLoser}</Badge>}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}
    </div>
  );
}
