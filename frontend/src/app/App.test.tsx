import { render, screen } from '@testing-library/react'
import { MemoryRouter } from 'react-router-dom'
import { describe, expect, it } from 'vitest'
import { AuthContext } from '../features/auth/AuthContext'
import { App } from './App'

describe('App', () => {
  it('renders the product search entry point', () => {
    render(
      <MemoryRouter>
        <App />
      </MemoryRouter>,
    )

    expect(screen.getByRole('heading', { name: /find it nearby/i })).toBeInTheDocument()
    expect(screen.getByRole('searchbox', { name: /search for a product/i })).toBeInTheDocument()
  })

  it('redirects signed-out users away from protected routes', () => {
    render(
      <AuthContext value={{ user: null, accessToken: null, loading: false, login: async () => {}, register: async () => {}, logout: async () => {} }}>
        <MemoryRouter initialEntries={['/account']}>
          <App />
        </MemoryRouter>
      </AuthContext>,
    )

    expect(screen.getByRole('heading', { name: /welcome back/i })).toBeInTheDocument()
  })
})
