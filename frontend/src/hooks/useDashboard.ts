import { useQuery } from '@tanstack/react-query';
import { getDashboard } from '@/api/endpoints';

export function useDashboard() {
  return useQuery({ queryKey: ['dashboard'], queryFn: getDashboard });
}
