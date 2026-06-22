import { useState, useEffect, useRef } from 'react';
import { ChevronDown, ChevronRight, Send, Clock, CheckCircle, Sparkles, Bug, Lightbulb, MessageSquare, ThumbsUp } from 'lucide-react';
import client from '@/api/client';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

interface FaqItem { id: number; category: string; question: string; answer: string; }

interface BugReportData {
  id: number; title: string; severity: string; status: string;
  testPassed: boolean | null; testResult: string | null;
  estimatedFixHours: number | null; estimatedFixDescription: string | null;
}

interface Ticket {
  id: number; subject: string; message: string; status: string;
  ticketType: string; priority: string;
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

const bugStatusBadge: Record<string, 'yellow' | 'red' | 'blue' | 'green' | 'gray'> = {
  PENDING_VERIFICATION: 'yellow', VERIFIED: 'red', NOT_REPRODUCIBLE: 'gray',
  APPROVED: 'blue', IN_DEVELOPMENT: 'yellow', FIXED: 'green', WONT_FIX: 'gray',
};

export default function HelpPage() {
  const [faqs, setFaqs] = useState<FaqItem[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);
  const [subject, setSubject] = useState('');
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [msg, setMsg] = useState('');
  const pollRef = useRef<ReturnType<typeof setInterval> | null>(null);

  function loadTickets() {
    client.get('/help/tickets').then(r => setTickets(r.data)).catch(() => {});
  }

  useEffect(() => {
    Promise.all([
      client.get('/help/faq').then(r => setFaqs(r.data)),
      client.get('/help/tickets').then(r => setTickets(r.data)),
    ]).finally(() => setLoading(false));
  }, []);

  useEffect(() => {
    const hasOpen = tickets.some(t => t.status === 'OPEN');
    if (hasOpen && !pollRef.current) {
      pollRef.current = setInterval(loadTickets, 8000);
    } else if (!hasOpen && pollRef.current) {
      clearInterval(pollRef.current);
      pollRef.current = null;
    }
    return () => { if (pollRef.current) clearInterval(pollRef.current); };
  }, [tickets]);

  async function handleSubmitTicket(e: React.FormEvent) {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) return;
    setSubmitting(true);
    try {
      await client.post('/help/tickets', { subject, message });
      setMsg('Request submitted — AI agent is reviewing...');
      setSubject('');
      setMessage('');
      loadTickets();
    } catch { setMsg('Failed to submit request'); }
    finally { setSubmitting(false); }
    setTimeout(() => setMsg(''), 5000);
  }

  if (loading) return <LoadingSpinner />;

