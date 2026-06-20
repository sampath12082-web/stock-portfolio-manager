import { useState, useRef } from 'react';
import { Plus, Upload, RefreshCw } from 'lucide-react';
import { useTransactions, useCreateTransaction, useTransactionAnalytics, useSyncGrowwOrders } from '@/hooks/useTransactions';
import { uploadTransactionPdf } from '@/api/endpoints';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import Badge from '@/components/ui/Badge';
import StatCard from '@/components/ui/StatCard';
import Modal from '@/components/ui/Modal';
import Card from '@/components/ui/Card';
import SortHeader, { toggleSort, sortData } from '@/components/ui/SortHeader';
import type { SortConfig } from '@/components/ui/SortHeader';
import { formatCurrency, formatDateTime } from '@/utils/format';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid } from 'recharts';
import type { TransactionType, CreateTransactionRequest } from '@/api/types';

const typeBadge: Record<TransactionType, 'green' | 'red' | 'blue' | 'purple' | 'yellow' | 'gray'> = {
  BUY: 'green', SELL: 'red', BONUS: 'blue', SPLIT: 'purple', DIVIDEND: 'yellow',
  DEPOSIT: 'green', WITHDRAWAL: 'red', CHARGES: 'gray',
};

const FUND_TYPES = ['DEPOSIT', 'WITHDRAWAL', 'CHARGES'];

function AddTransactionForm({ onSuccess }: { onSuccess: () => void }) {
  const [txnType, setTxnType] = useState('BUY');
  const createMut = useCreateTransaction();
  const isFund = FUND_TYPES.includes(txnType);

  return (
    <form onSubmit={(e) => {
      e.preventDefault();
      const fd = new FormData(e.currentTarget);
      const data: CreateTransactionRequest = {
        transactionType: fd.get('transactionType') as TransactionType,
        price: Number(fd.get('price')),
        ...(isFund
          ? { description: fd.get('description') as string }
          : { symbol: fd.get('symbol') as string, quantity: Number(fd.get('quantity')) }),
      };
      createMut.mutate(data, { onSuccess });
    }} className="space-y-4">
      <div><label className="block text-sm text-gray-500 mb-1">Type</label>
        <select name="transactionType" value={txnType} onChange={(e) => setTxnType(e.target.value)} required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
          {['BUY', 'SELL', 'BONUS', 'SPLIT', 'DIVIDEND', 'DEPOSIT', 'WITHDRAWAL', 'CHARGES'].map((t) => <option key={t} value={t}>{t}</option>)}
        </select></div>
      {isFund ? (
        <div><label className="block text-sm text-gray-500 mb-1">Description</label>
          <input name="description" placeholder="e.g. UPI deposit" className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
      ) : (
        <>
          <div><label className="block text-sm text-gray-500 mb-1">Symbol</label><input name="symbol" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-gray-500 mb-1">Quantity</label><input name="quantity" type="number" min="1" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
        </>
      )}
      <div><label className="block text-sm text-gray-500 mb-1">{isFund ? 'Amount' : 'Price per unit'}</label>
        <input name="price" type="number" step="0.01" min="0.01" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
      <button type="submit" disabled={createMut.isPending} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
        {createMut.isPending ? 'Recording...' : 'Record Transaction'}</button>
    </form>
  );
}

