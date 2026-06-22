import { useState } from 'react';
import { Zap } from 'lucide-react';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import StatCard from '@/components/ui/StatCard';
import Card from '@/components/ui/Card';
import PnLText from '@/components/ui/PnLText';
import Badge from '@/components/ui/Badge';
import EmptyState from '@/components/ui/EmptyState';
import { useDashboard } from '@/hooks/useDashboard';
import { usePortfolioSummary, useSectorAllocation, useStockPnL } from '@/hooks/usePortfolio';
import { useRecentPerformance } from '@/hooks/usePerformance';
import { useActiveSignals } from '@/hooks/useSignals';
import { useGrowwAccount } from '@/hooks/useHoldings';
import { useMfHoldings } from '@/hooks/useMutualFunds';
import { analyzeHoldings } from '@/api/endpoints';
import { formatCurrency, formatPercentage, formatNumber } from '@/utils/format';
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer, AreaChart, Area, XAxis, YAxis, CartesianGrid } from 'recharts';
import type { SignalType } from '@/api/types';

const COLORS = ['#059669', '#2563eb', '#ec4899', '#d97706', '#7c3aed', '#ea580c', '#0d9488', '#dc2626'];
const signalBadge: Record<SignalType, 'green' | 'red' | 'yellow' | 'blue'> = {
  BUY_SIGNAL: 'green', SELL_SIGNAL: 'red', HOLD: 'yellow', WATCH: 'blue',
};

function RunAnalysisButton({ onDone }: { onDone: () => void }) {
  const [loading, setLoading] = useState(false);
  return (
    <div className="flex flex-col items-center py-6 text-gray-400">
      <p className="text-sm mb-3">No signals yet</p>
      <button disabled={loading} onClick={async () => {
        setLoading(true);
        try { await analyzeHoldings(); onDone(); } catch { /* ignore */ }
        setLoading(false);
      }} className="flex items-center gap-2 px-4 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
        <Zap size={14} /> {loading ? 'Analyzing...' : 'Run Analysis'}
      </button>
    </div>
  );
}

