import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getTodaySignals, getActiveSignals, getSignals, createSignal, updateSignal, deleteSignal } from '@/api/endpoints';
import type { CreateTradingSignalRequest, UpdateTradingSignalRequest } from '@/api/types';

export function useTodaySignals() {
  return useQuery({ queryKey: ['signals', 'today'], queryFn: getTodaySignals });
}

export function useActiveSignals() {
  return useQuery({ queryKey: ['signals', 'active'], queryFn: getActiveSignals });
}

export function useFilteredSignals(params?: { date?: string; symbol?: string }) {
  return useQuery({
    queryKey: ['signals', 'filtered', params],
    queryFn: () => getSignals(params),
    enabled: !!(params?.date || params?.symbol),
  });
}

export function useCreateSignal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTradingSignalRequest) => createSignal(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['signals'] }),
  });
}

export function useUpdateSignal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateTradingSignalRequest }) => updateSignal(id, data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['signals'] }),
  });
}

export function useDeleteSignal() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteSignal(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['signals'] }),
  });
}
