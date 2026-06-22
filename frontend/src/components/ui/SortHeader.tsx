import { ChevronUp, ChevronDown } from 'lucide-react';

export interface SortConfig {
  key: string;
  dir: 'asc' | 'desc';
}

interface SortHeaderProps {
  label: string;
  sortKey: string;
  sort: SortConfig | null;
  onSort: (key: string) => void;
  align?: 'left' | 'right' | 'center';
}

export default function SortHeader({ label, sortKey, sort, onSort, align = 'left' }: SortHeaderProps) {
  const active = sort?.key === sortKey;
  const alignClass = align === 'right' ? 'text-right justify-end' : align === 'center' ? 'text-center justify-center' : 'text-left';

  return (
    <th className={`py-3 px-3 ${alignClass}`}>
      <button onClick={() => onSort(sortKey)}
        className={`inline-flex items-center gap-1 hover:text-gray-900 transition-colors ${active ? 'text-gray-900 font-semibold' : ''}`}>
        {label}
        <span className="flex flex-col -space-y-1">
          <ChevronUp size={10} className={active && sort.dir === 'asc' ? 'text-[#D85A30]' : 'text-gray-300'} />
          <ChevronDown size={10} className={active && sort.dir === 'desc' ? 'text-[#D85A30]' : 'text-gray-300'} />
        </span>
      </button>
    </th>
  );
}

export function toggleSort(current: SortConfig | null, key: string): SortConfig {
  if (current?.key === key) {
    return { key, dir: current.dir === 'asc' ? 'desc' : 'asc' };
  }
  return { key, dir: 'asc' };
}

export function sortData<T>(data: T[], sort: SortConfig | null, getter: (item: T, key: string) => number | string | null): T[] {
  if (!sort) return data;
  return [...data].sort((a, b) => {
    const va = getter(a, sort.key);
    const vb = getter(b, sort.key);
    if (va == null && vb == null) return 0;
    if (va == null) return 1;
    if (vb == null) return -1;
    const cmp = typeof va === 'string' ? va.localeCompare(vb as string) : (va as number) - (vb as number);
    return sort.dir === 'asc' ? cmp : -cmp;
  });
}
