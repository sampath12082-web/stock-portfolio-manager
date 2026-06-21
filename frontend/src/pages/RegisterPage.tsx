import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';

export default function RegisterPage() {
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
  const [otpStep, setOtpStep] = useState(false);
  const [otp, setOtp] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  async function handleRegister(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await axios.post('/api/auth/register', form);
      setOtpStep(true);
      setSuccess('OTP sent to your email. Please verify.');
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Registration failed');
    } finally {
      setLoading(false);
    }
  }

  async function handleVerifyOtp(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      await axios.post('/api/auth/verify-otp', { email: form.email, otpCode: otp });
      setSuccess('Email verified! Redirecting to login...');
      setTimeout(() => navigate('/login'), 2000);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'OTP verification failed');
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-blue-600">MyPortfolio</h1>
          <p className="text-sm text-gray-500 mt-1">{otpStep ? 'Verify your email' : 'Create your account'}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6 space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">{error}</div>}
          {success && <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-md text-sm text-emerald-700">{success}</div>}

          {!otpStep ? (
            <form onSubmit={handleRegister} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">First Name</label>
                <input type="text" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Last Name</label>
                <input type="text" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Password (min 6 characters)</label>
                <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={6}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading}
                className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Creating account...' : 'Create Account'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Enter 6-digit OTP</label>
                <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} placeholder="000000"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm text-center text-lg tracking-widest focus:outline-none focus:ring-2 focus:ring-blue-500" />
              </div>
              <button type="submit" disabled={loading}
                className="w-full py-2 bg-blue-600 hover:bg-blue-500 text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Verifying...' : 'Verify OTP'}
              </button>
            </form>
          )}
          <div className="text-center text-xs text-gray-500">
            Already have an account? <Link to="/login" className="text-blue-600 hover:underline">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
