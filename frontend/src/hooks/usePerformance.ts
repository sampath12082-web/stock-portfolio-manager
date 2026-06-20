import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getRecentPerformance, getPerformanceHistory, getTodaySnapshot, captureSnapshot } from '@/api/endpoints';

export function useRecentPerformance(days = 30) {
  return useQuery({
    queryKey: ['performance', 'recent', days],
    queryFn: () => getRecentPerformance(days),
  });
}

export function usePerformanceHistory(from: string, to: string) {
  return useQuery({
    queryKey: ['performance', 'history', from, to],
    queryFn: () => getPerformanceHistory(from, to),
    enabled: !!from && !!to,
  });
}

export function useTodaySnapshot() {
  return useQuery({ queryKey: ['performance', 'today'], queryFn: getTodaySnapshot });
}

export function useCaptureSnapshot() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: captureSnapshot,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['performance'] }),
  });
}
