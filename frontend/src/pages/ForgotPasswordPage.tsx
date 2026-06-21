import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function ForgotPasswordPage() {
  const [step, setStep] = useState<'email' | 'otp' | 'done'>('email');
  const [email, setEmail] = useState('');
  const [otp, setOtp] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleSendOtp(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await axios.post('/api/auth/forgot-password', { email });
      setStep('otp');
    } catch { setError('Failed to send OTP'); }
    finally { setLoading(false); }
  }

  async function handleReset(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
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
            <form onSubmit={handleSendOtp} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email</label>
                <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading} className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Sending...' : 'Send OTP'}
              </button>
            </form>
          )}

          {step === 'otp' && (
            <form onSubmit={handleReset} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">OTP Code</label>
                <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} placeholder="000000"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm text-center text-lg tracking-widest focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">New Password</label>
                <input type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={6}
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
