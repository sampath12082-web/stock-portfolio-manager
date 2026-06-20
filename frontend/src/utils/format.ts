const inrFormatter = new Intl.NumberFormat('en-IN', {
  style: 'currency',
  currency: 'INR',
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

const numberFormatter = new Intl.NumberFormat('en-IN', {
  maximumFractionDigits: 2,
});

export function formatCurrency(value: number | null | undefined): string {
  if (value == null) return '—';
  return inrFormatter.format(value);
}

export function formatNumber(value: number | null | undefined): string {
  if (value == null) return '—';
  return numberFormatter.format(value);
}

export function formatPercentage(value: number | null | undefined): string {
  if (value == null) return '—';
  return `${value >= 0 ? '+' : ''}${value.toFixed(2)}%`;
}

export function formatDate(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTime(dateStr: string | null | undefined): string {
  if (!dateStr) return '—';
  return new Date(dateStr).toLocaleString('en-IN', {
    day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit',
  });
}

export function pnlColor(value: number | null | undefined): string {
  if (value == null || value === 0) return 'text-gray-400';
  return value > 0 ? 'text-emerald-600' : 'text-red-600';
}

export function pnlBgColor(value: number | null | undefined): string {
  if (value == null || value === 0) return 'bg-gray-100';
  return value > 0 ? 'bg-emerald-50' : 'bg-red-50';
}
