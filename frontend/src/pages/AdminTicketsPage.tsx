import { useState, useEffect } from 'react';
import { MessageSquare, Send } from 'lucide-react';
import client from '@/api/client';
import { useAuth } from '@/auth/AuthContext';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

interface Ticket {
  id: number;
  userEmail: string;
  userName: string;
  subject: string;
  message: string;
  status: string;
  adminResponse: string;
  createdAt: string;
}

export default function AdminTicketsPage() {
  const { isAdmin } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [responding, setResponding] = useState<number | null>(null);
  const [response, setResponse] = useState('');
  const [status, setStatus] = useState('RESOLVED');

  useEffect(() => { fetchTickets(); }, []);

  async function fetchTickets() {
    setLoading(true);
    try {
      const resp = await client.get('/admin/tickets');
      setTickets(resp.data);
    } catch { /* */ }
    finally { setLoading(false); }
  }

  async function handleRespond(id: number) {
    try {
      await client.put(`/admin/tickets/${id}`, { adminResponse: response, status });
      setResponding(null);
      setResponse('');
      fetchTickets();
    } catch { /* */ }
  }

  if (!isAdmin) return <div className="p-8 text-center text-gray-500">Access denied</div>;
  if (loading) return <LoadingSpinner />;

  const statusBadge: Record<string, 'yellow' | 'blue' | 'green' | 'gray'> = {
    OPEN: 'yellow', IN_PROGRESS: 'blue', RESOLVED: 'green', CLOSED: 'gray',
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <MessageSquare size={24} className="text-[#D85A30]" />
        <h1 className="text-2xl font-bold text-gray-900">Admin — Support Tickets</h1>
        <span className="text-sm text-gray-400">{tickets.length} total</span>
      </div>

      {tickets.length === 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg p-8 text-center text-gray-400">No tickets yet</div>
      ) : (
        <div className="space-y-3">
          {tickets.map(t => (
            <div key={t.id} className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
              <div className="flex items-center justify-between mb-2">
                <div>
                  <span className="font-medium text-gray-900">{t.subject}</span>
                  <span className="text-xs text-gray-400 ml-2">#{t.id}</span>
                </div>
                <Badge variant={statusBadge[t.status] || 'gray'}>{t.status}</Badge>
              </div>
              <p className="text-sm text-gray-600 mb-2">{t.message}</p>
              <div className="flex items-center gap-4 text-xs text-gray-400">
                <span>From: {t.userName} ({t.userEmail})</span>
                <span>{new Date(t.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
              </div>

              {t.adminResponse && (
                <div className="mt-3 p-3 bg-[#FAECE7] border border-[#D85A30]/20 rounded text-sm text-[#712B13]">
                  <span className="font-medium">Response: </span>{t.adminResponse}
                </div>
              )}

              {responding === t.id ? (
                <div className="mt-3 space-y-2">
                  <textarea value={response} onChange={e => setResponse(e.target.value)} rows={3} placeholder="Type your response..."
                    className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm resize-none" />
                  <div className="flex items-center gap-2">
                    <select value={status} onChange={e => setStatus(e.target.value)} className="bg-gray-50 border border-gray-300 rounded-md px-2 py-1 text-sm">
                      <option value="IN_PROGRESS">In Progress</option>
                      <option value="RESOLVED">Resolved</option>
                      <option value="CLOSED">Closed</option>
                    </select>
                    <button onClick={() => handleRespond(t.id)} className="flex items-center gap-1 px-3 py-1 bg-[#D85A30] text-white rounded text-sm"><Send size={12} /> Send</button>
                    <button onClick={() => setResponding(null)} className="px-3 py-1 text-gray-500 text-sm">Cancel</button>
                  </div>
                </div>
              ) : (
                <button onClick={() => { setResponding(t.id); setResponse(t.adminResponse || ''); }}
                  className="mt-2 text-xs text-[#D85A30] hover:underline">
                  {t.adminResponse ? 'Edit Response' : 'Respond'}
                </button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
