import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getMfFunds, createMfFund, searchMfFunds, getMfHoldings, createMfHolding, getMfTransactions, createMfTransaction, refreshMfNav } from '@/api/endpoints';
import type { CreateMfRequest, CreateMfHoldingRequest, CreateMfTransactionRequest } from '@/api/types';

export function useMfFunds() {
  return useQuery({ queryKey: ['mf', 'funds'], queryFn: getMfFunds });
}

export function useMfSearch(query: string) {
  return useQuery({
    queryKey: ['mf', 'search', query],
    queryFn: () => searchMfFunds(query),
    enabled: query.length >= 3,
    staleTime: 60_000,
  });
}

export function useCreateMfFund() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateMfRequest) => createMfFund(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['mf'] }),
  });
}

export function useMfHoldings() {
  return useQuery({ queryKey: ['mf', 'holdings'], queryFn: getMfHoldings });
}

export function useCreateMfHolding() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateMfHoldingRequest) => createMfHolding(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['mf'] }),
  });
}

export function useMfTransactions() {
  return useQuery({ queryKey: ['mf', 'transactions'], queryFn: getMfTransactions });
}

export function useCreateMfTransaction() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: CreateMfTransactionRequest) => createMfTransaction(data),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['mf'] }),
  });
}

export function useRefreshMfNav() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => refreshMfNav(),
    onSuccess: () => qc.invalidateQueries({ queryKey: ['mf'] }),
  });
}
