import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import axios from 'axios';
import Logo from '@/components/brand/Logo';

export default function RegisterPage() {
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '', securityQuestion1: '', securityAnswer1: '', securityQuestion2: '', securityAnswer2: '' });
  const securityQuestions = [
    'What is your mother\'s maiden name?',
    'What was the name of your first pet?',
    'What city were you born in?',
    'What is the name of your first school?',
    'What is your favorite movie?',
    'What was your childhood nickname?',
    'What is the name of the street you grew up on?',
    'What is your favorite book?',
  ];
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
    <div className="min-h-screen bg-[#FAFAF8] flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <Logo size="lg" />
          <p className="text-sm text-[#888780] mt-3">{otpStep ? 'Verify your email' : 'Create your account'}</p>
        </div>
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-6 space-y-4">
          {error && <div className="p-3 bg-red-50 border border-red-200 rounded-md text-sm text-red-700">{error}</div>}
          {success && <div className="p-3 bg-emerald-50 border border-emerald-200 rounded-md text-sm text-emerald-700">{success}</div>}

          {!otpStep ? (
            <form onSubmit={handleRegister} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Email</label>
                <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">First Name</label>
                <input type="text" value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Last Name</label>
                <input type="text" value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Password (16-20 chars, upper+lower+digit+special)</label>
                <input type="password" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required minLength={16} maxLength={20}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Security Question 1</label>
                <select value={form.securityQuestion1} onChange={(e) => setForm({ ...form, securityQuestion1: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
                  <option value="">Select a question...</option>
                  {securityQuestions.map(q => <option key={q} value={q}>{q}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Answer 1</label>
                <input type="text" value={form.securityAnswer1} onChange={(e) => setForm({ ...form, securityAnswer1: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Security Question 2</label>
                <select value={form.securityQuestion2} onChange={(e) => setForm({ ...form, securityQuestion2: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm">
                  <option value="">Select a question...</option>
                  {securityQuestions.filter(q => q !== form.securityQuestion1).map(q => <option key={q} value={q}>{q}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-sm text-gray-600 mb-1">Answer 2</label>
                <input type="text" value={form.securityAnswer2} onChange={(e) => setForm({ ...form, securityAnswer2: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <button type="submit" disabled={loading}
                className="w-full py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Creating account...' : 'Create Account'}
              </button>
            </form>
          ) : (
            <form onSubmit={handleVerifyOtp} className="space-y-4">
              <div>
                <label className="block text-sm text-gray-600 mb-1">Enter 6-digit OTP</label>
                <input type="text" value={otp} onChange={(e) => setOtp(e.target.value)} required maxLength={6} placeholder="000000"
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm text-center text-lg tracking-widest focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50" />
              </div>
              <button type="submit" disabled={loading}
                className="w-full py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                {loading ? 'Verifying...' : 'Verify OTP'}
              </button>
            </form>
          )}
          <div className="text-center text-xs text-gray-500">
            Already have an account? <Link to="/login" className="text-[#D85A30] hover:underline">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
