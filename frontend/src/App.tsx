import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import { AuthGuard } from './auth/AuthGuard'
import LoginPage from './pages/LoginPage'
import RegisterPage from './pages/RegisterPage'
import ForgotPasswordPage from './pages/ForgotPasswordPage'
import DashboardPage from './pages/DashboardPage'
import HoldingsPage from './pages/HoldingsPage'
import TransactionsPage from './pages/TransactionsPage'
import StocksPage from './pages/StocksPage'
import MutualFundsPage from './pages/MutualFundsPage'
import PerformancePage from './pages/PerformancePage'
import ProfilePage from './pages/ProfilePage'
import AdminUsersPage from './pages/AdminUsersPage'

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route path="/forgot-password" element={<ForgotPasswordPage />} />
      <Route element={<AuthGuard><AppLayout /></AuthGuard>}>
        <Route path="/" element={<DashboardPage />} />
        <Route path="/holdings" element={<HoldingsPage />} />
        <Route path="/transactions" element={<TransactionsPage />} />
        <Route path="/stocks" element={<StocksPage />} />
        <Route path="/mutual-funds" element={<MutualFundsPage />} />
        <Route path="/performance" element={<PerformancePage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/admin/users" element={<AdminUsersPage />} />
      </Route>
    </Routes>
  )
}
