import { useState, useEffect } from 'react';
import { Shield, UserX, UserCheck, KeyRound, Trash2 } from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import client from '@/api/client';
import Badge from '@/components/ui/Badge';
import LoadingSpinner from '@/components/ui/LoadingSpinner';
import type { UserResponse } from '@/auth/types';

export default function AdminUsersPage() {
  const { isAdmin } = useAuth();
  const [users, setUsers] = useState<UserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [msg, setMsg] = useState('');

  useEffect(() => { fetchUsers(); }, []);

  async function fetchUsers() {
    setLoading(true);
    try {
      const resp = await client.get<UserResponse[]>('/admin/users');
      setUsers(resp.data);
    } catch { setMsg('Failed to load users'); }
    finally { setLoading(false); }
  }

  async function updateStatus(id: number, status: string) {
    try {
      await client.put(`/admin/users/${id}/status`, { status });
      setMsg(`User ${status.toLowerCase()}`);
      fetchUsers();
    } catch { setMsg('Failed to update status'); }
    setTimeout(() => setMsg(''), 3000);
  }

  async function resetPassword(id: number, email: string) {
    if (!confirm(`Reset password for ${email}?`)) return;
    try {
      const resp = await client.post(`/admin/users/${id}/reset-password`);
      setMsg(`Password reset. Temporary: ${resp.data.temporaryPassword}`);
    } catch { setMsg('Failed to reset password'); }
  }

  async function deleteUser(id: number, email: string) {
    if (!confirm(`Delete user ${email}? This cannot be undone.`)) return;
    try {
      const resp = await client.delete(`/admin/users/${id}`);
      if (resp.data.error) { setMsg(resp.data.error); }
      else { setMsg('User deleted'); fetchUsers(); }
    } catch { setMsg('Failed to delete user'); }
    setTimeout(() => setMsg(''), 5000);
  }

  if (!isAdmin) return <div className="p-8 text-center text-gray-500">Access denied — admin only</div>;
  if (loading) return <LoadingSpinner />;

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-3">
        <Shield size={24} className="text-[#D85A30]" />
        <h1 className="text-2xl font-bold text-gray-900">Admin — User Management</h1>
      </div>

      {msg && <div className="p-3 bg-[#FAECE7] border border-[#D85A30]/30 rounded-md text-sm text-[#712B13]">{msg}</div>}

      <div className="bg-white border border-gray-200 rounded-lg shadow-sm overflow-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="text-gray-500 text-left border-b border-gray-200 bg-gray-50">
              <th className="py-3 px-3">ID</th>
              <th className="py-3 px-3">Email</th>
              <th className="py-3 px-3">Name</th>
              <th className="py-3 px-3">Role</th>
              <th className="py-3 px-3">Status</th>
              <th className="py-3 px-3">Verified</th>
              <th className="py-3 px-3">Actions</th>
            </tr>
          </thead>
          <tbody>
            {users.map((u) => (
              <tr key={u.id} className="border-b border-gray-100 hover:bg-gray-50">
                <td className="py-2.5 px-3 text-gray-400">{u.id}</td>
                <td className="py-2.5 px-3 font-medium text-gray-900">{u.email}</td>
                <td className="py-2.5 px-3">{u.firstName} {u.lastName || ''}</td>
                <td className="py-2.5 px-3"><Badge variant={u.role === 'ROLE_ADMIN' ? 'purple' : 'blue'}>{u.role === 'ROLE_ADMIN' ? 'Admin' : 'User'}</Badge></td>
                <td className="py-2.5 px-3"><Badge variant={u.status === 'ACTIVE' ? 'green' : u.status === 'SUSPENDED' ? 'red' : 'gray'}>{u.status}</Badge></td>
                <td className="py-2.5 px-3">{u.emailVerified ? <Badge variant="green">Yes</Badge> : <Badge variant="gray">No</Badge>}</td>
                <td className="py-2.5 px-3">
                  <div className="flex gap-1">
                    {u.status === 'ACTIVE' ? (
                      <button onClick={() => updateStatus(u.id, 'INACTIVE')} title="Deactivate" className="p-1 text-gray-400 hover:text-yellow-600"><UserX size={14} /></button>
                    ) : (
                      <button onClick={() => updateStatus(u.id, 'ACTIVE')} title="Activate" className="p-1 text-gray-400 hover:text-green-600"><UserCheck size={14} /></button>
                    )}
                    <button onClick={() => resetPassword(u.id, u.email)} title="Reset Password" className="p-1 text-gray-400 hover:text-[#D85A30]"><KeyRound size={14} /></button>
                    {u.role !== 'ROLE_ADMIN' && (
                      <button onClick={() => deleteUser(u.id, u.email)} title="Delete" className="p-1 text-gray-400 hover:text-red-600"><Trash2 size={14} /></button>
                    )}
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      <p className="text-xs text-gray-400">Total users: {users.length}</p>
    </div>
  );
}
