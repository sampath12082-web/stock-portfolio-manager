import { lazy, Suspense } from 'react'
import { Routes, Route } from 'react-router-dom'
import AppLayout from './components/layout/AppLayout'
import { AuthGuard } from './auth/AuthGuard'
import LoadingSpinner from './components/ui/LoadingSpinner'

const LoginPage = lazy(() => import('./pages/LoginPage'))
const RegisterPage = lazy(() => import('./pages/RegisterPage'))
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage'))
const DashboardPage = lazy(() => import('./pages/DashboardPage'))
const HoldingsPage = lazy(() => import('./pages/HoldingsPage'))
const TransactionsPage = lazy(() => import('./pages/TransactionsPage'))
const StocksPage = lazy(() => import('./pages/StocksPage'))
const MutualFundsPage = lazy(() => import('./pages/MutualFundsPage'))
const PerformancePage = lazy(() => import('./pages/PerformancePage'))
const ProfilePage = lazy(() => import('./pages/ProfilePage'))
const AdminUsersPage = lazy(() => import('./pages/AdminUsersPage'))
const AdminTicketsPage = lazy(() => import('./pages/AdminTicketsPage'))
const HelpPage = lazy(() => import('./pages/HelpPage'))
const AiSearchPage = lazy(() => import('./pages/AiSearchPage'))

export default function App() {
  return (
    <Suspense fallback={<LoadingSpinner />}>
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
          <Route path="/ai-search" element={<AiSearchPage />} />
          <Route path="/help" element={<HelpPage />} />
          <Route path="/admin/users" element={<AdminUsersPage />} />
          <Route path="/admin/tickets" element={<AdminTicketsPage />} />
        </Route>
      </Routes>
    </Suspense>
  )
}
