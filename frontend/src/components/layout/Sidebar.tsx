import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard, Briefcase, ArrowLeftRight, Database, PieChart, TrendingUp, X,
} from 'lucide-react';

const links = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/holdings', label: 'Holdings', icon: Briefcase },
  { to: '/transactions', label: 'Transactions', icon: ArrowLeftRight },
  { to: '/stocks', label: 'Stocks', icon: Database },
  { to: '/mutual-funds', label: 'Mutual Funds', icon: PieChart },
  { to: '/performance', label: 'Performance', icon: TrendingUp },
];

interface SidebarProps {
  open: boolean;
  onClose: () => void;
}

export default function Sidebar({ open, onClose }: SidebarProps) {
  return (
    <>
      {open && (
        <div className="fixed inset-0 bg-black/30 z-40 lg:hidden" onClick={onClose} />
      )}
      <aside className={`
        fixed top-0 left-0 h-full w-60 bg-white border-r border-gray-200 z-50
        flex flex-col transition-transform duration-200
        ${open ? 'translate-x-0' : '-translate-x-full'}
        lg:translate-x-0 lg:static lg:z-auto
      `}>
        <div className="flex items-center justify-between h-14 px-4 border-b border-gray-200">
          <span className="text-lg font-bold text-blue-600">MyPortfolio</span>
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
                    ? 'bg-blue-50 text-blue-600 font-medium'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
      </aside>
    </>
  );
}
