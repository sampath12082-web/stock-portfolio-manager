import { useState, useEffect } from 'react';
import { MessageSquare, Send, Bug, Sparkles, CheckCircle, XCircle, Play, RefreshCw, Lightbulb, ThumbsUp } from 'lucide-react';
import client from '@/api/client';
import { useAuth } from '@/auth/AuthContext';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

interface BugReportData {
  id: number; title: string; severity: string; status: string;
  testPassed: boolean | null; testResult: string | null;
  estimatedFixHours: number | null; estimatedFixDescription: string | null;
}

interface Ticket {
  id: number; userEmail: string; userName: string; subject: string; message: string;
  status: string; ticketType: string; priority: string;
  adminResponse: string; aiResponse: string;
  bugReport?: BugReportData; createdAt: string;
}

const statusBadge: Record<string, 'yellow' | 'blue' | 'green' | 'gray' | 'red'> = {
  OPEN: 'yellow', AI_REVIEWED: 'blue', BUG_CONFIRMED: 'red', BUG_NOT_CONFIRMED: 'gray',
  IN_PROGRESS: 'blue', APPROVED: 'blue', IN_DEVELOPMENT: 'yellow', RESOLVED: 'green', CLOSED: 'gray',
};

const typeBadge: Record<string, { variant: 'red' | 'blue' | 'green' | 'purple'; icon: typeof Bug; label: string }> = {
  BUG_REPORT: { variant: 'red', icon: Bug, label: 'Bug' },
  INQUIRY: { variant: 'blue', icon: MessageSquare, label: 'Inquiry' },
  FEATURE_REQUEST: { variant: 'purple', icon: Lightbulb, label: 'Feature' },
  FEEDBACK: { variant: 'green', icon: ThumbsUp, label: 'Feedback' },
};

const priorityColor: Record<string, string> = {
  CRITICAL: 'text-red-700 bg-red-50', HIGH: 'text-orange-700 bg-orange-50',
  MEDIUM: 'text-yellow-700 bg-yellow-50', LOW: 'text-gray-600 bg-gray-100',
};

const bugStatusBadge: Record<string, 'yellow' | 'red' | 'blue' | 'green' | 'gray'> = {
  PENDING_VERIFICATION: 'yellow', VERIFIED: 'red', NOT_REPRODUCIBLE: 'gray',
  APPROVED: 'blue', IN_DEVELOPMENT: 'yellow', FIXED: 'green', WONT_FIX: 'gray',
};

type FilterTab = 'ALL' | 'BUG_REPORT' | 'INQUIRY' | 'FEATURE_REQUEST' | 'PENDING';

