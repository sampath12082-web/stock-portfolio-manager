export type Exchange = 'NSE' | 'BSE';
export type TransactionType = 'BUY' | 'SELL' | 'BONUS' | 'SPLIT' | 'DIVIDEND' | 'DEPOSIT' | 'WITHDRAWAL' | 'CHARGES';
export type SignalType = 'BUY_SIGNAL' | 'SELL_SIGNAL' | 'HOLD' | 'WATCH';
export type SignalStatus = 'ACTIVE' | 'EXECUTED' | 'EXPIRED' | 'CANCELLED';

export type TradeType = 'CNC' | 'MIS' | 'UNKNOWN';

export interface DashboardResponse {
  investedAmount: number;
  currentValue: number;
  unrealizedPnL: number;
  unrealizedPnLPercentage: number;
  totalDeposited: number;
}

export interface PortfolioSummaryResponse {
  totalHoldings: number;
  totalInvestment: number;
  currentValue: number;
  totalPnL: number;
  totalPnLPercentage: number;
  dayPnL: number;
  dayPnLPercentage: number;
}

export interface SectorAllocationResponse {
  sector: string;
  investedAmount: number;
  currentValue: number;
  percentage: number;
  holdingCount: number;
}

export interface StockPnLResponse {
  symbol: string;
  companyName: string;
  quantity: number;
  averageBuyPrice: number;
  currentPrice: number;
  investedAmount: number;
  currentValue: number;
  pnl: number;
  pnlPercentage: number;
}

export interface StockResponse {
  id: number;
  symbol: string;
  companyName: string;
  exchange: Exchange;
  sector: string;
  industry: string;
}

export interface HoldingResponse {
  id: number;
  symbol: string;
  quantity: number;
  averageBuyPrice: number;
  investedAmount: number;
  currentPrice: number | null;
  currentValue: number | null;
  pnl: number | null;
  pnlPercentage: number | null;
  dayChange: number | null;
  dayChangePercentage: number | null;
}

export interface TransactionResponse {
  id: number;
  symbol: string | null;
  quantity: number;
  price: number;
  totalAmount: number;
  transactionType: TransactionType;
  description: string | null;
  tradeDate: string | null;
  createdAt: string;
  tradeType: TradeType | null;
}

export interface TransactionAnalyticsResponse {
  totalBuyAmount: number;
  totalSellAmount: number;
  realizedGains: number;
  totalTransactions: number;
  buyCount: number;
  sellCount: number;
  mostTradedStock: string;
  transactionsByMonth: Record<string, number>;
  totalDeposits: number;
  totalWithdrawals: number;
  totalCharges: number;
  intradayBuyAmount: number;
  intradaySellAmount: number;
  intradayPnL: number;
  deliveryBuyAmount: number;
  deliverySellAmount: number;
  deliveryRealizedGains: number;
  intradayCount: number;
  deliveryCount: number;
}

export interface StockQuoteData {
  symbol: string;
  ltp: number;
  open: number;
  high: number;
  low: number;
  close: number;
  previousClose: number;
  volume: number;
  fetchedAt: string;
}

export interface PortfolioSnapshotResponse {
  id: number;
  snapshotDate: string;
  totalInvestment: number;
  currentValue: number;
  totalPnL: number;
  totalPnLPercentage: number;
  holdingCount: number;
  topGainer: string;
  topLoser: string;
}

export interface TradingSignalResponse {
  id: number;
  symbol: string;
  companyName: string;
  signalType: SignalType;
  targetPrice: number;
  stopLoss: number;
  currentPrice: number;
  rationale: string;
  signalDate: string;
  status: SignalStatus;
  notes: string;
  createdAt: string;
}

export interface StockLookupResponse {
  symbol: string;
  companyName: string;
  exchange: string;
  sector: string | null;
  industry: string | null;
  existsInDb: boolean;
}

export interface CreateStockRequest {
  symbol: string;
  companyName: string;
  exchange: Exchange;
  sector?: string;
  industry?: string;
}

export interface CreateHoldingRequest {
  symbol: string;
  quantity: number;
  averageBuyPrice: number;
}

export interface UpdateHoldingRequest {
  quantity?: number;
  averageBuyPrice?: number;
}

export interface CreateTransactionRequest {
  symbol?: string;
  quantity?: number;
  price: number;
  transactionType: TransactionType;
  description?: string;
}

export interface CreateTradingSignalRequest {
  symbol: string;
  signalType: SignalType;
  targetPrice?: number;
  stopLoss?: number;
  rationale?: string;
  signalDate?: string;
  notes?: string;
}

export interface UpdateTradingSignalRequest {
  status?: SignalStatus;
  targetPrice?: number;
  stopLoss?: number;
  rationale?: string;
  notes?: string;
}

export interface GrowwAccountResponse {
  userId: string;
  ucc: string;
  nseEnabled: boolean;
  bseEnabled: boolean;
  activeSegments: string[];
  availableCash: number;
  clearCash: number;
  marginUsed: number;
  todayPositions: GrowwPositionData[];
  todayOrders: GrowwOrderSummary[];
}

export interface GrowwPositionData {
  symbol: string;
  exchange: string;
  quantity: number;
  netPrice: number;
  realisedPnl: number;
  product: string;
}

export interface GrowwOrderSummary {
  symbol: string;
  transactionType: string;
  status: string;
  quantity: number;
  filledQuantity: number;
  price: number;
  avgFillPrice: number;
  exchange: string;
  tradeDate: string;
}

export interface GrowwSyncResponse {
  stocksCreated: number;
  holdingsCreated: number;
  holdingsUpdated: number;
  errors: string[];
}

export interface TransactionUploadResponse {
  transactionsCreated: number;
  stocksCreated: number;
  errors: string[];
}

// Mutual Funds
export type MfTransactionType = 'PURCHASE' | 'REDEMPTION' | 'SIP' | 'SWP' | 'STP' | 'DIVIDEND_REINVEST' | 'DIVIDEND_PAYOUT' | 'SWITCH_IN' | 'SWITCH_OUT';

export interface MfFundResponse {
  id: number;
  schemeCode: string;
  schemeName: string;
  fundHouse: string;
  isin: string;
  category: string;
  fundType: string;
  currentNav: number | null;
  navDate: string | null;
}

export interface MfHoldingResponse {
  id: number;
  schemeCode: string;
  schemeName: string;
  fundHouse: string;
  units: number;
  averageNav: number;
  investedAmount: number;
  currentNav: number | null;
  currentValue: number | null;
  pnl: number | null;
  pnlPercentage: number | null;
}

export interface MfTransactionResponse {
  id: number;
  schemeCode: string | null;
  schemeName: string | null;
  units: number;
  nav: number;
  amount: number;
  transactionType: MfTransactionType;
  description: string | null;
  folioNumber: string | null;
  tradeDate: string | null;
  createdAt: string;
}

export interface MfLookupResponse {
  schemeCode: string;
  schemeName: string;
  nav: number;
  navDate: string;
  existsInDb: boolean;
}

export interface CreateMfRequest {
  schemeCode: string;
  schemeName: string;
  fundHouse?: string;
  isin?: string;
  category?: string;
  fundType?: string;
}

export interface CreateMfHoldingRequest {
  schemeCode: string;
  units: number;
  averageNav: number;
}

export interface CreateMfTransactionRequest {
  schemeCode: string;
  units: number;
  nav: number;
  amount: number;
  transactionType: MfTransactionType;
  description?: string;
  tradeDate?: string;
  folioNumber?: string;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
}
