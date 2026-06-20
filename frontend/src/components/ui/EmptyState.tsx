import { Inbox } from 'lucide-react';
import type { ReactNode } from 'react';

interface EmptyStateProps {
  message: string;
  action?: ReactNode;
}

export default function EmptyState({ message, action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 text-gray-400">
      <Inbox size={48} className="mb-3 opacity-50" />
      <p className="text-sm">{message}</p>
      {action && <div className="mt-3">{action}</div>}
    </div>
  );
}
