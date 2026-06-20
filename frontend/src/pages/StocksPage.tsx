import { useState, useEffect, useRef } from 'react';
import { Plus, Search, Loader2, Database, Globe, Trash2 } from 'lucide-react';
import { useStocks, useCreateStock, useLookupStocks, useDeleteStock } from '@/hooks/useStocks';
import { useHoldings } from '@/hooks/useHoldings';
import { useActiveSignals } from '@/hooks/useSignals';
import { useAllQuotes } from '@/hooks/useQuotes';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import Badge from '@/components/ui/Badge';
import Modal from '@/components/ui/Modal';
import SortHeader, { toggleSort, sortData } from '@/components/ui/SortHeader';
import type { SortConfig } from '@/components/ui/SortHeader';
import { formatCurrency, formatPercentage } from '@/utils/format';
import type { StockLookupResponse, Exchange, SignalType } from '@/api/types';

const signalBadge: Record<SignalType, 'green' | 'red' | 'yellow' | 'blue'> = {
  BUY_SIGNAL: 'green', SELL_SIGNAL: 'red', HOLD: 'yellow', WATCH: 'blue',
};

function AddStockModal({ open, onClose, onAdded }: { open: boolean; onClose: () => void; onAdded: () => void }) {
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [picked, setPicked] = useState<StockLookupResponse | null>(null);
  const [showDropdown, setShowDropdown] = useState(false);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const createMut = useCreateStock();

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(searchQuery), 300);
    return () => clearTimeout(timer);
  }, [searchQuery]);

  const lookup = useLookupStocks(debouncedQuery);

  useEffect(() => {
    if (!open) { setSearchQuery(''); setDebouncedQuery(''); setPicked(null); setShowDropdown(false); }
  }, [open]);

  function selectResult(result: StockLookupResponse) {
    setPicked(result); setSearchQuery(result.symbol); setShowDropdown(false);
  }

  return (
    <Modal open={open} onClose={onClose} title="Add Stock">
      <div className="space-y-4">
        <div className="relative" ref={dropdownRef}>
          <label className="block text-sm text-gray-500 mb-1">Search stock (NSE / BSE)</label>
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input placeholder="Type symbol or company name..." value={searchQuery}
              onChange={(e) => { setSearchQuery(e.target.value); setPicked(null); setShowDropdown(true); }}
              onFocus={() => { if (lookup.data?.length) setShowDropdown(true); }}
              className="w-full bg-gray-50 border border-gray-300 rounded-md pl-9 pr-9 py-2 text-sm" />
            {lookup.isFetching && <Loader2 size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 animate-spin" />}
          </div>
          {showDropdown && lookup.data && lookup.data.length > 0 && (
            <div className="absolute z-10 mt-1 w-full bg-white border border-gray-200 rounded-md shadow-lg max-h-60 overflow-y-auto">
              {lookup.data.map((r, i) => (
                <button key={`${r.symbol}-${r.exchange}-${i}`} type="button" onClick={() => selectResult(r)}
                  className="w-full text-left px-3 py-2.5 hover:bg-gray-50 flex items-center gap-3 border-b border-gray-100 last:border-0">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2">
                      <span className="font-semibold text-sm text-gray-900">{r.symbol}</span>
                      <Badge variant={r.exchange === 'NSE' ? 'blue' : 'yellow'}>{r.exchange}</Badge>
                      {r.existsInDb ? <span className="flex items-center gap-1 text-xs text-emerald-600"><Database size={10} />In DB</span>
                        : <span className="flex items-center gap-1 text-xs text-blue-600"><Globe size={10} />Web</span>}
                    </div>
                    <p className="text-xs text-gray-500 truncate">{r.companyName}</p>
                  </div>
                </button>
              ))}
            </div>
          )}
        </div>
        {picked && (
          <form onSubmit={(e) => {
            e.preventDefault();
            const fd = new FormData(e.currentTarget);
            createMut.mutate({ symbol: fd.get('symbol') as string, companyName: fd.get('companyName') as string, exchange: fd.get('exchange') as Exchange, sector: (fd.get('sector') as string) || undefined, industry: (fd.get('industry') as string) || undefined },
              { onSuccess: onAdded });
          }} className="space-y-3">
            <div><label className="block text-sm text-gray-500 mb-1">Symbol</label><input name="symbol" required value={picked.symbol} onChange={(e) => setPicked({ ...picked, symbol: e.target.value })} className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-gray-500 mb-1">Company Name</label><input name="companyName" required value={picked.companyName} onChange={(e) => setPicked({ ...picked, companyName: e.target.value })} className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-gray-500 mb-1">Exchange</label><select name="exchange" required value={picked.exchange ?? 'NSE'} onChange={(e) => setPicked({ ...picked, exchange: e.target.value })} className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm"><option value="NSE">NSE</option><option value="BSE">BSE</option></select></div>
            <div><label className="block text-sm text-gray-500 mb-1">Sector</label><input name="sector" value={picked.sector ?? ''} onChange={(e) => setPicked({ ...picked, sector: e.target.value })} className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-gray-500 mb-1">Industry</label><input name="industry" value={picked.industry ?? ''} onChange={(e) => setPicked({ ...picked, industry: e.target.value })} className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
            <button type="submit" disabled={createMut.isPending || picked.existsInDb} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
              {createMut.isPending ? 'Adding...' : picked.existsInDb ? 'Already in database' : 'Add Stock'}</button>
          </form>
        )}
      </div>
    </Modal>
  );
}

