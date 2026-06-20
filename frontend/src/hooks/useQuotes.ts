import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getQuote, getAllQuotes, refreshQuotes } from '@/api/endpoints';

export function useQuote(symbol: string) {
  return useQuery({
    queryKey: ['quotes', symbol],
    queryFn: () => getQuote(symbol),
    enabled: !!symbol,
  });
}

export function useAllQuotes() {
  return useQuery({ queryKey: ['quotes'], queryFn: getAllQuotes });
}

export function useRefreshQuotes() {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: () => refreshQuotes(),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['quotes'] });
      qc.invalidateQueries({ queryKey: ['holdings'] });
      qc.invalidateQueries({ queryKey: ['portfolio'] });
      qc.invalidateQueries({ queryKey: ['dashboard'] });
    },
  });
}
