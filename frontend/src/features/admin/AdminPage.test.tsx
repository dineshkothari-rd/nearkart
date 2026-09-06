import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { MemoryRouter } from 'react-router-dom'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { AuthContext } from '../auth/AuthContext'
import { AdminPage } from './AdminPage'

const page = (content: unknown[]) => ({ content, page: 0, size: 50, totalElements: content.length, totalPages: 1 })

describe('AdminPage', () => {
  afterEach(() => vi.restoreAllMocks())

  it('loads operations and can suspend a user', async () => {
    const fetch = vi.spyOn(globalThis, 'fetch').mockImplementation(async (input) => {
      const path = String(input)
      let data: unknown = null
      if (path.includes('/admin/dashboard')) data = { users: 2, stores: 1, pendingStores: 0, products: 1, pendingReviews: 0, openReports: 0, searches: 3 }
      else if (path.includes('/admin/users')) data = page([{ id: 'customer-1', email: 'customer@example.com', displayName: 'Customer', status: 'ACTIVE', roles: 'CUSTOMER', createdAt: '2026-09-06T00:00:00Z' }])
      else if (path.includes('/admin/stores')) data = page([])
      else if (path.includes('/admin/reviews')) data = page([])
      else if (path.includes('/admin/reports')) data = page([])
      else if (path.includes('/admin/audit-logs')) data = page([])
      else if (path.includes('/admin/catalog/categories')) data = []
      else if (path.includes('/admin/catalog/products')) data = page([])
      return new Response(JSON.stringify({ success: true, data, message: null, errors: [] }), { status: 200, headers: { 'Content-Type': 'application/json' } })
    })
    const queryClient = new QueryClient({ defaultOptions: { queries: { retry: false } } })

    render(<QueryClientProvider client={queryClient}><AuthContext value={{ user: { id: 'admin-1', email: 'admin@example.com', displayName: 'Admin', roles: ['ADMIN'] }, accessToken: 'token', loading: false, login: async () => {}, register: async () => {}, logout: async () => {} }}><MemoryRouter><AdminPage /></MemoryRouter></AuthContext></QueryClientProvider>)

    expect(await screen.findByRole('heading', { name: 'Customer' })).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Suspend' }))
    await waitFor(() => expect(fetch).toHaveBeenCalledWith(expect.stringContaining('/admin/users/customer-1'), expect.objectContaining({ method: 'PATCH' })))
  })
})
