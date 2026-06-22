import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ForgotPasswordPage() {
  const [step, setStep] = useState<'email' | 'security' | 'otp' | 'done'>('email');
  const [email, setEmail] = useState('');
  const [q1, setQ1] = useState('');
  const [q2, setQ2] = useState('');
  const [a1, setA1] = useState('');
  const [a2, setA2] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleGetQuestions(e: React.FormEvent) {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      const resp = await axios.post('/api/auth/forgot-password', { email });
      setQ1(resp.data.securityQuestion1);
      setQ2(resp.data.securityQuestion2);
      setStep('security');
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Email not found');
    } finally { setLoading(false); }
  }

  async function handleVerifySecurity(e: React.FormEvent) {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      await axios.post('/api/auth/verify-security', { email, answer1: a1, answer2: a2 });
      setStep('otp');
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Security answers incorrect');
    } finally { setLoading(false); }
  }

  async function handleReset(e: React.FormEvent) {
    e.preventDefault();
    setError(''); setLoading(true);
    try {
      await axios.post('/api/auth/reset-password', { email, otpCode: otp, newPassword });
      setStep('done');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Reset failed');
    } finally { setLoading(false); }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-blue-600">MyPortfolio</h1>
          <p className="text-sm text-gray-500 mt-1">Reset your password</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6 space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">{error}</div>}
          {step === 'done' && <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-md text-sm text-emerald-700">Password reset! Redirecting to login...</div>}

          {step === 'email' && (
            <form onSubmit={handleGetQuestions} className="space-y-4">
              <p className="text-xs text-gray-500">Step 1 of 3: Enter your email</p>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email</label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Checking...' : 'Continue'}
              </button>
            </form>
          )}

          {step === 'security' && (
            <form onSubmit={handleVerifySecurity} className="space-y-4">
              <p className="text-xs text-gray-500">Step 2 of 3: Answer security questions</p>
              <div>
                <label className="block text-sm text-gray-600 mb-1">{q1}</label>
                <input type="text" value={a1} onChange={(e) => setA1(e.target.value)} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">{q2}</label>
                <input type="text" value={a2} onChange={(e) => setA2(e.target.value)} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Verifying...' : 'Verify & Send OTP'}
              </button>
            </form>
          )}

          {step === 'otp' && (
            <form onSubmit={handleReset} className="space-y-4">
              <p className="text-xs text-gray-500">Step 3 of 3: Enter OTP and new password</p>
              <div>
                <label className="block text-sm text-gray-600 mb-1">OTP Code</label>
                <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} placeholder="000000"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm text-center text-lg tracking-widest focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">New Password (16-20 chars)</label>
                <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={16} maxLength={20}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Resetting...' : 'Reset Password'}
              </button>
            </form>
          )}

          <div className="text-center text-xs text-gray-500">
            <Link to="/login" className="text-blue-600 hover:underline">Back to login</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
