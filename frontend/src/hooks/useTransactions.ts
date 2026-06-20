import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getTransactions, createTransaction, getTransactionAnalytics, syncGrowwOrders } from '@/api/endpoints';
import type { CreateTransactionRequest } from '@/api/types';

export function useTransactions(params?: { symbol?: string; type?: string; from?: string; to?: string }) {
  return useQuery({
    queryKey: ['transactions', params],
    queryFn: () => getTransactions(params),
  });
}

export function useTransactionAnalytics() {
  return useQuery({ queryKey: ['transactions', 'analytics'], queryFn: getTransactionAnalytics });
}

export function useSyncGrowwOrders() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => syncGrowwOrders(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['transactions'] });
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useCreateTransaction() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateTransactionRequest) => createTransaction(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['transactions'] });
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
