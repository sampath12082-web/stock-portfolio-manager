import { useState, useEffect } from 'react';
import { Plus, RefreshCw, Search, Loader2, Globe, Database } from 'lucide-react';
import { useMfHoldings, useMfFunds, useMfTransactions, useMfSearch, useCreateMfFund, useCreateMfHolding, useCreateMfTransaction, useRefreshMfNav } from '@/hooks/useMutualFunds';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import StatCard from '@/components/ui/StatCard';
import PnLText from '@/components/ui/PnLText';
import Badge from '@/components/ui/Badge';
import Modal from '@/components/ui/Modal';
import Card from '@/components/ui/Card';
import { formatCurrency, formatPercentage, formatNumber, formatDateTime } from '@/utils/format';
import type { MfLookupResponse, MfTransactionType } from '@/api/types';

const txnBadge: Record<MfTransactionType, 'green' | 'red' | 'blue' | 'yellow' | 'purple' | 'gray'> = {
  PURCHASE: 'green', REDEMPTION: 'red', SIP: 'green', SWP: 'red', STP: 'blue',
  DIVIDEND_REINVEST: 'yellow', DIVIDEND_PAYOUT: 'yellow', SWITCH_IN: 'blue', SWITCH_OUT: 'purple',
};

export default function MutualFundsPage() {
  const { data: holdings, isLoading } = useMfHoldings();
  const { data: transactions } = useMfTransactions();
  const refreshNav = useRefreshMfNav();
  const [showAddFund, setShowAddFund] = useState(false);
  const [showAddTxn, setShowAddTxn] = useState(false);

  if (isLoading) return <LoadingSpinner />;

  const totalInvested = holdings?.reduce((s, h) => s + h.investedAmount, 0) ?? 0;
  const totalCurrent = holdings?.reduce((s, h) => s + (h.currentValue ?? h.investedAmount), 0) ?? 0;
  const totalPnL = totalCurrent - totalInvested;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-2">
        <h1 className="text-2xl font-bold text-gray-900">Mutual Funds</h1>
        <div className="flex gap-2">
          <button onClick={() => refreshNav.mutate()} disabled={refreshNav.isPending}
            className="flex items-center gap-2 px-3 py-2 bg-gray-100 hover:bg-gray-200 border border-gray-300 text-gray-700 rounded-md text-sm font-medium disabled:opacity-50">
            <RefreshCw size={16} className={refreshNav.isPending ? 'animate-spin' : ''} /> Refresh NAV
          </button>
          <button onClick={() => setShowAddFund(true)} className="flex items-center gap-2 px-3 py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium">
            <Plus size={16} /> Add Fund
          </button>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <StatCard label="Total Invested" value={formatCurrency(totalInvested)} />
        <StatCard label="Current Value" value={formatCurrency(totalCurrent)} />
        <StatCard label="P&L" value={formatCurrency(totalPnL)} change={totalInvested > 0 ? (totalPnL / totalInvested) * 100 : null} changeLabel={formatPercentage(totalInvested > 0 ? (totalPnL / totalInvested) * 100 : null)} />
      </div>

      <Card title="Holdings">
        {holdings && holdings.length > 0 ? (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-gray-500 text-left border-b border-gray-200 bg-gray-50">
                <th className="py-3 px-3">Fund</th><th className="py-3 px-3">Fund House</th>
                <th className="py-3 px-3 text-right">Units</th><th className="py-3 px-3 text-right">Avg NAV</th>
                <th className="py-3 px-3 text-right">Current NAV</th><th className="py-3 px-3 text-right">Invested</th>
                <th className="py-3 px-3 text-right">Current Value</th><th className="py-3 px-3 text-right">P&L</th>
                <th className="py-3 px-3 text-right">P&L%</th>
              </tr></thead>
              <tbody>
                {holdings.map((h) => {
                  const isLoss = h.currentNav != null && h.averageNav != null && h.currentNav < h.averageNav;
                  const isGain = h.currentNav != null && h.averageNav != null && h.currentNav > h.averageNav;
                  const rowClass = isLoss ? 'text-red-600' : isGain ? 'bg-emerald-600 text-white' : '';
                  return (
                    <tr key={h.id} className={`border-b border-gray-100 hover:opacity-80 ${rowClass}`}>
                      <td className="py-2.5 px-3 max-w-64"><span className="font-medium">{h.schemeName}</span></td>
                      <td className="py-2.5 px-3 text-xs opacity-70">{h.fundHouse}</td>
                      <td className="py-2.5 px-3 text-right">{formatNumber(h.units)}</td>
                      <td className="py-2.5 px-3 text-right">{formatCurrency(h.averageNav)}</td>
                      <td className="py-2.5 px-3 text-right font-medium">{formatCurrency(h.currentNav)}</td>
                      <td className="py-2.5 px-3 text-right">{formatCurrency(h.investedAmount)}</td>
                      <td className="py-2.5 px-3 text-right">{formatCurrency(h.currentValue)}</td>
                      <td className="py-2.5 px-3 text-right">{formatCurrency(h.pnl)}</td>
                      <td className="py-2.5 px-3 text-right">{formatPercentage(h.pnlPercentage)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : <EmptyState message="No mutual fund holdings yet" action={<button onClick={() => setShowAddFund(true)} className="text-[#D85A30] text-sm">Add your first fund</button>} />}
      </Card>

      {transactions && transactions.length > 0 && (
        <Card title="Transactions">
          <div className="flex justify-end mb-3">
            <button onClick={() => setShowAddTxn(true)} className="flex items-center gap-1 px-2 py-1 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded text-xs font-medium">
              <Plus size={12} /> Add Transaction
            </button>
          </div>
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead><tr className="text-gray-500 text-left border-b border-gray-200">
                <th className="pb-2 px-2">Date</th><th className="pb-2 px-2">Fund</th><th className="pb-2 px-2">Type</th>
                <th className="pb-2 px-2 text-right">Units</th><th className="pb-2 px-2 text-right">NAV</th><th className="pb-2 px-2 text-right">Amount</th>
              </tr></thead>
              <tbody>
                {transactions.slice(0, 20).map((t) => (
                  <tr key={t.id} className="border-b border-gray-100">
                    <td className="py-2 px-2 text-gray-500 text-xs">{formatDateTime(t.tradeDate || t.createdAt)}</td>
                    <td className="py-2 px-2 text-gray-900 text-xs max-w-48 truncate">{t.schemeName || t.schemeCode}</td>
                    <td className="py-2 px-2"><Badge variant={txnBadge[t.transactionType]}>{t.transactionType}</Badge></td>
                    <td className="py-2 px-2 text-right">{formatNumber(t.units)}</td>
                    <td className="py-2 px-2 text-right">{formatCurrency(t.nav)}</td>
                    <td className="py-2 px-2 text-right">{formatCurrency(t.amount)}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      )}

      <AddFundModal open={showAddFund} onClose={() => setShowAddFund(false)} />
      <AddMfTransactionModal open={showAddTxn} onClose={() => setShowAddTxn(false)} />
    </div>
  );
}

function AddFundModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const [query, setQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const search = useMfSearch(debouncedQuery);
  const createFund = useCreateMfFund();
  const createHolding = useCreateMfHolding();
  const [step, setStep] = useState<'search' | 'holding'>('search');
  const [selectedFund, setSelectedFund] = useState<MfLookupResponse | null>(null);

  useEffect(() => {
    const t = setTimeout(() => setDebouncedQuery(query), 400);
    return () => clearTimeout(t);
  }, [query]);

  useEffect(() => {
    if (!open) { setQuery(''); setDebouncedQuery(''); setSelectedFund(null); setStep('search'); }
  }, [open]);

  function handleSelectFund(fund: MfLookupResponse) {
    if (fund.existsInDb) {
      setSelectedFund(fund);
      setStep('holding');
    } else {
      createFund.mutate({
        schemeCode: fund.schemeCode,
        schemeName: fund.schemeName,
      }, {
        onSuccess: () => { setSelectedFund(fund); setStep('holding'); },
      });
    }
  }

  return (
    <Modal open={open} onClose={onClose} title={step === 'search' ? 'Add Mutual Fund' : `Add Holding — ${selectedFund?.schemeName?.substring(0, 40)}`}>
      {step === 'search' ? (
        <div className="space-y-4">
          <div className="relative">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" />
            <input placeholder="Search fund name or scheme code..." value={query}
              onChange={(e) => setQuery(e.target.value)}
              className="w-full bg-gray-50 border border-gray-300 rounded-md pl-9 pr-9 py-2 text-sm" />
            {search.isFetching && <Loader2 size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 animate-spin" />}
          </div>
          {search.data && search.data.length > 0 && (
            <div className="max-h-60 overflow-y-auto space-y-1">
              {search.data.map((f, i) => (
                <button key={`${f.schemeCode}-${i}`} onClick={() => handleSelectFund(f)}
                  className="w-full text-left px-3 py-2 hover:bg-gray-50 rounded border border-gray-100 text-sm">
                  <div className="flex items-center gap-2">
                    <span className="font-medium text-gray-900">{f.schemeName.substring(0, 60)}</span>
                    {f.existsInDb ? <span className="text-xs text-emerald-600 flex items-center gap-1"><Database size={10} />In DB</span>
                      : <span className="text-xs text-[#D85A30] flex items-center gap-1"><Globe size={10} />AMFI</span>}
                  </div>
                  <div className="text-xs text-gray-500">Code: {f.schemeCode} | NAV: {formatCurrency(f.nav)} ({f.navDate})</div>
                </button>
              ))}
            </div>
          )}
          {debouncedQuery.length >= 3 && !search.isFetching && search.data?.length === 0 && (
            <p className="text-sm text-gray-400 text-center py-4">No funds found</p>
          )}
        </div>
      ) : (
        <form onSubmit={(e) => {
          e.preventDefault();
          const fd = new FormData(e.currentTarget);
          createHolding.mutate({
            schemeCode: selectedFund!.schemeCode,
            units: Number(fd.get('units')),
            averageNav: Number(fd.get('averageNav')),
          }, { onSuccess: onClose });
        }} className="space-y-4">
          <div><label className="block text-sm text-gray-500 mb-1">Units</label>
            <input name="units" type="number" step="0.0001" min="0.0001" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-gray-500 mb-1">Average NAV (buy price per unit)</label>
            <input name="averageNav" type="number" step="0.0001" min="0.0001" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <button type="submit" disabled={createHolding.isPending}
            className="w-full py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
            {createHolding.isPending ? 'Adding...' : 'Add Holding'}</button>
        </form>
      )}
    </Modal>
  );
}

function AddMfTransactionModal({ open, onClose }: { open: boolean; onClose: () => void }) {
  const { data: funds } = useMfFunds();
  const createTxn = useCreateMfTransaction();

  return (
    <Modal open={open} onClose={onClose} title="Add MF Transaction">
      <form onSubmit={(e) => {
        e.preventDefault();
        const fd = new FormData(e.currentTarget);
        createTxn.mutate({
          schemeCode: fd.get('schemeCode') as string,
          units: Number(fd.get('units')),
          nav: Number(fd.get('nav')),
          amount: Number(fd.get('amount')),
          transactionType: fd.get('transactionType') as MfTransactionType,
          description: (fd.get('description') as string) || undefined,
          folioNumber: (fd.get('folioNumber') as string) || undefined,
        }, { onSuccess: onClose });
      }} className="space-y-4">
        <div><label className="block text-sm text-gray-500 mb-1">Fund</label>
          <select name="schemeCode" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
            <option value="">Select fund...</option>
            {funds?.map((f) => <option key={f.id} value={f.schemeCode}>{f.schemeName.substring(0, 50)}</option>)}
          </select></div>
        <div><label className="block text-sm text-gray-500 mb-1">Type</label>
          <select name="transactionType" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
            {['PURCHASE', 'REDEMPTION', 'SIP', 'SWP', 'STP', 'DIVIDEND_REINVEST', 'DIVIDEND_PAYOUT'].map((t) => <option key={t} value={t}>{t}</option>)}
          </select></div>
        <div className="grid grid-cols-3 gap-2">
          <div><label className="block text-sm text-gray-500 mb-1">Units</label><input name="units" type="number" step="0.0001" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-gray-500 mb-1">NAV</label><input name="nav" type="number" step="0.0001" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-gray-500 mb-1">Amount</label><input name="amount" type="number" step="0.01" required className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
        </div>
        <div><label className="block text-sm text-gray-500 mb-1">Folio Number</label><input name="folioNumber" className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" /></div>
        <button type="submit" disabled={createTxn.isPending} className="w-full py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
          {createTxn.isPending ? 'Recording...' : 'Record Transaction'}</button>
      </form>
    </Modal>
  );
}
