import type { ReactNode } from 'react';

type Variant = 'green' | 'red' | 'blue' | 'yellow' | 'gray' | 'purple';

const variantClasses: Record<Variant, string> = {
  green: 'bg-emerald-50 text-emerald-700 border-emerald-200',
  red: 'bg-red-50 text-red-700 border-red-200',
  blue: 'bg-blue-50 text-blue-700 border-blue-200',
  yellow: 'bg-yellow-50 text-yellow-700 border-yellow-200',
  gray: 'bg-gray-100 text-gray-600 border-gray-200',
  purple: 'bg-purple-50 text-purple-700 border-purple-200',
};

interface BadgeProps {
  variant: Variant;
  children: ReactNode;
}

export default function Badge({ variant, children }: BadgeProps) {
  return (
    <span className={`inline-flex items-center px-2 py-0.5 text-xs font-medium rounded border ${variantClasses[variant]}`}>
      {children}
    </span>
  );
}
