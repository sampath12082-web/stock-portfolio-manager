import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAllStocks, searchStocks, lookupStocks, createStock, deleteStock } from '@/api/endpoints';
import type { CreateStockRequest } from '@/api/types';

export function useStocks() {
  return useQuery({ queryKey: ['stocks'], queryFn: getAllStocks });
}

export function useSearchStocks(params: { query?: string; exchange?: string; sector?: string }) {
  return useQuery({
    queryKey: ['stocks', 'search', params],
    queryFn: () => searchStocks(params),
    enabled: !!(params.query || params.exchange || params.sector),
  });
}

export function useLookupStocks(query: string) {
  return useQuery({
    queryKey: ['stocks', 'lookup', query],
    queryFn: () => lookupStocks(query),
    enabled: query.length >= 2,
    staleTime: 60_000,
  });
}

export function useCreateStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateStockRequest) => createStock(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['stocks'] }),
  });
}

export function useDeleteStock() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => deleteStock(id),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['stocks'] }),
  });
}
