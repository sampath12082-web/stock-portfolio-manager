import { useState, useRef, useEffect } from 'react';
import { Send, Sparkles, TrendingUp, TrendingDown, Minus, Bot, User } from 'lucide-react';
import client from '@/api/client';
import Badge from '@/components/ui/Badge';
import { formatCurrency } from '@/utils/format';

interface StockData {
  symbol: string;
  companyName: string;
  exchange: string;
  sector: string;
  ltp: number | null;
  dayChange: number | null;
  dayChangePct: number | null;
  signalType: string;
  targetPrice: number | null;
  existsInDb: boolean;
}

interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
  stockData?: StockData;
  source?: string;
  timestamp: Date;
}

const suggestions = [
  'How is my portfolio performing?',
  'What stocks should I buy?',
  'Analyze TCS',
  'Show me sell signals',
  'Which sectors are doing well?',
  'Tell me about RELIANCE',
  'What is the market trend today?',
  'Review HDFC Bank',
];

export default function AiSearchPage() {
  const [prompt, setPrompt] = useState('');
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [loading, setLoading] = useState(false);
  const chatEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => { chatEndRef.current?.scrollIntoView({ behavior: 'smooth' }); }, [messages]);

  async function handleSend(text?: string) {
    const q = (text || prompt).trim();
    if (!q) return;
    setPrompt('');
    const userMsg: ChatMessage = { role: 'user', content: q, timestamp: new Date() };
    setMessages(prev => [...prev, userMsg]);
    setLoading(true);

    try {
      const resp = await client.post('/ai/chat', { prompt: q });
      const data = resp.data;
      const assistantMsg: ChatMessage = {
        role: 'assistant',
        content: data.response,
        stockData: data.stockData,
        source: data.source,
        timestamp: new Date(),
      };
      setMessages(prev => [...prev, assistantMsg]);
    } catch {
      setMessages(prev => [...prev, { role: 'assistant', content: 'Sorry, something went wrong. Try again.', timestamp: new Date() }]);
    } finally {
      setLoading(false);
    }
  }

  const signalBadge: Record<string, { color: 'green' | 'red' | 'yellow' | 'gray'; label: string }> = {
    BUY_SIGNAL: { color: 'green', label: 'BUY' },
    SELL_SIGNAL: { color: 'red', label: 'SELL' },
    HOLD: { color: 'yellow', label: 'HOLD' },
    NO_SIGNAL: { color: 'gray', label: 'NO SIGNAL' },
  };

  return (
    <div className="flex flex-col h-[calc(100vh-3.5rem)]">
      <div className="flex items-center gap-3 px-4 py-3 border-b border-gray-200 bg-white">
        <Sparkles size={22} className="text-purple-600" />
        <h1 className="text-lg font-bold text-gray-900">AI Stock Assistant</h1>
        <span className="text-xs text-gray-400">Ask anything about stocks, portfolio, signals, or market</span>
      </div>

      <div className="flex-1 overflow-y-auto px-4 py-4 space-y-4 bg-gray-50">
        {messages.length === 0 && (
          <div className="flex flex-col items-center justify-center h-full text-center">
            <Sparkles size={48} className="text-purple-200 mb-4" />
            <h2 className="text-lg font-semibold text-gray-700 mb-2">Ask me anything about stocks</h2>
            <p className="text-sm text-gray-400 mb-6 max-w-md">I can analyze stocks, review your portfolio, show trading signals, and provide market insights.</p>
            <div className="flex flex-wrap gap-2 justify-center max-w-lg">
              {suggestions.map(s => (
                <button key={s} onClick={() => handleSend(s)}
                  className="px-3 py-1.5 bg-white border border-gray-200 rounded-full text-xs text-gray-600 hover:bg-purple-50 hover:border-purple-300 hover:text-purple-700 transition-colors">
                  {s}
                </button>
              ))}
            </div>
          </div>
        )}

        {messages.map((msg, i) => (
          <div key={i} className={`flex gap-3 ${msg.role === 'user' ? 'justify-end' : 'justify-start'}`}>
            {msg.role === 'assistant' && (
              <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center shrink-0">
                <Bot size={16} className="text-purple-600" />
              </div>
            )}
            <div className={`max-w-2xl ${msg.role === 'user' ? 'bg-[#D85A30] text-white rounded-2xl rounded-br-md' : 'bg-white border border-gray-200 rounded-2xl rounded-bl-md shadow-sm'} px-4 py-3`}>
              <p className={`text-sm leading-relaxed whitespace-pre-line ${msg.role === 'user' ? 'text-white' : 'text-gray-700'}`}>{msg.content}</p>

              {msg.stockData && (
                <div className={`mt-3 p-3 rounded-lg ${msg.role === 'user' ? 'bg-[#C04E28]' : 'bg-gray-50'}`}>
                  <div className="flex items-center justify-between mb-2">
                    <div>
                      <span className="font-bold text-sm">{msg.stockData.symbol}</span>
                      <span className={`text-xs ml-2 ${msg.role === 'user' ? 'text-[#F0997B]' : 'text-gray-400'}`}>{msg.stockData.companyName}</span>
                    </div>
                    {msg.stockData.ltp && (
                      <span className="font-bold text-sm">{formatCurrency(msg.stockData.ltp)}</span>
                    )}
                  </div>
                  <div className="flex items-center gap-2 flex-wrap">
                    {msg.stockData.dayChangePct != null && (
                      <span className={`text-xs font-medium ${msg.stockData.dayChangePct >= 0 ? 'text-emerald-600' : 'text-red-600'}`}>
                        {msg.stockData.dayChangePct >= 0 ? '+' : ''}{msg.stockData.dayChangePct}%
                      </span>
                    )}
                    <Badge variant={msg.stockData.exchange === 'NSE' ? 'blue' : 'yellow'}>{msg.stockData.exchange}</Badge>
                    {(() => {
                      const sig = signalBadge[msg.stockData.signalType] || signalBadge.NO_SIGNAL;
                      return <Badge variant={sig.color}>{sig.label}</Badge>;
                    })()}
                    {msg.stockData.targetPrice && (
                      <span className="text-xs text-gray-500">Target: {formatCurrency(msg.stockData.targetPrice)}</span>
                    )}
                    {msg.stockData.existsInDb && <Badge variant="green">In Portfolio</Badge>}
                  </div>
                </div>
              )}

              <div className={`flex items-center gap-2 mt-1 ${msg.role === 'user' ? 'justify-end' : ''}`}>
                <span className={`text-[10px] ${msg.role === 'user' ? 'text-[#F0997B]' : 'text-gray-300'}`}>
                  {msg.timestamp.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit' })}
                </span>
                {msg.source === 'claude' && <span className="text-[10px] text-purple-400">Claude AI</span>}
                {msg.source === 'local' && <span className="text-[10px] text-gray-300">Local</span>}
              </div>
            </div>
            {msg.role === 'user' && (
              <div className="w-8 h-8 rounded-full bg-[#FAECE7] flex items-center justify-center shrink-0">
                <User size={16} className="text-[#D85A30]" />
              </div>
            )}
          </div>
        ))}

        {loading && (
          <div className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-purple-100 flex items-center justify-center shrink-0">
              <Bot size={16} className="text-purple-600 animate-pulse" />
            </div>
            <div className="bg-white border border-gray-200 rounded-2xl rounded-bl-md shadow-sm px-4 py-3">
              <div className="flex gap-1">
                <span className="w-2 h-2 bg-gray-300 rounded-full animate-bounce" style={{ animationDelay: '0ms' }} />
                <span className="w-2 h-2 bg-gray-300 rounded-full animate-bounce" style={{ animationDelay: '150ms' }} />
                <span className="w-2 h-2 bg-gray-300 rounded-full animate-bounce" style={{ animationDelay: '300ms' }} />
              </div>
            </div>
          </div>
        )}
        <div ref={chatEndRef} />
      </div>

      <div className="border-t border-gray-200 bg-white px-4 py-3">
        <form onSubmit={(e) => { e.preventDefault(); handleSend(); }} className="flex gap-3">
          <input value={prompt} onChange={e => setPrompt(e.target.value)} placeholder="Ask about any stock, your portfolio, or market trends..."
            disabled={loading}
            className="flex-1 bg-gray-50 border border-gray-300 rounded-xl px-4 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-purple-500 disabled:opacity-50" />
          <button type="submit" disabled={loading || !prompt.trim()}
            className="flex items-center gap-2 px-5 py-2.5 bg-purple-600 hover:bg-purple-500 text-white rounded-xl text-sm font-medium disabled:opacity-50 transition-colors">
            <Send size={14} />
          </button>
        </form>
      </div>
    </div>
  );
}
