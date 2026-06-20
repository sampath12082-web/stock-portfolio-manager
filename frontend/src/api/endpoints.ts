import client from './client';
import type {
  DashboardResponse, PortfolioSummaryResponse, SectorAllocationResponse, StockPnLResponse,
  StockResponse, StockLookupResponse, CreateStockRequest, GrowwAccountResponse, GrowwSyncResponse, TransactionUploadResponse,
  MfFundResponse, MfLookupResponse, MfHoldingResponse, MfTransactionResponse,
  CreateMfRequest, CreateMfHoldingRequest, CreateMfTransactionRequest,
  HoldingResponse, CreateHoldingRequest, UpdateHoldingRequest,
  TransactionResponse, CreateTransactionRequest, TransactionAnalyticsResponse,
  StockQuoteData,
  PortfolioSnapshotResponse,
  TradingSignalResponse, CreateTradingSignalRequest, UpdateTradingSignalRequest,
} from './types';

export const getDashboard = () => client.get<DashboardResponse>('/dashboard').then(r => r.data);

export const getPortfolioSummary = () => client.get<PortfolioSummaryResponse>('/portfolio/summary').then(r => r.data);
export const getSectorAllocation = () => client.get<SectorAllocationResponse[]>('/portfolio/allocation').then(r => r.data);
export const getStockPnL = () => client.get<StockPnLResponse[]>('/portfolio/pnl').then(r => r.data);

export const createStock = (data: CreateStockRequest) => client.post<StockResponse>('/stocks', data).then(r => r.data);
export const getStock = (symbol: string) => client.get<StockResponse>(`/stocks/${symbol}`).then(r => r.data);
export const getAllStocks = () => client.get<StockResponse[]>('/stocks').then(r => r.data);
export const searchStocks = (params: { query?: string; exchange?: string; sector?: string }) =>
  client.get<StockResponse[]>('/stocks/search', { params }).then(r => r.data);
export const lookupStocks = (query: string) =>
  client.get<StockLookupResponse[]>('/stocks/lookup', { params: { query } }).then(r => r.data);

export const createHolding = (data: CreateHoldingRequest) => client.post<HoldingResponse>('/holdings', data).then(r => r.data);
export const getAllHoldings = () => client.get<HoldingResponse[]>('/holdings').then(r => r.data);
export const updateHolding = (id: number, data: UpdateHoldingRequest) =>
  client.put<HoldingResponse>(`/holdings/${id}`, data).then(r => r.data);

export const createTransaction = (data: CreateTransactionRequest) =>
  client.post<TransactionResponse>('/transactions', data).then(r => r.data);
export const getTransactions = (params?: { symbol?: string; type?: string; from?: string; to?: string }) =>
  client.get<TransactionResponse[]>('/transactions', { params }).then(r => r.data);
export const getTransactionAnalytics = () =>
  client.get<TransactionAnalyticsResponse>('/transactions/analytics').then(r => r.data);

export const getQuote = (symbol: string) => client.get<StockQuoteData>(`/quotes/${symbol}`).then(r => r.data);
export const getAllQuotes = () => client.get<Record<string, StockQuoteData>>('/quotes').then(r => r.data);
export const refreshQuotes = () => client.post<void>('/quotes/refresh');

export const getPerformanceHistory = (from: string, to: string) =>
  client.get<PortfolioSnapshotResponse[]>('/performance/history', { params: { from, to } }).then(r => r.data);
export const getRecentPerformance = (days = 30) =>
  client.get<PortfolioSnapshotResponse[]>('/performance/recent', { params: { days } }).then(r => r.data);
export const getTodaySnapshot = () => client.get<PortfolioSnapshotResponse>('/performance/today').then(r => r.data);
export const captureSnapshot = () => client.post<PortfolioSnapshotResponse>('/performance/snapshot').then(r => r.data);

export const createSignal = (data: CreateTradingSignalRequest) =>
  client.post<TradingSignalResponse>('/signals', data).then(r => r.data);
export const updateSignal = (id: number, data: UpdateTradingSignalRequest) =>
  client.put<TradingSignalResponse>(`/signals/${id}`, data).then(r => r.data);
export const getTodaySignals = () => client.get<TradingSignalResponse[]>('/signals/today').then(r => r.data);
export const getActiveSignals = () => client.get<TradingSignalResponse[]>('/signals/active').then(r => r.data);
export const getSignals = (params?: { date?: string; symbol?: string }) =>
  client.get<TradingSignalResponse[]>('/signals', { params }).then(r => r.data);
export const deleteSignal = (id: number) => client.delete<void>(`/signals/${id}`);

export const syncGroww = () => client.post<GrowwSyncResponse>('/groww/sync').then(r => r.data);
export const getGrowwAccount = () => client.get<GrowwAccountResponse>('/groww/account').then(r => r.data);
export const syncGrowwOrders = () => client.post<GrowwSyncResponse>('/groww/sync-orders').then(r => r.data);
export const deleteStock = (id: number) => client.delete<void>(`/stocks/${id}`);
export const uploadTransactionPdf = (file: File) => {
  const fd = new FormData();
  fd.append('file', file);
  return client.post<TransactionUploadResponse>('/transactions/upload', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  }).then(r => r.data);
};
export const analyzeHoldings = () => client.post<TradingSignalResponse[]>('/signals/analyze').then(r => r.data);
export const getRecommendations = () => client.get<TradingSignalResponse[]>('/signals/recommendations').then(r => r.data);

// Mutual Funds
export const getMfFunds = () => client.get<MfFundResponse[]>('/mf/funds').then(r => r.data);
export const createMfFund = (data: CreateMfRequest) => client.post<MfFundResponse>('/mf/funds', data).then(r => r.data);
export const searchMfFunds = (query: string) => client.get<MfLookupResponse[]>('/mf/funds/search', { params: { query } }).then(r => r.data);
export const deleteMfFund = (id: number) => client.delete<void>(`/mf/funds/${id}`);
export const refreshMfNav = () => client.post<void>('/mf/funds/refresh-nav');
export const getMfHoldings = () => client.get<MfHoldingResponse[]>('/mf/holdings').then(r => r.data);
export const createMfHolding = (data: CreateMfHoldingRequest) => client.post<MfHoldingResponse>('/mf/holdings', data).then(r => r.data);
export const getMfTransactions = () => client.get<MfTransactionResponse[]>('/mf/transactions').then(r => r.data);
export const createMfTransaction = (data: CreateMfTransactionRequest) => client.post<MfTransactionResponse>('/mf/transactions', data).then(r => r.data);