function GrowwOrdersSection({ orders }: { orders: Array<{ symbol: string; transactionType: string; status: string; quantity: number; filledQuantity: number; price: number; avgFillPrice: number; exchange: string }> }) {
  const [showAll, setShowAll] = useState(true);
  const filtered = showAll ? orders : orders.filter((o) => o.status === 'EXECUTED');

  return (
    <Card title={
      <div className="flex items-center justify-between w-full">
        <span>Today's Orders</span>
        <div className="flex gap-1">
          <button onClick={() => setShowAll(false)} className={`px-2 py-0.5 rounded text-xs font-medium ${!showAll ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>Executed</button>
          <button onClick={() => setShowAll(true)} className={`px-2 py-0.5 rounded text-xs font-medium ${showAll ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>All ({orders.length})</button>
        </div>
      </div>
    }>
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead><tr className="text-gray-500 text-left border-b border-gray-200">
            <th className="pb-2 px-2">Symbol</th><th className="pb-2 px-2">Type</th>
            <th className="pb-2 px-2">Status</th><th className="pb-2 px-2 text-right">Qty</th>
            <th className="pb-2 px-2 text-right">Filled</th><th className="pb-2 px-2 text-right">Price</th>
            <th className="pb-2 px-2">Exchange</th>
          </tr></thead>
          <tbody>
            {filtered.map((o, i) => (
              <tr key={i} className="border-b border-gray-100">
                <td className="py-2 px-2 font-medium text-gray-900">{o.symbol}</td>
                <td className="py-2 px-2"><Badge variant={o.transactionType === 'BUY' ? 'green' : 'red'}>{o.transactionType}</Badge></td>
                <td className="py-2 px-2"><Badge variant={o.status === 'EXECUTED' ? 'green' : o.status === 'CANCELLED' ? 'gray' : 'yellow'}>{o.status}</Badge></td>
                <td className="py-2 px-2 text-right">{o.quantity}</td>
                <td className="py-2 px-2 text-right">{o.filledQuantity}</td>
                <td className="py-2 px-2 text-right">{formatCurrency(o.avgFillPrice || o.price)}</td>
                <td className="py-2 px-2"><Badge variant={o.exchange === 'NSE' ? 'blue' : 'yellow'}>{o.exchange}</Badge></td>
              </tr>
            ))}
            {filtered.length === 0 && (
              <tr><td colSpan={7} className="py-4 text-center text-gray-400 text-sm">No executed orders today</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </Card>
  );
}

export default function DashboardPage() {
  const dashboard = useDashboard();
  const summary = usePortfolioSummary();
  const allocation = useSectorAllocation();
  const stockPnl = useStockPnL();
  const growwAccount = useGrowwAccount();
  const mfHoldings = useMfHoldings();
  const performance = useRecentPerformance(30);
  const signals = useActiveSignals();

  if (dashboard.isLoading) return <LoadingSpinner />;
  const d = dashboard.data;
  const s = summary.data;
  const g = growwAccount.data;

  const mf = mfHoldings.data;
  const mfInvested = mf?.reduce((s, h) => s + h.investedAmount, 0) ?? 0;
  const mfCurrent = mf?.reduce((s, h) => s + (h.currentValue ?? h.investedAmount), 0) ?? 0;
  const mfPnL = mfCurrent - mfInvested;
  const mfPnLPct = mfInvested > 0 ? (mfPnL / mfInvested) * 100 : 0;

  const growwAvailable = g != null;
  const clearCash = g?.clearCash ?? null;
  const cashBalance = clearCash;
  const realizedPnL = (d && clearCash != null) ? (d.investedAmount + clearCash) - d.totalDeposited : null;
  const totalPnL = (d && realizedPnL != null) ? realizedPnL + d.unrealizedPnL : null;

  const totalInvested = (d?.investedAmount ?? 0) + mfInvested;
  const totalCurrentValue = (d?.currentValue ?? 0) + mfCurrent;
  const totalCash = clearCash ?? 0;
  const totalNetWorth = totalCurrentValue + totalCash;
  const totalUnrealizedPnL = (d?.unrealizedPnL ?? 0) + mfPnL;
  const totalUnrealizedPct = totalInvested > 0 ? (totalUnrealizedPnL / totalInvested) * 100 : 0;

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-4 gap-4">
        <div className="bg-white border-2 border-blue-200 rounded-lg shadow-sm p-4">
          <h3 className="text-xs font-bold text-blue-500 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-blue-100">Total Funds</h3>
          <div className="grid grid-cols-[1fr_1px_1fr] gap-x-4">
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Total Invested</span><span className="font-semibold text-gray-900">{formatCurrency(totalInvested)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Current Value</span><span className="font-semibold text-gray-900">{formatCurrency(totalCurrentValue)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Cash</span><span className="font-semibold text-gray-900">{clearCash != null ? formatCurrency(totalCash) : <span className="text-gray-400 text-xs">Groww offline</span>}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Net Worth</span><span className="font-bold text-gray-900">{clearCash != null ? formatCurrency(totalNetWorth) : formatCurrency(totalCurrentValue)}</span></div>
            </div>
            <div className="bg-blue-100" />
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Unrealized P&L</span><span className="font-semibold"><PnLText value={totalUnrealizedPnL} format={formatCurrency} /><span className="text-xs ml-1">(<PnLText value={totalUnrealizedPct} format={formatPercentage} />)</span></span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Stocks</span><span className="font-semibold text-gray-900">{formatCurrency(d?.currentValue)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Mutual Funds</span><span className="font-semibold text-gray-900">{formatCurrency(mfCurrent)}</span></div>
            </div>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Portfolio</h3>
          <div className="grid grid-cols-[1fr_1px_1fr] gap-x-4">
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Deposited</span><span className="font-semibold text-gray-900">{formatCurrency(d?.totalDeposited)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Invested</span><span className="font-semibold text-gray-900">{formatCurrency(d?.investedAmount)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Current Value</span><span className="font-semibold text-gray-900">{formatCurrency(d?.currentValue)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Cash Balance</span><span className="font-semibold text-gray-900">{cashBalance != null ? formatCurrency(cashBalance) : <span className="text-gray-400 text-xs">Groww offline</span>}</span></div>
            </div>
            <div className="bg-gray-200" />
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Unrealized P&L</span><span className="font-semibold"><PnLText value={d?.unrealizedPnL} format={formatCurrency} />{d?.unrealizedPnLPercentage != null && <span className="text-xs ml-1">(<PnLText value={d.unrealizedPnLPercentage} format={formatPercentage} />)</span>}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Realized P&L</span><span className="font-semibold">{realizedPnL != null ? <PnLText value={realizedPnL} format={formatCurrency} /> : <span className="text-gray-400 text-xs">Groww offline</span>}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Total P&L</span><span className="font-semibold">{totalPnL != null ? <PnLText value={totalPnL} format={formatCurrency} /> : <span className="text-gray-400 text-xs">Groww offline</span>}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Day Change</span><span className="font-semibold"><PnLText value={s?.dayPnL} format={formatCurrency} />{s?.dayPnLPercentage != null && <span className="text-xs ml-1">(<PnLText value={s.dayPnLPercentage} format={formatPercentage} />)</span>}</span></div>
            </div>
          </div>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Mutual Funds</h3>
          <div className="grid grid-cols-[1fr_1px_1fr] gap-x-4">
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Holdings</span><span className="font-semibold text-gray-900">{mf?.length ?? 0} funds</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">Invested</span><span className="font-semibold text-gray-900">{formatCurrency(mfInvested)}</span></div>
            </div>
            <div className="bg-gray-200" />
            <div className="space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Current Value</span><span className="font-semibold text-gray-900">{formatCurrency(mfCurrent)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">P&L</span><span className="font-semibold"><PnLText value={mfPnL} format={formatCurrency} /><span className="text-xs ml-1">(<PnLText value={mfPnLPct} format={formatPercentage} />)</span></span></div>
            </div>
          </div>
        </div>

        {g ? (
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Groww Account</h3>
            <div className="grid grid-cols-[1fr_1px_1fr] gap-x-4">
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">Available Cash</span><span className="font-semibold text-gray-900">{formatCurrency(g.availableCash)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Clear Cash</span><span className="font-semibold text-gray-900">{formatCurrency(g.clearCash)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Margin Used</span><span className="font-semibold text-gray-900">{formatCurrency(g.marginUsed)}</span></div>
              </div>
              <div className="bg-gray-200" />
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">UCC</span><span className="font-semibold text-gray-900">{g.ucc}</span></div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-500">NSE</span>
                  <Badge variant={g.nseEnabled ? 'green' : 'gray'}>{g.nseEnabled ? 'Enabled' : 'Disabled'}</Badge>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-500">BSE</span>
                  <Badge variant={g.bseEnabled ? 'green' : 'gray'}>{g.bseEnabled ? 'Enabled' : 'Disabled'}</Badge>
                </div>
                <div className="flex justify-between items-center text-sm">
                  <span className="text-gray-500">Segments</span>
                  <div className="flex gap-1">{g.activeSegments.map((seg) => <Badge key={seg} variant="blue">{seg}</Badge>)}</div>
                </div>
              </div>
            </div>
          </div>
        ) : (
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Groww Account</h3>
            <div className="flex flex-col items-center justify-center py-6 text-gray-400">
              <p className="text-sm">Groww API offline</p>
              <p className="text-xs mt-1">Renew API key at groww.in/trade-api</p>
            </div>
          </div>
        )}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card title="Sector Allocation">
          {allocation.data && allocation.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <PieChart>
                <Pie data={allocation.data} dataKey="percentage" nameKey="sector" cx="50%" cy="50%" outerRadius={90}
                  label={({ name, value }: { name?: string; value?: number }) => `${name ?? ''} ${(value ?? 0).toFixed(1)}%`}>
                  {allocation.data.map((_: unknown, i: number) => (<Cell key={i} fill={COLORS[i % COLORS.length]} />))}
                </Pie>
                <Tooltip formatter={(value: unknown) => `${Number(value).toFixed(1)}%`} contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }} />
              </PieChart>
            </ResponsiveContainer>
          ) : <EmptyState message="No holdings to show allocation" />}
        </Card>

        <Card title="Top Holdings by P&L">
          {stockPnl.data && stockPnl.data.length > 0 ? (
            <table className="w-full text-sm">
              <thead><tr className="text-gray-500 text-left border-b border-gray-200">
                <th className="pb-2">Symbol</th><th className="pb-2 text-right">Current</th><th className="pb-2 text-right">P&L</th><th className="pb-2 text-right">P&L%</th>
              </tr></thead>
              <tbody>
                {[...stockPnl.data].sort((a, b) => (b.currentValue ?? 0) - (a.currentValue ?? 0)).slice(0, 5).map((s) => (
                  <tr key={s.symbol} className="border-b border-gray-100">
                    <td className="py-2 font-medium text-gray-900">{s.symbol}</td>
                    <td className="py-2 text-right">{formatCurrency(s.currentPrice)}</td>
                    <td className="py-2 text-right"><PnLText value={s.pnl} format={formatCurrency} /></td>
                    <td className="py-2 text-right"><PnLText value={s.pnlPercentage} format={formatPercentage} /></td>
                  </tr>
                ))}
              </tbody>
            </table>
          ) : <EmptyState message="No P&L data yet" />}
        </Card>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <Card title="Portfolio Value (30 Days)">
          {performance.data && performance.data.length > 0 ? (
            <ResponsiveContainer width="100%" height={220}>
              <AreaChart data={performance.data}>
                <defs><linearGradient id="valueGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#2563eb" stopOpacity={0.2} /><stop offset="95%" stopColor="#2563eb" stopOpacity={0} />
                </linearGradient></defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                <XAxis dataKey="snapshotDate" tick={{ fill: '#6b7280', fontSize: 12 }} />
                <YAxis tick={{ fill: '#6b7280', fontSize: 12 }} tickFormatter={(v) => `₹${(v / 1000).toFixed(0)}k`} />
                <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }} formatter={(value: unknown) => [formatCurrency(Number(value)), 'Value']} />
                <Area type="monotone" dataKey="currentValue" stroke="#2563eb" fill="url(#valueGrad)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          ) : <EmptyState message="No snapshots yet" />}
        </Card>

        <Card title="Trading Signals">
          {signals.data && signals.data.length > 0 ? (
            <div className="space-y-2">
              {signals.data.slice(0, 5).map((sig) => (
                <div key={sig.id} className="flex items-center justify-between py-2 border-b border-gray-100 last:border-0">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">{sig.symbol}</span>
                    <Badge variant={signalBadge[sig.signalType]}>{sig.signalType.replace('_', ' ')}</Badge>
                  </div>
                  <div className="text-right text-sm">
                    {sig.targetPrice && <span className="text-gray-500">Target: {formatCurrency(sig.targetPrice)}</span>}
                  </div>
                </div>
              ))}
            </div>
          ) : <RunAnalysisButton onDone={() => signals.refetch()} />}
        </Card>
      </div>

      {g && (
        <GrowwOrdersSection orders={g.todayOrders} />
      )}
    </div>
  );
}