export default function StocksPage() {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const allStocks = useStocks();
  const { data: holdings } = useHoldings();
  const { data: signals } = useActiveSignals();
  const { data: quotes } = useAllQuotes();
  const lookupResults = useLookupStocks(debouncedQuery);
  const deleteMut = useDeleteStock();
  const createMut = useCreateStock();
  const [showAdd, setShowAdd] = useState(false);
  const [sort, setSort] = useState<SortConfig | null>(null);
  const [signalFilter, setSignalFilter] = useState<string>('ALL');
  const [targetFilter, setTargetFilter] = useState<string>('ALL');

  useEffect(() => {
    const timer = setTimeout(() => setDebouncedQuery(query), 300);
    return () => clearTimeout(timer);
  }, [query]);

  const stocks = allStocks.data;
  if (allStocks.isLoading) return <LoadingSpinner />;

  const holdingMap = new Map<string, { qty: number; avg: number }>();
  holdings?.forEach((h) => holdingMap.set(h.symbol, { qty: h.quantity, avg: h.averageBuyPrice }));

  const signalMap = new Map<string, { type: SignalType; target?: number }>();
  signals?.forEach((s) => {
    if (!signalMap.has(s.symbol)) signalMap.set(s.symbol, { type: s.signalType, target: s.targetPrice });
  });

  const filtered = (query
    ? stocks?.filter((s) => s.symbol.toLowerCase().includes(query.toLowerCase()) || s.companyName.toLowerCase().includes(query.toLowerCase()))
    : stocks)?.filter((s) => {
    const sig = signalMap.get(s.symbol);
    if (signalFilter !== 'ALL') {
      if (signalFilter === 'NONE' && sig) return false;
      if (signalFilter === 'BUY' && sig?.type !== 'BUY_SIGNAL') return false;
      if (signalFilter === 'SELL' && sig?.type !== 'SELL_SIGNAL') return false;
      if (signalFilter === 'HOLD' && sig?.type !== 'HOLD') return false;
      if (signalFilter === 'WATCH' && sig?.type !== 'WATCH') return false;
    }
    if (targetFilter === 'WITH_TARGET' && !sig?.target) return false;
    if (targetFilter === 'WITHOUT_TARGET' && sig?.target) return false;
    return true;
  })?.sort((a, b) => {
    const aHeld = holdingMap.has(a.symbol) && (holdingMap.get(a.symbol)!.qty > 0) ? 0 : 1;
    const bHeld = holdingMap.has(b.symbol) && (holdingMap.get(b.symbol)!.qty > 0) ? 0 : 1;
    return aHeld - bHeld;
  });

  const signalCounts = { ALL: stocks?.length ?? 0, BUY: 0, SELL: 0, HOLD: 0, WATCH: 0, NONE: 0 };
  stocks?.forEach((s) => {
    const sig = signalMap.get(s.symbol);
    if (!sig) signalCounts.NONE++;
    else if (sig.type === 'BUY_SIGNAL') signalCounts.BUY++;
    else if (sig.type === 'SELL_SIGNAL') signalCounts.SELL++;
    else if (sig.type === 'HOLD') signalCounts.HOLD++;
    else if (sig.type === 'WATCH') signalCounts.WATCH++;
  });
  const targetCounts = {
    ALL: stocks?.length ?? 0,
    WITH_TARGET: stocks?.filter((s) => signalMap.get(s.symbol)?.target).length ?? 0,
    WITHOUT_TARGET: stocks?.filter((s) => !signalMap.get(s.symbol)?.target).length ?? 0,
  };

  const webResults = lookupResults.data?.filter((r) => !r.existsInDb) ?? [];

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">Stocks <span className="text-sm text-gray-500 font-normal">({filtered?.length ?? 0})</span></h1>
        <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium">
          <Plus size={16} /> Add Stock
        </button>
      </div>

      <div className="relative">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
        <input placeholder="Search stocks..." value={query} onChange={(e) => setQuery(e.target.value)}
          className="w-full bg-gray-50 border border-gray-200 rounded-md pl-9 pr-3 py-2 text-sm" />
      </div>

      <div className="flex gap-4 flex-wrap items-center">
        <div className="flex gap-1.5 items-center">
          <span className="text-xs text-gray-500 mr-1">Signal:</span>
          {(['ALL', 'BUY', 'SELL', 'HOLD', 'WATCH', 'NONE'] as const).map((key) => {
            const count = signalCounts[key];
            if (key !== 'ALL' && count === 0) return null;
            return (
              <button key={key} onClick={() => setSignalFilter(key)}
                className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${signalFilter === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
                {key === 'NONE' ? 'No Signal' : key}
                <span className={`rounded-full px-1.5 py-0.5 text-[10px] ${signalFilter === key ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-600'}`}>{count}</span>
              </button>
            );
          })}
        </div>
        <div className="flex gap-1.5 items-center">
          <span className="text-xs text-gray-500 mr-1">Target:</span>
          {([['ALL', 'All', targetCounts.ALL], ['WITH_TARGET', 'Has Target', targetCounts.WITH_TARGET], ['WITHOUT_TARGET', 'No Target', targetCounts.WITHOUT_TARGET]] as const).map(([key, label, count]) => (
            <button key={key} onClick={() => setTargetFilter(key)}
              className={`flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-medium transition-colors ${targetFilter === key ? 'bg-blue-600 text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
              {label}
              <span className={`rounded-full px-1.5 py-0.5 text-[10px] ${targetFilter === key ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-600'}`}>{count}</span>
            </button>
          ))}
        </div>
      </div>

      {filtered && filtered.length > 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-auto max-h-[70vh]">
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10"><tr className="text-gray-500 text-left border-b border-gray-200 bg-gray-50">
              <SortHeader label="Stock" sortKey="symbol" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="Exchange" sortKey="exchange" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="My Qty" sortKey="qty" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Avg Price" sortKey="avg" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Current Price" sortKey="ltp" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Target" sortKey="target" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Signal" sortKey="signal" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="center" />
              <th className="py-3 px-3"></th>
            </tr></thead>
            <tbody>
              {sortData(filtered ?? [], sort, (s, k) => {
                const h = holdingMap.get(s.symbol);
                const ltp = quotes?.[s.symbol]?.ltp ?? null;
                const sig = signalMap.get(s.symbol);
                if (k === 'symbol') return s.symbol;
                if (k === 'exchange') return s.exchange;
                if (k === 'ltp') return ltp;
                if (k === 'qty') return h?.qty ?? null;
                if (k === 'avg') return h?.avg ?? null;
                if (k === 'target') return sig?.target ?? null;
                if (k === 'signal') return sig ? ({'SELL_SIGNAL':0,'HOLD':1,'BUY_SIGNAL':2,'WATCH':3}[sig.type] ?? 4) : null;
                return null;
              }).map((s) => {
                const h = holdingMap.get(s.symbol);
                const ltp = quotes?.[s.symbol]?.ltp;
                const isHeld = h && h.qty > 0;
                const sig = signalMap.get(s.symbol);
                return (
                  <tr key={s.id} className={`border-b border-gray-100 hover:bg-gray-50 ${isHeld ? 'border-l-4 border-l-blue-500' : ''}`}>
                    <td className="py-2.5 px-3"><span className="font-semibold">{s.symbol}</span><br /><span className="text-xs opacity-70">{s.companyName}</span></td>
                    <td className="py-2.5 px-3"><Badge variant={s.exchange === 'NSE' ? 'blue' : 'yellow'}>{s.exchange}</Badge></td>
                    <td className="py-2.5 px-3 text-right">{h ? h.qty : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right">{h && h.avg > 0 ? formatCurrency(h.avg) : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right font-medium">{ltp ? formatCurrency(ltp) : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-right">{sig?.target ? formatCurrency(sig.target) : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3 text-center">{sig ? <Badge variant={signalBadge[sig.type]}>{sig.type.replace('_', ' ')}</Badge> : <span className="text-gray-300">—</span>}</td>
                    <td className="py-2.5 px-3">
                      <button onClick={() => { if (confirm(`Delete ${s.symbol}?`)) deleteMut.mutate(s.id); }}
                        className="text-gray-400 hover:text-red-600 p-1" title="Delete">
                        <Trash2 size={14} />
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      ) : <EmptyState message={query ? 'No stocks match your search' : 'No stocks added yet'} />}

      {query && webResults.length > 0 && (
        <div className="bg-white border border-blue-200 rounded-lg shadow-sm overflow-x-auto">
          <div className="px-3 py-2 bg-blue-50 border-b border-blue-200 text-sm text-blue-700 font-medium flex items-center gap-2">
            <Globe size={14} /> Web Results — Click + to add
          </div>
          <table className="w-full text-sm">
            <tbody>
              {webResults.map((r, i) => (
                <tr key={`web-${r.symbol}-${i}`} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-2.5 px-3"><span className="font-semibold text-gray-900">{r.symbol}</span><br /><span className="text-xs text-gray-500">{r.companyName}</span></td>
                  <td className="py-2.5 px-3"><Badge variant={r.exchange === 'NSE' ? 'blue' : 'yellow'}>{r.exchange}</Badge></td>
                  <td className="py-2.5 px-3" colSpan={5}></td>
                  <td className="py-2.5 px-3">
                    <button onClick={() => createMut.mutate({
                      symbol: r.symbol, companyName: r.companyName,
                      exchange: (r.exchange as Exchange) ?? 'NSE',
                      sector: r.sector ?? undefined, industry: r.industry ?? undefined,
                    }, { onSuccess: () => allStocks.refetch() })}
                      disabled={createMut.isPending}
                      className="text-blue-600 hover:text-blue-500 p-1 font-bold" title="Add to database">
                      <Plus size={16} />
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <AddStockModal open={showAdd} onClose={() => setShowAdd(false)} onAdded={() => { setShowAdd(false); allStocks.refetch(); }} />
    </div>
  );
}
