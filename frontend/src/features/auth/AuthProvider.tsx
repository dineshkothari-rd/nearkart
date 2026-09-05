import { useEffect, useMemo, useState, type PropsWithChildren } from 'react'
import { AuthContext } from './AuthContext'
import * as authApi from './api'
import type { AuthResponse, RegisterInput } from './api'

export function AuthProvider({ children }: PropsWithChildren) {
  const [session, setSession] = useState<AuthResponse | null>(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    authApi
      .refresh()
      .then(setSession)
      .catch(() => setSession(null))
      .finally(() => setLoading(false))
  }, [])

  const value = useMemo(
    () => ({
      user: session?.user ?? null,
      accessToken: session?.accessToken ?? null,
      loading,
      login: async (email: string, password: string) => setSession(await authApi.login(email, password)),
      register: async (input: RegisterInput) => setSession(await authApi.register(input)),
      logout: async () => {
        await authApi.logout()
        setSession(null)
      },
    }),
    [loading, session],
  )

  return <AuthContext value={value}>{children}</AuthContext>
}
