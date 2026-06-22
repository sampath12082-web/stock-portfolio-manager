import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '@/auth/AuthContext';
import { encryptField } from '@/auth/crypto';
import type { AuthResponse } from '@/auth/types';
import axios from 'axios';
import Logo from '@/components/brand/Logo';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const encryptedPassword = await encryptField(password);
      const resp = await axios.post<AuthResponse>('/api/auth/login', { email, password: encryptedPassword });
      login(resp.data.accessToken, resp.data.refreshToken, resp.data.user);
      navigate('/');
    } catch (err: unknown) {
      if (axios.isAxiosError(err) && err.response?.data?.message) {
        setError(err.response.data.message);
      } else {
        setError('Login failed. Please try again.');
      }
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="min-h-screen bg-[#FAFAF8] flex items-center justify-center px-4">
      <div className="w-full max-w-sm">
        <div className="flex flex-col items-center mb-8">
          <Logo size="lg" />
          <p className="text-sm text-[#888780] mt-3">Sign in to your account</p>
        </div>
        <form onSubmit={handleSubmit} className="bg-white border border-[#D3D1C7] rounded-lg shadow-sm p-6 space-y-4">
          {error && <div className="p-3 bg-[#FAECE7] border border-[#D85A30]/30 rounded-md text-sm text-[#712B13]">{error}</div>}
          <div>
            <label className="block text-sm text-[#444441] mb-1">Email</label>
            <input type="email" value={email} onChange={(e) => setEmail(e.target.value)} required autoFocus
              className="w-full bg-[#FAFAF8] border border-[#D3D1C7] rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50 focus:border-[#D85A30]" />
          </div>
          <div>
            <label className="block text-sm text-[#444441] mb-1">Password</label>
            <input type="password" value={password} onChange={(e) => setPassword(e.target.value)} required
              className="w-full bg-[#FAFAF8] border border-[#D3D1C7] rounded-md px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#D85A30]/50 focus:border-[#D85A30]" />
          </div>
          <button type="submit" disabled={loading}
            className="w-full py-2 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50 transition-colors">
            {loading ? 'Signing in...' : 'Sign In'}
          </button>
          <div className="flex justify-between text-xs text-[#888780]">
            <Link to="/forgot-password" className="hover:text-[#D85A30]">Forgot password?</Link>
            <Link to="/register" className="hover:text-[#D85A30]">Create account</Link>
          </div>
        </form>
      </div>
    </div>
  );
}
