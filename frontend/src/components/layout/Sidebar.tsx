import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Briefcase, ArrowLeftRight, Database, PieChart, TrendingUp, Shield, HelpCircle, MessageSquare, Sparkles, X,
} from 'lucide-react';
import { useAuth } from '@/auth/AuthContext';
import Logo from '@/components/brand/Logo';

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/holdings', label: 'Holdings', icon: Briefcase },
  { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { to: '/stocks', label: 'Stocks', icon: Database },
  { to: '/mutual-funds', label: 'Mutual Funds', icon: PieChart },
  { to: '/performance', label: 'Performance', icon: TrendingUp },
  { to: '/ai-search', label: 'AI Search', icon: Sparkles },
  { to: '/help', label: 'Help & Support', icon: HelpCircle },
];

const adminLinks = [
  { to: '/admin/users', label: 'User Management', icon: Shield },
  { to: '/admin/tickets', label: 'Support Tickets', icon: MessageSquare },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  const { isAdmin } = useAuth();

  return (
    <>
      {open && (
        <div className="fixed inset-0 bg-black/30 z-40 lg:hidden" onClick={onClose} />
      )}
      <aside className={`
        fixed top-0 left-0 h-full w-60 bg-white border-r border-[#D3D1C7] z-50
        flex flex-col transition-transform duration-200
        ${open ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:z-auto
      `}>
        <div className="flex items-center justify-between h-14 px-4 border-b border-[#D3D1C7]">
          <Logo size="sm" />
          <button onClick={onClose} className="lg:hidden text-gray-400 hover:text-gray-600">
            <X size={20} />
          </button>
        </div>
        <nav className="flex-1 py-3 px-2 space-y-1">
          {links.map(({ to, label, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              end={to === '/'}
              onClick={onClose}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                  isActive
                    ? 'bg-[#FAECE7] text-[#D85A30] font-medium'
                    : 'text-[#444441] hover:text-[#2C2C2A] hover:bg-[#F1EFE8]'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
          {isAdmin && (
            <>
              <div className="border-t border-[#D3D1C7] my-2" />
              {adminLinks.map(({ to, label, icon: Icon }) => (
                <NavLink
                  key={to}
                  to={to}
                  onClick={onClose}
                  className={({ isActive }) =>
                    `flex items-center gap-3 px-3 py-2 rounded-md text-sm transition-colors ${
                      isActive
                        ? 'bg-[#FAEEDA] text-[#BA7517] font-medium'
                        : 'text-[#444441] hover:text-[#2C2C2A] hover:bg-[#F1EFE8]'
                    }`
                  }
                >
                  <Icon size={18} />
                  {label}
                </NavLink>
              ))}
            </>
          )}
        </nav>
        <div className="px-4 py-3 border-t border-[#D3D1C7]">
          <span className="text-[10px] text-[#888780] font-mono tracking-wide">v0.1.0-beta</span>
        </div>
      </aside>
    </>
  );
}
