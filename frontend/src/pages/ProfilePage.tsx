import { useState } from 'react';
import { useAuth } from '@/auth/AuthContext';
import client from '@/api/client';
import axios from 'axios';

export default function ProfilePage() {
  const { user, login, accessToken, refreshToken } = useAuth();
  const [form, setForm] = useState({ firstName: user?.firstName || '', lastName: user?.lastName || '', phone: user?.phone || '' });
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '' });
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  async function handleUpdateProfile(e: React.FormEvent) {
    e.preventDefault();
    setMsg(''); setError(''); setSaving(true);
    try {
      const resp = await client.put('/profile', form);
      if (user && accessToken && refreshToken) {
        login(accessToken, refreshToken, { ...user, ...resp.data });
      }
      setMsg('Profile updated');
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Update failed');
    } finally { setSaving(false); }
  }

  async function handleChangePassword(e: React.FormEvent) {
    e.preventDefault();
    setMsg(''); setError(''); setSaving(true);
    try {
      await client.post('/auth/change-password', pwForm);
      setMsg('Password changed successfully');
      setPwForm({ currentPassword: '', newPassword: '' });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Password change failed');
    } finally { setSaving(false); }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Profile</h1>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Personal Details</h3>
          {msg && <div className="p-2 bg-emerald-50 border border-emerald-200 rounded text-sm text-emerald-700 mb-3">{msg}</div>}
          {error && <div className="p-2 bg-red-50 border border-red-200 rounded text-sm text-red-700 mb-3">{error}</div>}
          <form onSubmit={handleUpdateProfile} className="space-y-3">
            <div>
              <label className="block text-xs text-gray-500 mb-1">Email (cannot be changed)</label>
              <input value={user?.email || ''} disabled className="w-full bg-gray-100 border border-gray-200 rounded-md px-3 py-2 text-sm text-gray-500 cursor-not-allowed" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">First Name</label>
              <input value={form.firstName} onChange={(e) => setForm({ ...form, firstName: e.target.value })} required
                className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">Last Name</label>
              <input value={form.lastName} onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">Phone</label>
              <input value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })}
                className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
            </div>
            <div className="flex justify-between items-center text-sm">
              <span className="text-gray-400">Role: <span className="font-medium text-gray-600">{user?.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</span></span>
              <button type="submit" disabled={saving} className="px-4 py-1.5 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </form>
        </div>

        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Change Password</h3>
          <form onSubmit={handleChangePassword} className="space-y-3">
            <div>
              <label className="block text-xs text-gray-500 mb-1">Current Password</label>
              <input type="password" value={pwForm.currentPassword} onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })} required
                className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
            </div>
            <div>
              <label className="block text-xs text-gray-500 mb-1">New Password (min 6 characters)</label>
              <input type="password" value={pwForm.newPassword} onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })} required minLength={6}
                className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
            </div>
            <div className="text-right">
              <button type="submit" disabled={saving} className="px-4 py-1.5 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                {saving ? 'Changing...' : 'Change Password'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}
