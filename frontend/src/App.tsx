import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import DashboardPage from './pages/DashboardPage'
import HoldingsPage from './pages/HoldingsPage'
import TransactionsPage from './pages/TransactionsPage'
import StocksPage from './pages/StocksPage'
import MutualFundsPage from './pages/MutualFundsPage'
import PerformancePage from './pages/PerformancePage'

export default function App() {
  return (
    <Routes>
      <Route element={<AppLayout />}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/holdings" element={<HoldingsPage />} />
        <Route path="/transactions" element={<TransactionsPage />} />
        <Route path="/stocks" element={<StocksPage />} />
        <Route path="/mutual-funds" element={<MutualFundsPage />} />
        <Route path="/performance" element={<PerformancePage />} />
      </Route>
    </Routes>
  )
}
