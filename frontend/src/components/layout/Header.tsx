import { Menu, RefreshCw, LogOut, User } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useRefreshQuotes } from '@/hooks/useQuotes';
import { useAuth } from '@/auth/AuthContext';

interface HeaderProps {
  onMenuClick: () => void;
}

export default function Header({ onMenuClick }: HeaderProps) {
  const refreshMutation = useRefreshQuotes();
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate('/login');
  }

  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4">
      <button onClick={onMenuClick} className="lg:hidden text-gray-500 hover:text-gray-700">
        <Menu size={22} />
      </button>
      <div className="hidden lg:block" />
      <div className="flex items-center gap-3">
        <button
          onClick={() => refreshMutation.mutate()}
          disabled={refreshMutation.isPending}
          className="flex items-center gap-2 px-3 py-1.5 text-sm bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-md transition-colors disabled:opacity-50"
        >
          <RefreshCw size={14} className={refreshMutation.isPending ? 'animate-spin' : ''} />
          Refresh Quotes
        </button>
        <button onClick={() => navigate('/profile')} className="flex items-center gap-2 px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 hover:bg-gray-50 rounded-md">
          <User size={14} />
          <span className="hidden sm:inline">{user?.firstName || 'Profile'}</span>
        </button>
        <button onClick={handleLogout}
          className="flex items-center gap-2 px-3 py-1.5 text-sm text-red-600 hover:text-red-700 hover:bg-red-50 rounded-md">
          <LogOut size={14} />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
}