export default function AdminTicketsPage() {
  const { isAdmin } = useAuth();
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [responding, setResponding] = useState<number | null>(null);
  const [response, setResponse] = useState('');
  const [status, setStatus] = useState('RESOLVED');
  const [filter, setFilter] = useState<FilterTab>('ALL');
  const [approveNotes, setApproveNotes] = useState('');
  const [approvePriority, setApprovePriority] = useState('MEDIUM');

  useEffect(() => { fetchTickets(); }, []);

  async function fetchTickets() {
    setLoading(true);
    try { setTickets((await client.get('/admin/tickets')).data); }
    catch { /* */ }
    finally { setLoading(false); }
  }

  async function handleRespond(id: number) {
    try {
      await client.put(`/admin/tickets/${id}`, { adminResponse: response, status });
      setResponding(null); setResponse(''); fetchTickets();
    } catch { /* */ }
  }

  async function handleBugAction(bugId: number, action: string, body?: Record<string, string>) {
    try {
      if (action === 'rerun-tests') await client.post(`/admin/bugs/${bugId}/rerun-tests`);
      else await client.put(`/admin/bugs/${bugId}/${action}`, body || {});
      fetchTickets();
    } catch { /* */ }
  }

  if (!isAdmin) return <div className="p-8 text-center text-gray-500">Access denied</div>;
  if (loading) return <LoadingSpinner />;

  const filtered = tickets.filter(t => {
    if (filter === 'ALL') return true;
    if (filter === 'PENDING') return ['OPEN', 'AI_REVIEWED', 'BUG_CONFIRMED'].includes(t.status);
    return t.ticketType === filter;
  });

  const counts = { ALL: tickets.length, BUG_REPORT: 0, INQUIRY: 0, FEATURE_REQUEST: 0, PENDING: 0 };
  tickets.forEach(t => {
    if (t.ticketType === 'BUG_REPORT') counts.BUG_REPORT++;
    else if (t.ticketType === 'INQUIRY') counts.INQUIRY++;
    else if (t.ticketType === 'FEATURE_REQUEST') counts.FEATURE_REQUEST++;
    if (['OPEN', 'AI_REVIEWED', 'BUG_CONFIRMED'].includes(t.status)) counts.PENDING++;
  });

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <MessageSquare size={24} className="text-[#D85A30]" />
        <h1 className="text-2xl font-bold text-gray-900">Admin — Support Tickets</h1>
        <span className="text-sm text-gray-400">{tickets.length} total</span>
      </div>

      <div className="flex gap-2 flex-wrap">
        {([['ALL', counts.ALL], ['PENDING', counts.PENDING], ['BUG_REPORT', counts.BUG_REPORT], ['INQUIRY', counts.INQUIRY], ['FEATURE_REQUEST', counts.FEATURE_REQUEST]] as const).map(([key, count]) => (
          <button key={key} onClick={() => setFilter(key as FilterTab)}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors ${filter === key ? 'bg-[#D85A30] text-white' : 'bg-gray-100 text-gray-600 hover:bg-gray-200'}`}>
            {key === 'BUG_REPORT' ? 'Bugs' : key === 'FEATURE_REQUEST' ? 'Features' : key.charAt(0) + key.slice(1).toLowerCase()} ({count})
          </button>
        ))}
      </div>

      {filtered.length === 0 ? (
        <div className="bg-white border border-gray-200 rounded-lg p-8 text-center text-gray-400">No tickets</div>
      ) : (
        <div className="space-y-3">
          {filtered.map(t => {
            const tp = typeBadge[t.ticketType] || typeBadge.INQUIRY;
            const TypeIcon = tp.icon;
            return (
              <div key={t.id} className="bg-white border border-gray-200 rounded-lg shadow-sm p-4">
                <div className="flex items-center justify-between mb-2 gap-2">
                  <div className="flex items-center gap-2 flex-1 min-w-0">
                    <span className="font-medium text-gray-900 truncate">{t.subject}</span>
                    <span className="text-xs text-gray-400">#{t.id}</span>
                  </div>
                  <div className="flex items-center gap-1.5 shrink-0">
                    <span className={`text-[10px] font-medium px-1.5 py-0.5 rounded ${priorityColor[t.priority] || priorityColor.MEDIUM}`}>{t.priority}</span>
                    <Badge variant={tp.variant}><TypeIcon size={10} className="mr-0.5" />{tp.label}</Badge>
                    <Badge variant={statusBadge[t.status] || 'gray'}>{t.status.replace(/_/g, ' ')}</Badge>
                  </div>
                </div>
                <p className="text-sm text-gray-600 mb-2">{t.message}</p>
                <div className="flex items-center gap-4 text-xs text-gray-400">
                  <span>{t.userName} ({t.userEmail})</span>
                  <span>{new Date(t.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}</span>
                </div>

                {t.aiResponse && (
                  <div className="mt-3 p-2.5 bg-purple-50 border border-purple-200 rounded text-sm text-purple-800">
                    <div className="flex items-center gap-1 mb-1 font-semibold text-xs"><Sparkles size={10} /> AI Agent</div>
                    <p className="text-xs leading-relaxed">{t.aiResponse}</p>
                  </div>
                )}

                {t.bugReport && (
                  <div className="mt-3 p-3 bg-red-50 border border-red-200 rounded">
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-xs font-semibold text-red-700 flex items-center gap-1"><Bug size={12} /> Bug #{t.bugReport.id}</span>
                      <Badge variant={bugStatusBadge[t.bugReport.status] || 'gray'}>{t.bugReport.status.replace(/_/g, ' ')}</Badge>
                    </div>
                    {t.bugReport.testResult && (
                      <p className="text-xs text-gray-600 mb-2">
                        Tests: <span className={t.bugReport.testPassed ? 'text-emerald-600 font-medium' : 'text-red-600 font-medium'}>{t.bugReport.testPassed ? 'PASSING' : 'FAILING'}</span> — {t.bugReport.testResult}
                      </p>
                    )}
                    {t.bugReport.estimatedFixDescription && (
                      <p className="text-xs text-gray-500 mb-2">Estimate: {t.bugReport.estimatedFixDescription}</p>
                    )}
                    <div className="flex gap-2 flex-wrap">
                      {t.bugReport.status === 'VERIFIED' && (
                        <>
                          <button onClick={() => handleBugAction(t.bugReport!.id, 'approve', { adminNotes: approveNotes || 'Approved', priority: approvePriority })}
                            className="flex items-center gap-1 px-2 py-1 bg-emerald-600 text-white rounded text-xs"><CheckCircle size={10} /> Approve</button>
                          <button onClick={() => handleBugAction(t.bugReport!.id, 'reject', { adminNotes: 'Rejected by admin' })}
                            className="flex items-center gap-1 px-2 py-1 bg-gray-500 text-white rounded text-xs"><XCircle size={10} /> Reject</button>
                          <select value={approvePriority} onChange={e => setApprovePriority(e.target.value)} className="text-xs border border-gray-300 rounded px-1 py-0.5">
                            <option value="CRITICAL">Critical</option><option value="HIGH">High</option><option value="MEDIUM">Medium</option><option value="LOW">Low</option>
                          </select>
                        </>
                      )}
                      {t.bugReport.status === 'APPROVED' && (
                        <button onClick={() => handleBugAction(t.bugReport!.id, 'start-development')}
                          className="flex items-center gap-1 px-2 py-1 bg-yellow-600 text-white rounded text-xs"><Play size={10} /> Start Dev</button>
                      )}
                      {t.bugReport.status === 'IN_DEVELOPMENT' && (
                        <button onClick={() => handleBugAction(t.bugReport!.id, 'mark-fixed', { resolution: 'Bug fixed in latest release' })}
                          className="flex items-center gap-1 px-2 py-1 bg-emerald-600 text-white rounded text-xs"><CheckCircle size={10} /> Mark Fixed</button>
                      )}
                      {['VERIFIED', 'APPROVED', 'IN_DEVELOPMENT', 'NOT_REPRODUCIBLE'].includes(t.bugReport.status) && (
                        <button onClick={() => handleBugAction(t.bugReport!.id, 'rerun-tests')}
                          className="flex items-center gap-1 px-2 py-1 bg-gray-200 text-gray-700 rounded text-xs"><RefreshCw size={10} /> Re-run Tests</button>
                      )}
                    </div>
                  </div>
                )}

                {t.adminResponse && (
                  <div className="mt-3 p-2.5 bg-[#FAECE7] border border-[#D85A30]/20 rounded text-sm text-[#712B13]">
                    <span className="font-semibold text-xs">Admin: </span><span className="text-xs">{t.adminResponse}</span>
                  </div>
                )}

                {responding === t.id ? (
                  <div className="mt-3 space-y-2">
                    <textarea value={response} onChange={e => setResponse(e.target.value)} rows={3} placeholder="Type your response..."
                      className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm resize-none" />
                    <div className="flex items-center gap-2">
                      <select value={status} onChange={e => setStatus(e.target.value)} className="bg-gray-50 border border-gray-300 rounded-md px-2 py-1 text-sm">
                        <option value="AI_REVIEWED">AI Reviewed</option><option value="IN_PROGRESS">In Progress</option>
                        <option value="RESOLVED">Resolved</option><option value="CLOSED">Closed</option>
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
            );
          })}
        </div>
      )}
    </div>
  );
}
