import { useState } from 'react';
import { Plus, Trash2, Edit } from 'lucide-react';
import { useTodaySignals, useActiveSignals, useCreateSignal, useUpdateSignal, useDeleteSignal } from '@/hooks/useSignals';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import EmptyState from '@/components/ui/EmptyState';
import Badge from '@/components/ui/Badge';
import Modal from '@/components/ui/Modal';
import { formatCurrency, formatDate } from '@/utils/format';
import type { TradingSignalResponse, SignalType, SignalStatus } from '@/api/types';

const signalBadge: Record<SignalType, 'green' | 'red' | 'yellow' | 'blue'> = {
  BUY_SIGNAL: 'green', SELL_SIGNAL: 'red', HOLD: 'yellow', WATCH: 'blue',
};
const statusBadge: Record<SignalStatus, 'green' | 'blue' | 'gray' | 'red'> = {
  ACTIVE: 'green', EXECUTED: 'blue', EXPIRED: 'gray', CANCELLED: 'red',
};

export default function SignalsPage() {
  const [tab, setTab] = useState<'today' | 'active'>('today');
  const todayQ = useTodaySignals();
  const activeQ = useActiveSignals();
  const createMut = useCreateSignal();
  const updateMut = useUpdateSignal();
  const deleteMut = useDeleteSignal();
  const [showAdd, setShowAdd] = useState(false);
  const [editing, setEditing] = useState<TradingSignalResponse | null>(null);

  const signals = tab === 'today' ? todayQ.data : activeQ.data;
  const isLoading = tab === 'today' ? todayQ.isLoading : activeQ.isLoading;

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Trading Signals</h1>
        <button onClick={() => setShowAdd(true)} className="flex items-center gap-2 px-3 py-2 bg-blue-600 hover:bg-blue-500 rounded-md text-sm font-medium">
          <Plus size={16} /> New Signal
        </button>
      </div>

      <div className="flex gap-2">
        {(['today', 'active'] as const).map((t) => (
          <button key={t} onClick={() => setTab(t)} className={`px-4 py-1.5 rounded-md text-sm font-medium ${tab === t ? 'bg-blue-600 text-white' : 'bg-slate-800 text-slate-400 hover:text-white'}`}>
            {t === 'today' ? 'Today' : 'Active'}
          </button>
        ))}
      </div>

      {isLoading ? <LoadingSpinner /> : signals && signals.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {signals.map((s) => (
            <div key={s.id} className="bg-slate-900 border border-slate-700 rounded-lg p-4">
              <div className="flex items-center justify-between mb-2">
                <div className="flex items-center gap-2">
                  <span className="font-bold text-lg">{s.symbol}</span>
                  {s.companyName && <span className="text-sm text-slate-400">{s.companyName}</span>}
                </div>
                <div className="flex gap-1">
                  <button onClick={() => setEditing(s)} className="text-slate-400 hover:text-blue-400 p-1"><Edit size={14} /></button>
                  <button onClick={() => { if (confirm('Cancel this signal?')) deleteMut.mutate(s.id); }} className="text-slate-400 hover:text-red-400 p-1"><Trash2 size={14} /></button>
                </div>
              </div>
              <div className="flex gap-2 mb-3">
                <Badge variant={signalBadge[s.signalType]}>{s.signalType.replace('_', ' ')}</Badge>
                <Badge variant={statusBadge[s.status]}>{s.status}</Badge>
              </div>
              <div className="grid grid-cols-3 gap-2 text-sm mb-3">
                {s.targetPrice != null && <div className="bg-slate-800 rounded p-2"><span className="text-slate-400 text-xs">Target</span><br />{formatCurrency(s.targetPrice)}</div>}
                {s.stopLoss != null && <div className="bg-slate-800 rounded p-2"><span className="text-slate-400 text-xs">Stop Loss</span><br />{formatCurrency(s.stopLoss)}</div>}
                {s.currentPrice != null && <div className="bg-slate-800 rounded p-2"><span className="text-slate-400 text-xs">Current</span><br />{formatCurrency(s.currentPrice)}</div>}
              </div>
              {s.rationale && <p className="text-sm text-slate-300 mb-2">{s.rationale}</p>}
              <p className="text-xs text-slate-500">{formatDate(s.signalDate)}</p>
            </div>
          ))}
        </div>
      ) : (
        <EmptyState message={tab === 'today' ? 'No signals for today' : 'No active signals'} action={<button onClick={() => setShowAdd(true)} className="text-blue-400 text-sm">Create a signal</button>} />
      )}

      <Modal open={showAdd} onClose={() => setShowAdd(false)} title="New Signal">
        <form onSubmit={(e) => {
          e.preventDefault();
          const fd = new FormData(e.currentTarget);
          createMut.mutate({
            symbol: fd.get('symbol') as string,
            signalType: fd.get('signalType') as SignalType,
            targetPrice: fd.get('targetPrice') ? Number(fd.get('targetPrice')) : undefined,
            stopLoss: fd.get('stopLoss') ? Number(fd.get('stopLoss')) : undefined,
            rationale: (fd.get('rationale') as string) || undefined,
            notes: (fd.get('notes') as string) || undefined,
          }, { onSuccess: () => setShowAdd(false) });
        }} className="space-y-4">
          <div><label className="block text-sm text-slate-400 mb-1">Symbol</label><input name="symbol" required className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-slate-400 mb-1">Signal Type</label><select name="signalType" required className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm"><option value="BUY_SIGNAL">BUY SIGNAL</option><option value="SELL_SIGNAL">SELL SIGNAL</option><option value="HOLD">HOLD</option><option value="WATCH">WATCH</option></select></div>
          <div><label className="block text-sm text-slate-400 mb-1">Target Price</label><input name="targetPrice" type="number" step="0.01" className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-slate-400 mb-1">Stop Loss</label><input name="stopLoss" type="number" step="0.01" className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
          <div><label className="block text-sm text-slate-400 mb-1">Rationale</label><textarea name="rationale" rows={2} className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
          <button type="submit" disabled={createMut.isPending} className="w-full py-2 bg-blue-600 hover:bg-blue-500 rounded-md text-sm font-medium disabled:opacity-50">{createMut.isPending ? 'Creating...' : 'Create Signal'}</button>
        </form>
      </Modal>

      <Modal open={!!editing} onClose={() => setEditing(null)} title={`Edit ${editing?.symbol} Signal`}>
        {editing && (
          <form onSubmit={(e) => {
            e.preventDefault();
            const fd = new FormData(e.currentTarget);
            updateMut.mutate({
              id: editing.id,
              data: {
                status: fd.get('status') as SignalStatus,
                targetPrice: fd.get('targetPrice') ? Number(fd.get('targetPrice')) : undefined,
                stopLoss: fd.get('stopLoss') ? Number(fd.get('stopLoss')) : undefined,
                rationale: (fd.get('rationale') as string) || undefined,
              },
            }, { onSuccess: () => setEditing(null) });
          }} className="space-y-4">
            <div><label className="block text-sm text-slate-400 mb-1">Status</label><select name="status" defaultValue={editing.status} className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm"><option value="ACTIVE">ACTIVE</option><option value="EXECUTED">EXECUTED</option><option value="EXPIRED">EXPIRED</option><option value="CANCELLED">CANCELLED</option></select></div>
            <div><label className="block text-sm text-slate-400 mb-1">Target Price</label><input name="targetPrice" type="number" step="0.01" defaultValue={editing.targetPrice ?? ''} className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-slate-400 mb-1">Stop Loss</label><input name="stopLoss" type="number" step="0.01" defaultValue={editing.stopLoss ?? ''} className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
            <div><label className="block text-sm text-slate-400 mb-1">Rationale</label><textarea name="rationale" rows={2} defaultValue={editing.rationale ?? ''} className="w-full bg-slate-700 border border-slate-600 rounded-md px-3 py-2 text-sm" /></div>
            <button type="submit" disabled={updateMut.isPending} className="w-full py-2 bg-blue-600 hover:bg-blue-500 rounded-md text-sm font-medium disabled:opacity-50">{updateMut.isPending ? 'Saving...' : 'Save Changes'}</button>
          </form>
        )}
      </Modal>
    </div>
  );
}
