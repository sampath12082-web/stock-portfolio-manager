import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllHoldings, createHolding, updateHolding, syncGroww, getGrowwAccount } from '@/api/endpoints';
import type { CreateHoldingRequest, UpdateHoldingRequest } from '@/api/types';

export function useHoldings() {
  return useQuery({ queryKey: ['holdings'], queryFn: getAllHoldings });
}

export function useCreateHolding() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateHoldingRequest) => createHolding(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useGrowwAccount() {
  return useQuery({
    queryKey: ['groww', 'account'],
    queryFn: getGrowwAccount,
    retry: false,
  });
}

export function useSyncGroww() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => syncGroww(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['stocks'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}

export function useUpdateHolding() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateHoldingRequest }) => updateHolding(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
