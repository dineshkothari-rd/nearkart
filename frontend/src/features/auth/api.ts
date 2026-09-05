export type Role = 'CUSTOMER' | 'STORE_OWNER' | 'ADMIN'

export type User = {
  id: string
  email: string
  displayName: string
  roles: Role[]
}

export type AuthResponse = {
  accessToken: string
  expiresInSeconds: number
  user: User
}

type ApiResponse<T> = {
  success: boolean
  data: T
  message: string | null
}

const apiBase = import.meta.env.VITE_API_BASE_URL ?? 'http://localhost:8080/api/v1'

async function request<T>(path: string, init: RequestInit = {}) {
  const response = await fetch(`${apiBase}${path}`, {
    ...init,
    credentials: 'include',
    headers: { 'Content-Type': 'application/json', ...init.headers },
  })
  const body = (await response.json()) as ApiResponse<T>
  if (!response.ok) throw new Error(body.message ?? 'Request failed')
  return body.data
}

export type RegisterInput = {
  email: string
  password: string
  displayName: string
  accountType: 'CUSTOMER' | 'STORE_OWNER'
}

export const login = (email: string, password: string) =>
  request<AuthResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })

export const register = (input: RegisterInput) =>
  request<AuthResponse>('/auth/register', { method: 'POST', body: JSON.stringify(input) })

let refreshRequest: Promise<AuthResponse> | undefined

export function refresh() {
  refreshRequest ??= request<AuthResponse>('/auth/refresh', { method: 'POST' }).finally(
    () => (refreshRequest = undefined),
  )
  return refreshRequest
}

export const logout = () => request<void>('/auth/logout', { method: 'POST' })
