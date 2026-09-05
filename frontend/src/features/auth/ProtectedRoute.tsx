import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from './AuthContext'
import type { Role } from './api'

export function ProtectedRoute({ role }: { role?: Role }) {
  const { loading, user } = useAuth()
  if (loading) return <p className="p-6" role="status">Restoring your session…</p>
  if (!user) return <Navigate replace to="/login" />
  return !role || user.roles.includes(role) ? <Outlet /> : <Navigate replace to="/account" />
}
