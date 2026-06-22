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
    <header className="h-14 bg-white border-b border-[#D3D1C7] flex items-center justify-between px-4">
      <button onClick={onMenuClick} className="lg:hidden text-[#888780] hover:text-[#2C2C2A]">
        <Menu size={22} />
      </button>
      <div className="hidden lg:block" />
      <div className="flex items-center gap-3">
        <button
          onClick={() => refreshMutation.mutate()}
          disabled={refreshMutation.isPending}
          className="flex items-center gap-2 px-3 py-1.5 text-sm bg-[#F1EFE8] hover:bg-[#E8E5DB] border border-[#D3D1C7] rounded-md transition-colors disabled:opacity-50"
        >
          <RefreshCw size={14} className={refreshMutation.isPending ? 'animate-spin' : ''} />
          Refresh Quotes
        </button>
        <button onClick={() => navigate('/profile')} className="flex items-center gap-2 px-3 py-1.5 text-sm text-[#444441] hover:text-[#2C2C2A] hover:bg-[#F1EFE8] rounded-md">
          <User size={14} />
          <span className="hidden sm:inline">{user?.firstName || 'Profile'}</span>
        </button>
        <button onClick={handleLogout}
          className="flex items-center gap-2 px-3 py-1.5 text-sm text-[#D85A30] hover:text-[#C04E28] hover:bg-[#FAECE7] rounded-md">
          <LogOut size={14} />
          <span className="hidden sm:inline">Logout</span>
        </button>
      </div>
    </header>
  );
}
