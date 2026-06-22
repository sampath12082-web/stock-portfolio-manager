import { useState } from 'react';
import { Search, Sparkles, TrendingUp, TrendingDown, Minus } from 'lucide-react';
import client from '@/api/client';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import { formatCurrency, formatPercentage } from '@/utils/format';

interface SearchResult {
  query: string;
  symbol: string;
  companyName: string;
  exchange: string;
  sector: string;
  industry: string;
  existsInDb: boolean;
  ltp: number | null;
  open: number | null;
  high: number | null;
  low: number | null;
  previousClose: number | null;
  volume: number | null;
  dayChange: number | null;
  dayChangePct: number | null;
  signalType: string;
  targetPrice: number | null;
  signalDate: string | null;
  rationale: string | null;
  aiAnalysis: string;
  error?: string;
}

export default function AiSearchPage() {
  const [query, setQuery] = useState('');
  const [result, setResult] = useState<SearchResult | null>(null);
  const [loading, setLoading] = useState(false);
  const [history, setHistory] = useState<SearchResult[]>([]);

  async function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    if (!query.trim()) return;
    setLoading(true);
    setResult(null);
    try {
      const resp = await client.get<SearchResult>(`/ai/search?query=${encodeURIComponent(query.trim())}`);
      setResult(resp.data);
      if (!resp.data.error) {
        setHistory(prev => [resp.data, ...prev.filter(h => h.symbol !== resp.data.symbol)].slice(0, 10));
      }
    } catch {
      setResult({ error: 'Search failed. Try again.' } as SearchResult);
    } finally {
      setLoading(false);
    }
  }

  const signalConfig: Record<string, { icon: typeof TrendingUp; color: 'green' | 'red' | 'yellow' | 'blue' | 'gray' | 'purple'; label: string }> = {
    BUY_SIGNAL: { icon: TrendingUp, color: 'green', label: 'BUY' },
    SELL_SIGNAL: { icon: TrendingDown, color: 'red', label: 'SELL' },
    HOLD: { icon: Minus, color: 'yellow', label: 'HOLD' },
    WATCH: { icon: Search, color: 'blue', label: 'WATCH' },
    NO_SIGNAL: { icon: Minus, color: 'gray', label: 'NO SIGNAL' },
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Sparkles size={24} className="text-purple-600" />
        <h1 className="text-2xl font-bold text-gray-900">AI Stock Search</h1>
      </div>

      <form onSubmit={handleSearch} className="flex gap-3">
        <div className="relative flex-1">
          <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
          <input value={query} onChange={e => setQuery(e.target.value)} placeholder="Search any stock — RELIANCE, TCS, HDFC, INFY..."
            className="w-full bg-white border border-gray-300 rounded-lg pl-10 pr-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 shadow-sm" />
        </div>
        <button type="submit" disabled={loading}
          className="flex items-center gap-2 px-6 py-3 bg-purple-600 hover:bg-purple-500 text-white rounded-lg text-sm font-medium disabled:opacity-50 shadow-sm">
          <Sparkles size={14} /> {loading ? 'Analyzing...' : 'Analyze'}
        </button>
      </form>

      {loading && <div className="flex justify-center py-12"><LoadingSpinner /></div>}

      {result && !result.error && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-4">
          <div className="lg:col-span-2 space-y-4">
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
              <div className="flex items-center justify-between mb-4">
                <div>
                  <h2 className="text-lg font-bold text-gray-900">{result.symbol}</h2>
                  <p className="text-sm text-gray-500">{result.companyName}</p>
                </div>
                <div className="text-right">
                  <p className="text-2xl font-bold text-gray-900">{formatCurrency(result.ltp)}</p>
                  {result.dayChangePct != null && (
                    <p className={`text-sm font-medium ${result.dayChangePct >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                      {result.dayChange != null && formatCurrency(result.dayChange)} ({result.dayChangePct >= 0 ? '+' : ''}{result.dayChangePct}%)
                    </p>
                  )}
                </div>
              </div>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-3 text-sm">
                <div className="bg-gray-50 rounded p-2"><span className="text-gray-500 block text-xs">Open</span><span className="font-medium">{formatCurrency(result.open)}</span></div>
                <div className="bg-gray-50 rounded p-2"><span className="text-gray-500 block text-xs">High</span><span className="font-medium">{formatCurrency(result.high)}</span></div>
                <div className="bg-gray-50 rounded p-2"><span className="text-gray-500 block text-xs">Low</span><span className="font-medium">{formatCurrency(result.low)}</span></div>
                <div className="bg-gray-50 rounded p-2"><span className="text-gray-500 block text-xs">Prev Close</span><span className="font-medium">{formatCurrency(result.previousClose)}</span></div>
              </div>

              <div className="flex gap-2 mt-3">
                <Badge variant={result.exchange === 'NSE' ? 'blue' : 'yellow'}>{result.exchange}</Badge>
                {result.sector && <Badge variant="gray">{result.sector}</Badge>}
                {result.existsInDb && <Badge variant="green">In Portfolio</Badge>}
              </div>
            </div>

            <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100 flex items-center gap-2">
                <Sparkles size={12} /> AI Analysis
              </h3>
              <p className="text-sm text-gray-700 leading-relaxed whitespace-pre-line">{result.aiAnalysis}</p>
            </div>
          </div>

          <div className="space-y-4">
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Trade Signal</h3>
              {(() => {
                const sig = signalConfig[result.signalType] || signalConfig.NO_SIGNAL;
                const Icon = sig.icon;
                return (
                  <div className="text-center py-4">
                    <div className={`inline-flex items-center gap-2 px-4 py-2 rounded-full text-sm font-bold ${
                      sig.color === 'green' ? 'bg-emerald-100 text-emerald-700' :
                      sig.color === 'red' ? 'bg-red-100 text-red-700' :
                      sig.color === 'yellow' ? 'bg-yellow-100 text-yellow-700' :
                      'bg-gray-100 text-gray-600'
                    }`}>
                      <Icon size={16} /> {sig.label}
                    </div>
                    {result.targetPrice && (
                      <p className="mt-3 text-sm text-gray-600">Target: <span className="font-semibold">{formatCurrency(result.targetPrice)}</span></p>
                    )}
                    {result.rationale && (
                      <p className="mt-2 text-xs text-gray-500">{result.rationale}</p>
                    )}
                    {result.signalDate && (
                      <p className="mt-1 text-xs text-gray-400">Signal date: {result.signalDate}</p>
                    )}
                  </div>
                );
              })()}
            </div>

            <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Details</h3>
              <div className="space-y-2 text-sm">
                <div className="flex justify-between"><span className="text-gray-500">Exchange</span><span className="font-medium">{result.exchange}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Sector</span><span className="font-medium">{result.sector || '—'}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Industry</span><span className="font-medium">{result.industry || '—'}</span></div>
                <div className="flex justify-between"><span className="text-gray-500">Volume</span><span className="font-medium">{result.volume?.toLocaleString() || '—'}</span></div>
              </div>
            </div>
          </div>
        </div>
      )}

      {result?.error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-4 text-sm text-red-700">{result.error}</div>
      )}

      {history.length > 0 && !loading && (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Recent Searches</h3>
          <div className="flex gap-2 flex-wrap">
            {history.map(h => (
              <button key={h.symbol} onClick={() => { setQuery(h.symbol); }}
                className="flex items-center gap-2 px-3 py-1.5 bg-gray-50 hover:bg-gray-100 rounded-full text-sm border border-gray-200">
                <span className="font-medium">{h.symbol}</span>
                {h.ltp && <span className="text-gray-500">{formatCurrency(h.ltp)}</span>}
                <Badge variant={signalConfig[h.signalType]?.color || 'gray'}>{signalConfig[h.signalType]?.label || 'N/A'}</Badge>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  );
}