  const categories = [...new Set(faqs.map(f => f.category))];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Help & Support</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Frequently Asked Questions</h3>
          {categories.length > 0 ? (
            <div className="space-y-4">
              {categories.map(cat => (
                <div key={cat}>
                  <h4 className="text-xs font-semibold text-[#D85A30] uppercase mb-2">{cat}</h4>
                  <div className="space-y-1">
                    {faqs.filter(f => f.category === cat).map(faq => (
                      <div key={faq.id} className="border border-gray-100 rounded-md">
                        <button onClick={() => setExpandedFaq(expandedFaq === faq.id ? null : faq.id)}
                          className="w-full flex items-center gap-2 px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-50">
                          {expandedFaq === faq.id ? <ChevronDown size={14} className="text-gray-400 shrink-0" /> : <ChevronRight size={14} className="text-gray-400 shrink-0" />}
                          <span className="font-medium">{faq.question}</span>
                        </button>
                        {expandedFaq === faq.id && <div className="px-9 pb-3 text-sm text-gray-600 leading-relaxed">{faq.answer}</div>}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : <p className="text-sm text-gray-400 text-center py-6">No FAQs available</p>}
        </div>

        <div className="space-y-6">
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Submit a Request</h3>
            {msg && <div className={`p-2 mb-3 rounded text-sm ${msg.includes('Failed') ? 'bg-red-50 border border-red-200 text-red-700' : 'bg-emerald-50 border border-emerald-200 text-emerald-700'}`}>{msg}</div>}
            <form onSubmit={handleSubmitTicket} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Subject</label>
                <input value={subject} onChange={e => setSubject(e.target.value)} required placeholder="Brief description of your issue or question"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Message</label>
                <textarea value={message} onChange={e => setMessage(e.target.value)} required rows={4} placeholder="Describe your issue, bug, or question in detail..."
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm resize-none focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <button type="submit" disabled={submitting}
                className="flex items-center gap-2 px-4 py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                <Send size={14} /> {submitting ? 'Submitting...' : 'Submit Request'}
              </button>
            </form>
          </div>

          {tickets.length > 0 && (
            <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">My Requests ({tickets.length})</h3>
              <div className="space-y-3">
                {tickets.map(t => {
                  const tp = typeBadge[t.ticketType] || typeBadge.INQUIRY;
                  const TypeIcon = tp.icon;
                  return (
                    <div key={t.id} className="border border-gray-100 rounded-md p-3">
                      <div className="flex items-center justify-between mb-1 gap-2">
                        <span className="text-sm font-medium text-gray-900 flex-1">{t.subject}</span>
                        <div className="flex items-center gap-1.5 shrink-0">
                          <Badge variant={tp.variant}><TypeIcon size={10} className="mr-0.5" />{tp.label}</Badge>
                          <Badge variant={statusBadge[t.status] || 'gray'}>{t.status.replace(/_/g, ' ')}</Badge>
                        </div>
                      </div>
                      <p className="text-xs text-gray-500 mb-1">{t.message}</p>
                      <div className="flex items-center gap-1 text-xs text-gray-400">
                        <Clock size={10} /> {new Date(t.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                      </div>

                      {t.aiResponse && (
                        <div className="mt-2 p-2.5 bg-purple-50 border border-purple-200 rounded text-xs text-purple-800">
                          <div className="flex items-center gap-1 mb-1 font-semibold"><Sparkles size={10} /> AI Agent</div>
                          <p className="leading-relaxed">{t.aiResponse}</p>
                        </div>
                      )}

                      {t.bugReport && (
                        <div className="mt-2 p-2.5 bg-red-50 border border-red-200 rounded text-xs">
                          <div className="flex items-center justify-between mb-1">
                            <span className="font-semibold text-red-700 flex items-center gap-1"><Bug size={10} /> Bug #{t.bugReport.id}</span>
                            <Badge variant={bugStatusBadge[t.bugReport.status] || 'gray'}>{t.bugReport.status.replace(/_/g, ' ')}</Badge>
                          </div>
                          <div className="flex flex-wrap gap-2 text-gray-600">
                            {t.bugReport.testPassed != null && (
                              <span className={t.bugReport.testPassed ? 'text-emerald-600' : 'text-red-600'}>
                                Tests: {t.bugReport.testPassed ? 'Passing' : 'Failing'}
                              </span>
                            )}
                            {t.bugReport.testResult && <span>({t.bugReport.testResult})</span>}
                            {t.bugReport.estimatedFixDescription && (
                              <span className="text-gray-500">Est: {t.bugReport.estimatedFixDescription}</span>
                            )}
                          </div>
                        </div>
                      )}

                      {t.adminResponse && (
                        <div className="mt-2 p-2.5 bg-[#FAECE7] border border-[#D85A30]/20 rounded text-xs text-[#712B13]">
                          <div className="flex items-center gap-1 mb-1 font-semibold"><CheckCircle size={10} /> Admin Response</div>
                          <p className="leading-relaxed">{t.adminResponse}</p>
                        </div>
                      )}

                      {t.status === 'OPEN' && (
                        <div className="mt-2 flex items-center gap-2 text-xs text-gray-400">
                          <div className="flex gap-1">
                            <span className="w-1.5 h-1.5 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                            <span className="w-1.5 h-1.5 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                            <span className="w-1.5 h-1.5 bg-purple-400 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
                          </div>
                          AI agent is reviewing...
                        </div>
                      )}
                    </div>
                  );
                })}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
