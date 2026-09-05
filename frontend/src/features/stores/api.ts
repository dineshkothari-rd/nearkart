import { apiRequest } from '../auth/api'

export type Store = {
  id: string
  name: string
  description: string | null
  phone: string
  timezone: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED' | 'SUSPENDED'
  approvalReason: string | null
  hours: { weekday: number; opensAt: string | null; closesAt: string | null; closed: boolean }[]
  location: {
    addressLine: string
    locality: string
    city: string
    state: string
    postalCode: string
    latitude: number
    longitude: number
  }
}

type Page<T> = { content: T[] }

export type CreateStoreInput = {
  name: string
  description?: string
  phone: string
  timezone: string
  location: Store['location']
}

export const listOwnedStores = (token: string) =>
  apiRequest<Page<Store>>('/owner/stores', {}, token).then((page) => page.content)

export const createStore = (token: string, input: CreateStoreInput) =>
  apiRequest<Store>('/owner/stores', { method: 'POST', body: JSON.stringify(input) }, token)

export const listPendingStores = (token: string) =>
  apiRequest<Page<Store>>('/admin/stores', {}, token).then((page) => page.content)

export const decideStore = (token: string, id: string, decision: 'APPROVE' | 'REJECT', reason?: string) =>
  apiRequest<Store>(
    `/admin/stores/${id}/approval`,
    { method: 'PATCH', body: JSON.stringify({ decision, reason }) },
    token,
  )
