import { useState } from 'react';
import { Plus, RefreshCw, Search } from 'lucide-react';
import { useHoldings, useCreateHolding, useUpdateHolding, useSyncGroww } from '@/hooks/useHoldings';
import { useStocks } from '@/hooks/useStocks';
import { useTransactions } from '@/hooks/useTransactions';
import { useActiveSignals } from '@/hooks/useSignals';
import { useAllQuotes } from '@/hooks/useQuotes';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import PnLText from '@/components/ui/PnLText';
import Badge from '@/components/ui/Badge';
import Modal from '@/components/ui/Modal';
import SortHeader, { toggleSort, sortData } from '@/components/ui/SortHeader';
import type { SortConfig } from '@/components/ui/SortHeader';
import { formatCurrency, formatPercentage } from '@/utils/format';
import type { HoldingResponse, SignalType } from '@/api/types';

const signalBadge: Record<SignalType, 'green' | 'red' | 'yellow' | 'blue'> = {
  BUY_SIGNAL: 'green', SELL_SIGNAL: 'red', HOLD: 'yellow', WATCH: 'blue',
};

export default function HoldingsPage() {
  const { data: holdings, isLoading } = useHoldings();
  const { data: stocks } = useStocks();
  const { data: txns } = useTransactions();
  const { data: signals } = useActiveSignals();
  const { data: quotes } = useAllQuotes();
  const createMut = useCreateHolding();
  const updateMut = useUpdateHolding();
  const syncMut = useSyncGroww();
  const [showAdd, setShowAdd] = useState(false);
  const [editing, setEditing] = useState<HoldingResponse | null>(null);
  const [syncResult, setSyncResult] = useState<string | null>(null);
  const [signalFilter, setSignalFilter] = useState<string>('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [sort, setSort] = useState<SortConfig | null>(null);

  if (isLoading) return <LoadingSpinner />;

  const totalInvested = holdings?.reduce((s, h) => s + h.investedAmount, 0) ?? 0;
  const totalCurrent = holdings?.reduce((s, h) => s + (h.currentValue ?? h.investedAmount), 0) ?? 0;
  const totalPnL = totalCurrent - totalInvested;

  const signalMap = new Map<string, { type: SignalType; target?: number }>();
  signals?.forEach((s) => {
    if (!signalMap.has(s.symbol)) {
      signalMap.set(s.symbol, { type: s.signalType, target: s.targetPrice });
    }
  });

  const stockNameMap = new Map<string, string>();
  stocks?.forEach((s) => stockNameMap.set(s.symbol, s.companyName));

  const holdingPnlMap = new Map<string, { realizedPnL: number; unrealizedPnL: number | null }>();
  if (txns && holdings) {
    const txnTotals = new Map<string, { bought: number; sold: number }>();
    txns.forEach((t) => {
      if (!t.symbol) return;
      const e = txnTotals.get(t.symbol) || { bought: 0, sold: 0 };
      if (t.transactionType === 'BUY') e.bought += t.totalAmount;
      else if (t.transactionType === 'SELL') e.sold += t.totalAmount;
      txnTotals.set(t.symbol, e);
    });
    holdings.forEach((h) => {
      const tt = txnTotals.get(h.symbol);
      if (!tt) return;
      const currentHoldingCost = h.quantity > 0 ? h.averageBuyPrice * h.quantity : 0;
      const soldCost = tt.bought - currentHoldingCost;
      const realizedPnL = tt.sold - soldCost;
      const ltp = quotes?.[h.symbol]?.ltp ?? null;
      const unrealizedPnL = h.quantity > 0 && ltp ? (ltp * h.quantity) - currentHoldingCost : null;
      holdingPnlMap.set(h.symbol, { realizedPnL, unrealizedPnL });
    });
  }

  const signalPriority: Record<string, number> = { SELL_SIGNAL: 0, HOLD: 1, BUY_SIGNAL: 2, WATCH: 3 };

  const sortedHoldings = holdings ? [...holdings].sort((a, b) => {
    const aActive = a.quantity > 0 ? 0 : 1;
    const bActive = b.quantity > 0 ? 0 : 1;
    if (aActive !== bActive) return aActive - bActive;
    const aSig = signalMap.get(a.symbol);
    const bSig = signalMap.get(b.symbol);
    const aPri = aSig ? (signalPriority[aSig.type] ?? 4) : 4;
    const bPri = bSig ? (signalPriority[bSig.type] ?? 4) : 4;
    if (aPri !== bPri) return aPri - bPri;
    return (b.currentValue ?? 0) - (a.currentValue ?? 0);
  }) : [];

  const signalCounts = { ALL: sortedHoldings.length, BUY: 0, SELL: 0, HOLD: 0, NONE: 0 };
  sortedHoldings.forEach((h) => {
    const sig = signalMap.get(h.symbol);
    if (!sig) signalCounts.NONE++;
    else if (sig.type === 'BUY_SIGNAL') signalCounts.BUY++;
    else if (sig.type === 'SELL_SIGNAL') signalCounts.SELL++;
    else if (sig.type === 'HOLD') signalCounts.HOLD++;
  });

  const filteredHoldings = sortedHoldings.filter((h) => {
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      const name = stockNameMap.get(h.symbol) || '';
      if (!h.symbol.toLowerCase().includes(q) && !name.toLowerCase().includes(q)) return false;
    }
    if (signalFilter === 'ALL') return true;
    const sig = signalMap.get(h.symbol);
    if (signalFilter === 'NONE') return !sig;
    if (signalFilter === 'BUY') return sig?.type === 'BUY_SIGNAL';
    if (signalFilter === 'SELL') return sig?.type === 'SELL_SIGNAL';
    if (signalFilter === 'HOLD') return sig?.type === 'HOLD';
    return true;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-2">
        <h1 className="text-2xl font-bold text-gray-900">Holdings</h1>
        <div className="flex gap-2">
          <button onClick={() => {
            syncMut.mutate(undefined, {
              onSuccess: (data) => {
                setSyncResult(`Synced: ${data.stocksCreated} stocks created, ${data.holdingsCreated} new, ${data.holdingsUpdated} updated${data.errors.length ? `. Errors: ${data.errors.join(', ')}` : ''}`);
                setTimeout(() => setSyncResult(null), 8000);
              },
              onError: (err) => {
                setSyncResult(`Sync failed: ${err.message}`);
                setTimeout(() => setSyncResult(null), 5000);
              },
            });
          }} disabled={syncMut.isPending}
            className="flex items-center gap-2 px-3 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
            <RefreshCw size={16} className={syncMut.isPending ? 'animate-spin' : ''} />
            {syncMut.isPending ? 'Syncing...' : 'Sync from Groww'}
          </button>
          <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium">
            <Plus size={16} /> Add Holding
          </button>
        </div>
      </div>

      {syncResult && (
        <div className={`p-3 rounded-md text-sm ${syncResult.includes('failed') ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-emerald-50 text-emerald-700 border border-emerald-200'}`}>
          {syncResult}
        </div>
      )}

      <div className="grid grid-cols-3 gap-4">
        <div className="bg-white border border-gray-200 rounded-lg p-3 text-center shadow-sm">
          <p className="text-xs text-gray-500">Invested</p>
          <p className="text-lg font-semibold text-gray-900">{formatCurrency(totalInvested)}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-3 text-center shadow-sm">
          <p className="text-xs text-gray-500">Current</p>
          <p className="text-lg font-semibold text-gray-900">{formatCurrency(totalCurrent)}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg p-3 text-center shadow-sm">
          <p className="text-xs text-gray-500">P&L</p>
          <p className="text-lg font-semibold"><PnLText value={totalPnL} format={formatCurrency} /></p>
        </div>
      </div>

      <div className="relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input placeholder="Search holdings..." value={searchQuery} onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full bg-gray-50 border border-gray-200 rounded-md pl-9 pr-3 py-2 text-sm" />
      </div>

      <div className="flex gap-2 flex-wrap">
        {([['ALL', signalCounts.ALL], ['BUY', signalCounts.BUY], ['SELL', signalCounts.SELL], ['HOLD', signalCounts.HOLD], ['NONE', signalCounts.NONE]] as const).map(([key, count]) => (
          <button key={key} onClick={() => setSignalFilter(key)}
            className={`flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-medium transition-colors ${signalFilter === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            {key === 'NONE' ? 'No Signal' : key}
            <span className={`rounded-full px-1.5 py-0.5 text-[10px] ${signalFilter === key ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-600'}`}>{count}</span>
          </button>
        ))}
      </div>

      {filteredHoldings.length > 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-auto max-h-[70vh]">
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10">
              <tr className="text-gray-500 text-left border-b border-gray-200 bg-gray-50">
                <SortHeader label="Stock" sortKey="symbol" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
                <SortHeader label="Qty" sortKey="quantity" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="Avg Price" sortKey="averageBuyPrice" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="LTP" sortKey="currentPrice" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <th className="py-3 px-3 text-right">Target</th>
                <SortHeader label="Invested" sortKey="investedAmount" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="Current Value" sortKey="currentValue" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="Realized P&L" sortKey="realizedPnL" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="Unrealized P&L" sortKey="unrealizedPnL" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
                <SortHeader label="Signal" sortKey="signal" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="center" />
                <th className="py-3 px-3"></th>
              </tr>
            </thead>
            <tbody>
              {sortData(filteredHoldings, sort, (h, k) => {
                if (k === 'symbol') return stockNameMap.get(h.symbol) || h.symbol;
                const pnl = holdingPnlMap.get(h.symbol);
                if (k === 'realizedPnL') return pnl?.realizedPnL ?? null;
                if (k === 'unrealizedPnL') return pnl?.unrealizedPnL ?? null;
                if (k === 'signal') { const sig = signalMap.get(h.symbol); return sig ? ({'SELL_SIGNAL':0,'HOLD':1,'BUY_SIGNAL':2,'WATCH':3}[sig.type] ?? 4) : null; }
                return (h as unknown as Record<string, unknown>)[k] as number ?? null;
              }).map((h) => {
                const sig = signalMap.get(h.symbol);
                const pnl = holdingPnlMap.get(h.symbol);
                const realizedPnL = pnl?.realizedPnL ?? null;
                const unrealizedPnL = pnl?.unrealizedPnL ?? null;
                const totalPnl = (realizedPnL ?? 0) + (unrealizedPnL ?? 0);
                const hasTraded = pnl != null;
                const isLoss = hasTraded && totalPnl < 0;
                const isGain = hasTraded && totalPnl > 0;
                const rowClass = isLoss ? 'bg-red-50 text-red-700' : isGain ? 'bg-emerald-50 text-emerald-700' : '';
                const isHeld = h.quantity > 0;
                return (
                  <tr key={h.id} className={`border-b border-gray-100 hover:opacity-80 ${rowClass} ${isHeld ? 'border-l-4 border-l-blue-500' : ''} ${!isHeld ? 'opacity-40' : ''}`}>
                    <td className="py-2.5 px-3"><span className="font-medium">{stockNameMap.get(h.symbol) || h.symbol}</span><br /><span className="text-xs opacity-70">{h.symbol}</span></td>
                    <td className="py-2.5 px-3 text-right">{h.quantity}</td>
                    <td className="py-2.5 px-3 text-right">{formatCurrency(h.averageBuyPrice)}</td>
                    <td className="py-2.5 px-3 text-right font-medium">{h.currentPrice != null ? formatCurrency(h.currentPrice) : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right">{sig?.target ? formatCurrency(sig.target) : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right">{formatCurrency(h.investedAmount)}</td>
                    <td className="py-2.5 px-3 text-right">{formatCurrency(h.currentValue)}</td>
                    <td className="py-2.5 px-3 text-right">{realizedPnL !== null ? <PnLText value={realizedPnL} format={formatCurrency} /> : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right">{unrealizedPnL !== null ? <PnLText value={unrealizedPnL} format={formatCurrency} /> : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-center">
                      {sig ? <Badge variant={signalBadge[sig.type]}>{sig.type.replace('_', ' ')}</Badge> : <span className="text-gray-300">—</span>}
                    </td>
                    <td className="py-2.5 px-3">
                      <button onClick={() => setEditing(h)} className="text-blue-600 hover:text-blue-500 text-xs">Edit</button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : (
        <EmptyState message="No holdings yet" action={<button onClick={() => setShowAdd(true)} className="text-blue-600 text-sm">Add your first holding</button>} />
      )}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Holding">
        <form onSubmit={(e) => {
          e.preventDefault();
          const fd = new FormData(e.currentTarget);
          createMut.mutate({ symbol: fd.get('symbol') as string, quantity: Number(fd.get('quantity')), averageBuyPrice: Number(fd.get('averageBuyPrice')) },
            { onSuccess: () => setShowAdd(false) });
        }} className="space-y-4">
          <div><label className="block text-sm text-gray-500 mb-1">Symbol</label>
            <select name="symbol" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
              <option value="">Select stock...</option>
              {stocks?.map((s) => <option key={s.id} value={s.symbol}>{s.symbol} - {s.companyName}</option>)}
            </select></div>
          <div><label className="block text-sm text-gray-500 mb-1">Quantity</label>
            <input name="quantity" type="number" min="1" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-gray-500 mb-1">Average Buy Price</label>
            <input name="averageBuyPrice" type="number" step="0.01" min="0.01" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <button type="submit" disabled={createMut.isPending} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
            {createMut.isPending ? 'Adding...' : 'Add Holding'}</button>
        </form>
      </Modal>

      <Modal open={!!editing} onClose={() => setEditing(null)} title={`Edit ${editing?.symbol}`}>
        {editing && (
          <form onSubmit={(e) => {
            e.preventDefault();
            const fd = new FormData(e.currentTarget);
            updateMut.mutate({ id: editing.id, data: { quantity: Number(fd.get('quantity')), averageBuyPrice: Number(fd.get('averageBuyPrice')) } },
              { onSuccess: () => setEditing(null) });
          }} className="space-y-4">
            <div><label className="block text-sm text-gray-500 mb-1">Quantity</label>
              <input name="quantity" type="number" min="1" defaultValue={editing.quantity} required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-gray-500 mb-1">Average Buy Price</label>
              <input name="averageBuyPrice" type="number" step="0.01" min="0.01" defaultValue={editing.averageBuyPrice} required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <button type="submit" disabled={updateMut.isPending} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
              {updateMut.isPending ? 'Saving...' : 'Save Changes'}</button>
          </form>
        )}
      </Modal>
    </div>
  );
}
