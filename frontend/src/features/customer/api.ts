import { apiRequest } from '../auth/api'
import type { Store } from '../stores/api'

export type Favorite = {
  id: string
  type: 'PRODUCT' | 'STORE'
  targetId: string
  name: string
  subtitle: string | null
  createdAt: string
}

export type SearchHistory = { id: string; query: string; createdAt: string }
export type Review = {
  id: string
  storeId: string
  displayName: string
  rating: number
  text: string
  status: 'PENDING' | 'APPROVED' | 'REJECTED'
  createdAt: string
}
export type Product = {
  id: string
  name: string
  brand: string | null
  category: string
  variants: { id: string; label: string; barcode: string | null }[]
}
export type StoreProduct = {
  listingId: string
  productId: string
  productName: string
  brand: string | null
  variantId: string
  variant: string
  amount: number
  currency: string
  availability: string
  observedAt: string
}

export const getProduct = (id: string) => apiRequest<Product>(`/products/${id}`)
export const getStore = (id: string) => apiRequest<Store>(`/stores/${id}`)
export const getStoreProducts = (id: string) => apiRequest<StoreProduct[]>(`/stores/${id}/products`)
export const getStoreReviews = (id: string) => apiRequest<Review[]>(`/stores/${id}/reviews`)
export const getFavorites = (token: string) => apiRequest<Favorite[]>('/me/favorites', {}, token)
export const addFavorite = (token: string, type: Favorite['type'], targetId: string) =>
  apiRequest<Favorite>('/favorites', { method: 'POST', body: JSON.stringify({ type, targetId }) }, token)
export const removeFavorite = (token: string, id: string) =>
  apiRequest<void>(`/favorites/${id}`, { method: 'DELETE' }, token)
export const getHistory = (token: string) => apiRequest<SearchHistory[]>('/me/search-history', {}, token)
export const clearHistory = (token: string) => apiRequest<void>('/me/search-history', { method: 'DELETE' }, token)
export const addReview = (token: string, storeId: string, rating: number, text: string) =>
  apiRequest<Review>('/reviews', { method: 'POST', body: JSON.stringify({ storeId, rating, text }) }, token)
export const addReport = (token: string, storeId: string, type: string, details: string, storeProductId?: string) =>
  apiRequest('/reports', { method: 'POST', body: JSON.stringify({ storeId, type, details, storeProductId }) }, token)
