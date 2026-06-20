import { useQuery } from '@tanstack/react-query';
import { getPortfolioSummary, getSectorAllocation, getStockPnL } from '@/api/endpoints';

export function usePortfolioSummary() {
  return useQuery({ queryKey: ['portfolio', 'summary'], queryFn: getPortfolioSummary });
}

export function useSectorAllocation() {
  return useQuery({ queryKey: ['portfolio', 'allocation'], queryFn: getSectorAllocation });
}

export function useStockPnL() {
  return useQuery({ queryKey: ['portfolio', 'pnl'], queryFn: getStockPnL });
}
