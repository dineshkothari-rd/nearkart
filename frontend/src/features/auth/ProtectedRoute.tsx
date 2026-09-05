import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'

export function ProtectedRoute() {
  const { loading, user } = useAuth()
  if (loading) return <p className="p-6" role="status">Restoring your session…</p>
  return user ? <Outlet /> : <Navigate replace to="/login" />
}