export default function TransactionsPage() {
  const [filters, setFilters] = useState<{ symbol?: string; type?: string }>({});
  const { data: txns, isLoading, refetch } = useTransactions(filters);
  const analytics = useTransactionAnalytics();
  const createMut = useCreateTransaction();
  const syncOrdersMut = useSyncGrowwOrders();
  const [showAdd, setShowAdd] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [sort, setSort] = useState<SortConfig | null>(null);
  const [uploadMsg, setUploadMsg] = useState<string | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  if (isLoading) return <LoadingSpinner />;

  const monthData = analytics.data?.transactionsByMonth
    ? Object.entries(analytics.data.transactionsByMonth).map(([month, count]) => ({ month, count }))
    : [];

  async function handleUpload(file: File) {
    setUploading(true);
    setUploadMsg(null);
    try {
      const result = await uploadTransactionPdf(file);
      const msgs = [];
      if (result.transactionsCreated > 0) msgs.push(`${result.transactionsCreated} transactions imported`);
      if (result.stocksCreated > 0) msgs.push(`${result.stocksCreated} stocks created`);
      if (result.errors.length > 0) msgs.push(...result.errors);
      if (msgs.length === 0) msgs.push('File parsed but no transactions found');
      setUploadMsg(msgs.join('\n'));
      refetch();
      analytics.refetch();
    } catch (err: unknown) {
      setUploadMsg(`Upload failed: ${err instanceof Error ? err.message : 'Unknown error'}`);
    } finally {
      setUploading(false);
    }
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-2">
        <h1 className="text-2xl font-bold text-gray-900">Transactions</h1>
        <div className="flex gap-2">
          <button onClick={() => {
            syncOrdersMut.mutate(undefined, {
              onSuccess: (data) => {
                setUploadMsg(`Synced from Groww: ${data.holdingsCreated} transactions imported, ${data.stocksCreated} stocks created${data.errors.length ? `. Errors: ${data.errors.join(', ')}` : ''}`);
                refetch(); analytics.refetch();
              },
              onError: (err) => setUploadMsg(`Sync failed: ${err.message}`),
            });
          }} disabled={syncOrdersMut.isPending}
            className="flex items-center gap-2 px-3 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
            <RefreshCw size={16} className={syncOrdersMut.isPending ? 'animate-spin' : ''} />
            {syncOrdersMut.isPending ? 'Syncing...' : 'Sync from Groww'}
          </button>
          <input ref={fileRef} type="file" accept=".pdf" className="hidden" onChange={(e) => { if (e.target.files?.[0]) handleUpload(e.target.files[0]); }} />
          <button onClick={() => fileRef.current?.click()} disabled={uploading}
            className="flex items-center gap-2 px-3 py-2 bg-gray-100 hover:bg-gray-200 border border-gray-300 text-gray-700 rounded-md text-sm font-medium disabled:opacity-50">
            <Upload size={16} /> {uploading ? 'Uploading...' : 'Upload PDF'}
          </button>
          <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium">
            <Plus size={16} /> Add Transaction
          </button>
        </div>
      </div>

      {uploadMsg && (
        <div className={`p-3 rounded-md text-sm whitespace-pre-line ${uploadMsg.includes('failed') ? 'bg-red-50 text-red-700 border border-red-200' : uploadMsg.includes('Note:') ? 'bg-blue-50 text-blue-700 border border-blue-200' : 'bg-emerald-50 text-emerald-700 border border-emerald-200'}`}>
          {uploadMsg}
        </div>
      )}

      {analytics.data && (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
            <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Fund Flow</h3>
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">Deposited</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.totalDeposits)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Withdrawn</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.totalWithdrawals)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Charges</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.totalCharges)}</span></div>
              </div>
            </div>
            <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Delivery (CNC)</h3>
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">Buy</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.deliveryBuyAmount)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Sell</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.deliverySellAmount)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Realized P&L</span><span className={`font-medium ${analytics.data.deliveryRealizedGains >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>{formatCurrency(analytics.data.deliveryRealizedGains)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Trades</span><span className="font-medium text-gray-900">{analytics.data.deliveryCount}</span></div>
              </div>
            </div>
            <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Intraday (MIS)</h3>
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">Buy</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.intradayBuyAmount)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Sell</span><span className="font-medium text-gray-900">{formatCurrency(analytics.data.intradaySellAmount)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Net P&L</span><span className={`font-medium ${analytics.data.intradayPnL >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>{formatCurrency(analytics.data.intradayPnL)}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Trades</span><span className="font-medium text-gray-900">{analytics.data.intradayCount}</span></div>
              </div>
            </div>
            <div className="bg-white border border-gray-200 rounded-lg p-4 shadow-sm">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-2.5 border-b border-gray-100">Activity</h3>
              <div className="space-y-1.5">
                <div className="flex justify-between text-sm"><span className="text-gray-500">Total Txns</span><span className="font-medium text-gray-900">{analytics.data.totalTransactions}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Buy / Sell</span><span className="font-medium text-gray-900">{analytics.data.buyCount} / {analytics.data.sellCount}</span></div>
                <div className="flex justify-between text-sm"><span className="text-gray-500">Most Traded</span><span className="font-medium text-gray-900">{analytics.data.mostTradedStock ?? '—'}</span></div>
              </div>
            </div>
          </div>
          {monthData.length > 0 && (
            <Card title="Transactions by Month">
              <ResponsiveContainer width="100%" height={200}>
                <BarChart data={monthData}>
                  <CartesianGrid strokeDasharray="3 3" stroke="#e5e7eb" />
                  <XAxis dataKey="month" tick={{ fill: '#6b7280', fontSize: 12 }} />
                  <YAxis tick={{ fill: '#6b7280', fontSize: 12 }} />
                  <Tooltip contentStyle={{ backgroundColor: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px' }} />
                  <Bar dataKey="count" fill="#2563eb" radius={[4, 4, 0, 0]} />
                </BarChart>
              </ResponsiveContainer>
            </Card>
          )}
        </>
      )}

      <div className="flex gap-3 flex-wrap">
        <input placeholder="Filter by symbol..." value={filters.symbol ?? ''} onChange={(e) => setFilters({ symbol: e.target.value || undefined })}
          className="bg-gray-50 border border-gray-300 rounded-md px-3 py-1.5 text-sm w-40" />
        <select value={filters.type ?? ''} onChange={(e) => setFilters({ type: e.target.value || undefined })}
          className="bg-gray-50 border border-gray-300 rounded-md px-3 py-1.5 text-sm">
          <option value="">All Types</option>
          {['BUY', 'SELL', 'BONUS', 'SPLIT', 'DIVIDEND', 'DEPOSIT', 'WITHDRAWAL', 'CHARGES'].map((t) => <option key={t} value={t}>{t}</option>)}
        </select>
        {(filters.symbol || filters.type) && <button onClick={() => setFilters({})} className="text-gray-500 hover:text-gray-700 text-sm">Clear</button>}
      </div>

      {txns && txns.length > 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-auto max-h-[70vh]">
          <table className="w-full text-sm">
            <thead className="sticky top-0 z-10"><tr className="text-gray-500 text-left border-b border-gray-200 bg-gray-50">
              <SortHeader label="Trade Date" sortKey="tradeDate" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="Symbol" sortKey="symbol" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="Type" sortKey="transactionType" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="Mode" sortKey="tradeType" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} />
              <SortHeader label="Qty" sortKey="quantity" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Price" sortKey="price" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
              <SortHeader label="Total" sortKey="totalAmount" sort={sort} onSort={(k) => setSort(toggleSort(sort, k))} align="right" />
            </tr></thead>
            <tbody>
              {sortData(txns, sort, (t, k) => {
                if (k === 'tradeDate') return t.tradeDate || t.createdAt;
                if (k === 'symbol') return t.symbol || t.description || '';
                if (k === 'transactionType') return t.transactionType;
                if (k === 'tradeType') return t.tradeType || '';
                return (t as unknown as Record<string, unknown>)[k] as number ?? null;
              }).map((t) => (
                <tr key={t.id} className="border-b border-gray-100 hover:bg-gray-50">
                  <td className="py-2.5 px-3 text-gray-500">{formatDateTime(t.tradeDate || t.createdAt)}</td>
                  <td className="py-2.5 px-3 font-medium text-gray-900">{t.symbol || t.description || '—'}</td>
                  <td className="py-2.5 px-3"><Badge variant={typeBadge[t.transactionType]}>{t.transactionType}</Badge></td>
                  <td className="py-2.5 px-3">{t.tradeType === 'MIS' ? <Badge variant="purple">MIS</Badge> : t.tradeType === 'CNC' ? <Badge variant="blue">CNC</Badge> : null}</td>
                  <td className="py-2.5 px-3 text-right">{t.quantity}</td>
                  <td className="py-2.5 px-3 text-right">{formatCurrency(t.price)}</td>
                  <td className="py-2.5 px-3 text-right">{formatCurrency(t.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : <EmptyState message="No transactions yet" action={<button onClick={() => setShowAdd(true)} className="text-blue-600 text-sm">Record your first transaction</button>} />}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="Add Transaction">
        <AddTransactionForm onSuccess={() => { setShowAdd(false); refetch(); analytics.refetch(); }} />
      </Modal>
    </div>
  );
}
