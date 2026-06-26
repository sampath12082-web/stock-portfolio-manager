import { NavLink } from 'react-router-dom';
import { LayoutDashboard, Briefcase, ArrowLeftRight, Database, MoreHorizontal } from 'lucide-react';

const links = [
  { to: '/', label: 'Home', icon: LayoutDashboard },
  { to: '/holdings', label: 'Holdings', icon: Briefcase },
  { to: '/transactions', label: 'Trades', icon: ArrowLeftRight },
  { to: '/stocks', label: 'Stocks', icon: Database },
];

export default function BottomNav({ onMore }: { onMore: () => void }) {
  return (
    <nav className="lg:hidden fixed bottom-0 left-0 right-0 bg-white border-t border-[#D3D1C7] z-40 flex justify-around py-1.5 px-1">
      {links.map(({ to, label, icon: Icon }) => (
        <NavLink key={to} to={to} end={to === '/'}
          className={({ isActive }) =>
            `flex flex-col items-center gap-0.5 px-2 py-1 rounded-md text-[10px] ${
              isActive ? 'text-[#D85A30] font-medium' : 'text-[#888780]'
            }`
          }>
          <Icon size={18} />
          {label}
        </NavLink>
      ))}
      <button onClick={onMore} className="flex flex-col items-center gap-0.5 px-2 py-1 rounded-md text-[10px] text-[#888780]">
        <MoreHorizontal size={18} />
        More
      </button>
    </nav>
  );
}
