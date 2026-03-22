import { createContext, useContext, useState, useEffect, ReactNode } from 'react'
import { authApi } from '../api/authApi'
import type { LoginRequest, RegisterRequest, AuthResponse } from '../api/authApi'

interface AuthContextValue {
  token: string | null
  user: { email: string; firstName: string; lastName: string } | null
  login: (req: LoginRequest) => Promise<void>
  register: (req: RegisterRequest) => Promise<void>
  logout: () => void
  isAuthenticated: boolean
}

const AuthContext = createContext<AuthContextValue | null>(null)

function isTokenExpired(token: string): boolean {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(() => localStorage.getItem('token'))
  const [user, setUser] = useState<AuthContextValue['user']>(() => {
    const stored = localStorage.getItem('user')
    return stored ? JSON.parse(stored) : null
  })

  useEffect(() => {
    if (token && isTokenExpired(token)) {
      logout()
    }
  }, [])

  function saveAuth(res: AuthResponse) {
    setToken(res.token)
    const u = { email: res.email, firstName: res.firstName, lastName: res.lastName }
    setUser(u)
    localStorage.setItem('token', res.token)
    localStorage.setItem('user', JSON.stringify(u))
  }

  async function login(req: LoginRequest) {
    const res = await authApi.login(req)
    saveAuth(res)
  }

  async function register(req: RegisterRequest) {
    const res = await authApi.register(req)
    saveAuth(res)
  }

  function logout() {
    setToken(null)
    setUser(null)
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  return (
    <AuthContext.Provider value={{ token, user, login, register, logout, isAuthenticated: !!token }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
