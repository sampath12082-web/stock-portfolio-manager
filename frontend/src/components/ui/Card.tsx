import type { ReactNode } from 'react';

interface CardProps {
  title?: ReactNode;
  children: ReactNode;
  className?: string;
}

export default function Card({ title, children, className = '' }: CardProps) {
  return (
    <div className={`bg-white border border-gray-200 rounded-lg p-5 shadow-sm ${className}`}>
      {title && <h3 className="text-sm font-bold text-gray-400 pb-1.5 mb-2.5 border-b border-gray-100">{title}</h3>}
      {children}
    </div>
  );
}
