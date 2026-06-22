import { useState, useEffect } from 'react';
import { ChevronDown, ChevronRight, Send, MessageSquare, Clock, CheckCircle } from 'lucide-react';
import client from '@/api/client';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';

interface FaqItem {
  id: number;
  category: string;
  question: string;
  answer: string;
}

interface Ticket {
  id: number;
  subject: string;
  message: string;
  status: string;
  adminResponse: string;
  createdAt: string;
}

export default function HelpPage() {
  const [faqs, setFaqs] = useState<FaqItem[]>([]);
  const [tickets, setTickets] = useState<Ticket[]>([]);
  const [loading, setLoading] = useState(true);
  const [expandedFaq, setExpandedFaq] = useState<number | null>(null);
  const [subject, setSubject] = useState('');
  const [message, setMessage] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [msg, setMsg] = useState('');

  useEffect(() => {
    Promise.all([
      client.get('/help/faq').then(r => setFaqs(r.data)),
      client.get('/help/tickets').then(r => setTickets(r.data)),
    ]).finally(() => setLoading(false));
  }, []);

  async function handleSubmitTicket(e: React.FormEvent) {
    e.preventDefault();
    if (!subject.trim() || !message.trim()) return;
    setSubmitting(true);
    try {
      await client.post('/help/tickets', { subject, message });
      setMsg('Request submitted successfully!');
      setSubject('');
      setMessage('');
      const resp = await client.get('/help/tickets');
      setTickets(resp.data);
    } catch { setMsg('Failed to submit request'); }
    finally { setSubmitting(false); }
    setTimeout(() => setMsg(''), 4000);
  }

  if (loading) return <LoadingSpinner />;

  const categories = [...new Set(faqs.map(f => f.category))];

  const statusBadge: Record<string, 'yellow' | 'blue' | 'green' | 'gray'> = {
    OPEN: 'yellow', IN_PROGRESS: 'blue', RESOLVED: 'green', CLOSED: 'gray',
  };

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
                        <button
                          onClick={() => setExpandedFaq(expandedFaq === faq.id ? null : faq.id)}
                          className="w-full flex items-center gap-2 px-3 py-2 text-left text-sm text-gray-700 hover:bg-gray-50"
                        >
                          {expandedFaq === faq.id ? <ChevronDown size={14} className="text-gray-400 shrink-0" /> : <ChevronRight size={14} className="text-gray-400 shrink-0" />}
                          <span className="font-medium">{faq.question}</span>
                        </button>
                        {expandedFaq === faq.id && (
                          <div className="px-9 pb-3 text-sm text-gray-600 leading-relaxed">{faq.answer}</div>
                        )}
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-sm text-gray-400 text-center py-6">No FAQs available</p>
          )}
        </div>

        <div className="space-y-6">
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Submit a Request</h3>
            {msg && <div className="p-2 mb-3 bg-emerald-50 border border-emerald-200 rounded text-sm text-emerald-700">{msg}</div>}
            <form onSubmit={handleSubmitTicket} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Subject</label>
                <input value={subject} onChange={e => setSubject(e.target.value)} required placeholder="Brief description of your issue"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">Message</label>
                <textarea value={message} onChange={e => setMessage(e.target.value)} required rows={4} placeholder="Describe your issue in detail..."
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
              <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">My Requests</h3>
              <div className="space-y-3">
                {tickets.map(t => (
                  <div key={t.id} className="border border-gray-100 rounded-md p-3">
                    <div className="flex items-center justify-between mb-1">
                      <span className="text-sm font-medium text-gray-900">{t.subject}</span>
                      <Badge variant={statusBadge[t.status] || 'gray'}>{t.status}</Badge>
                    </div>
                    <p className="text-xs text-gray-500 mb-1">{t.message}</p>
                    <div className="flex items-center gap-1 text-xs text-gray-400">
                      <Clock size={10} /> {new Date(t.createdAt).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' })}
                    </div>
                    {t.adminResponse && (
                      <div className="mt-2 p-2 bg-[#FAECE7] border border-[#D85A30]/20 rounded text-xs text-[#712B13]">
                        <div className="flex items-center gap-1 mb-1 font-medium"><CheckCircle size={10} /> Admin Response</div>
                        {t.adminResponse}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
