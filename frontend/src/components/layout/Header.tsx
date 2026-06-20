import { Menu, RefreshCw } from 'lucide-react';
import { useRefreshQuotes } from '@/hooks/useQuotes';

interface HeaderProps {
  onMenuClick: () => void;
}

export default function Header({ onMenuClick }: HeaderProps) {
  const refreshMutation = useRefreshQuotes();

  return (
    <header className="h-14 bg-white border-b border-gray-200 flex items-center justify-between px-4">
      <button onClick={onMenuClick} className="lg:hidden text-gray-500 hover:text-gray-700">
        <Menu size={22} />
      </button>
      <div className="hidden lg:block" />
      <button
        onClick={() => refreshMutation.mutate()}
        disabled={refreshMutation.isPending}
        className="flex items-center gap-2 px-3 py-1.5 text-sm bg-gray-50 hover:bg-gray-100 border border-gray-200 rounded-md transition-colors disabled:opacity-50"
      >
        <RefreshCw size={14} className={refreshMutation.isPending ? 'animate-spin' : ''} />
        Refresh Quotes
      </button>
    </header>
  );
}
