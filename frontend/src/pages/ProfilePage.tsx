import { useState, useEffect } from 'react';
import { useAuth } from '@/auth/AuthContext';
import { encryptField } from '@/auth/crypto';
import client from '@/api/client';
import axios from 'axios';

export default function ProfilePage() {
  const { user, login, accessToken, refreshToken } = useAuth();
  const [form, setForm] = useState({ firstName: user?.firstName || '', lastName: user?.lastName || '', phone: user?.phone || '' });
  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '' });
  const [growwForm, setGrowwForm] = useState({ accessToken: '', apiSecret: '' });
  const [growwStatus, setGrowwStatus] = useState<{ enabled: boolean; hasAccessToken: boolean; hasApiSecret: boolean } | null>(null);
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    client.get('/profile/groww').then(r => setGrowwStatus(r.data)).catch(() => {});
  }, []);

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
      const encCurrent = await encryptField(pwForm.currentPassword);
      const encNew = await encryptField(pwForm.newPassword);
      await client.post('/auth/change-password', { currentPassword: encCurrent, newPassword: encNew });
      setMsg('Password changed successfully');
      setPwForm({ currentPassword: '', newPassword: '' });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Password change failed');
    } finally { setSaving(false); }
  }

  async function handleSaveGroww(e: React.FormEvent) {
    e.preventDefault();
    setMsg(''); setError(''); setSaving(true);
    try {
      const encToken = growwForm.accessToken ? await encryptField(growwForm.accessToken) : undefined;
      const encSecret = growwForm.apiSecret ? await encryptField(growwForm.apiSecret) : undefined;
      const data: Record<string, string> = {};
      if (encToken) data.accessToken = encToken;
      if (encSecret) data.apiSecret = encSecret;
      const resp = await client.put('/profile/groww', data);
      const result = resp.data;
      if (result.connected) {
        setMsg(result.validationMessage || 'Connected to Groww successfully');
      } else if (result.connected === false) {
        setError(result.validationMessage || 'Groww connection failed — check credentials');
      } else {
        setMsg('Groww config saved');
      }
      setGrowwStatus({ enabled: result.enabled, hasAccessToken: true, hasApiSecret: true });
      setGrowwForm({ accessToken: '', apiSecret: '' });
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Groww config update failed');
    } finally { setSaving(false); }
  }

  async function handleDeleteGroww() {
    setMsg(''); setError('');
    try {
      await client.delete('/profile/groww');
      setMsg('Groww config removed');
      setGrowwStatus(null);
    } catch (err: unknown) {
      if (axios.isAxiosError(err)) setError(err.response?.data?.message || 'Failed to remove');
    }
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Profile</h1>

      {msg && <div className="p-2 bg-emerald-50 border border-emerald-200 rounded text-sm text-emerald-700">{msg}</div>}
      {error && <div className="p-2 bg-red-50 border border-red-200 rounded text-sm text-red-700">{error}</div>}

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
          <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Personal Details</h3>
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

        <div className="space-y-6">
          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Change Password</h3>
            <form onSubmit={handleChangePassword} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Current Password</label>
                <input type="password" value={pwForm.currentPassword} onChange={(e) => setPwForm({ ...pwForm, currentPassword: e.target.value })} required
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">New Password (16-20 characters)</label>
                <input type="password" value={pwForm.newPassword} onChange={(e) => setPwForm({ ...pwForm, newPassword: e.target.value })} required minLength={16} maxLength={20}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
              </div>
              <div className="text-right">
                <button type="submit" disabled={saving} className="px-4 py-1.5 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50">
                  {saving ? 'Changing...' : 'Change Password'}
                </button>
              </div>
            </form>
          </div>

          <div className="bg-white border border-gray-200 rounded-lg shadow-sm p-5">
            <h3 className="text-xs font-bold text-gray-400 uppercase tracking-wide pb-1.5 mb-3 border-b border-gray-100">Groww Config</h3>
            {growwStatus && (
              <div className="flex items-center gap-2 mb-3">
                <span className={`w-2 h-2 rounded-full ${growwStatus.enabled ? 'bg-emerald-500' : 'bg-gray-300'}`} />
                <span className="text-xs text-gray-500">{growwStatus.enabled ? 'Connected' : 'Not connected'}</span>
                {growwStatus.hasAccessToken && <span className="text-xs text-emerald-600">Token set</span>}
                {growwStatus.hasApiSecret && <span className="text-xs text-emerald-600">Secret set</span>}
              </div>
            )}
            <form onSubmit={handleSaveGroww} className="space-y-3">
              <div>
                <label className="block text-xs text-gray-500 mb-1">Access Token {growwStatus?.hasAccessToken && <span className="text-emerald-500">(saved — enter new to update)</span>}</label>
                <input type="password" value={growwForm.accessToken} onChange={(e) => setGrowwForm({ ...growwForm, accessToken: e.target.value })}
                  placeholder={growwStatus?.hasAccessToken ? '••••••••••••' : 'Paste Groww JWT token'}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
              </div>
              <div>
                <label className="block text-xs text-gray-500 mb-1">API Secret {growwStatus?.hasApiSecret && <span className="text-emerald-500">(saved — enter new to update)</span>}</label>
                <input type="password" value={growwForm.apiSecret} onChange={(e) => setGrowwForm({ ...growwForm, apiSecret: e.target.value })}
                  placeholder={growwStatus?.hasApiSecret ? '••••••••••••' : 'Paste Groww API secret'}
                  className="w-full bg-gray-50 border border-gray-300 rounded-md px-3 py-2 text-sm" />
              </div>
              <p className="text-[10px] text-gray-400">Credentials are RSA-encrypted before sending. Never visible in network traffic.</p>
              <div className="flex justify-between">
                {growwStatus?.enabled && (
                  <button type="button" onClick={handleDeleteGroww} className="px-3 py-1.5 text-red-600 hover:bg-red-50 rounded-md text-xs font-medium">
                    Remove Config
                  </button>
                )}
                <button type="submit" disabled={saving || (!growwForm.accessToken && !growwForm.apiSecret)}
                  className="px-4 py-1.5 bg-[#D85A30] hover:bg-[#C04E28] text-white rounded-md text-sm font-medium disabled:opacity-50 ml-auto">
                  {saving ? 'Saving...' : 'Save Groww Config'}
                </button>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}
