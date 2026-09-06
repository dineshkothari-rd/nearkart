import { apiRequest } from '../auth/api'

type Page<T> = { content: T[]; totalElements: number }
export type Dashboard = { users: number; stores: number; pendingStores: number; products: number; pendingReviews: number; openReports: number; searches: number }
export type AdminUser = { id: string; email: string; displayName: string; status: 'ACTIVE' | 'SUSPENDED'; roles: string; createdAt: string }
export type AdminReview = { id: string; storeId: string; storeName: string; displayName: string; rating: number; text: string; status: string; createdAt: string }
export type AdminReport = { id: string; storeId: string; storeName: string; displayName: string; type: string; details: string | null; status: string; createdAt: string }
export type AuditEntry = { id: string; actor: string | null; action: string; targetType: string; targetId: string; metadata: string; createdAt: string }
export type Category = { id: string; name: string; slug: string }
export type Product = { id: string; categoryId: string; category: string; name: string; brand: string | null; status: 'ACTIVE' | 'INACTIVE'; variants: number }

export const getDashboard = (token: string) => apiRequest<Dashboard>('/admin/dashboard', {}, token)
export const getUsers = (token: string) => apiRequest<Page<AdminUser>>('/admin/users', {}, token).then((page) => page.content)
export const setUserStatus = (token: string, id: string, status: AdminUser['status']) => apiRequest<void>(`/admin/users/${id}`, { method: 'PATCH', body: JSON.stringify({ status }) }, token)
export const getReviews = (token: string) => apiRequest<Page<AdminReview>>('/admin/reviews?status=PENDING', {}, token).then((page) => page.content)
export const setReviewStatus = (token: string, id: string, status: 'APPROVED' | 'REJECTED') => apiRequest<void>(`/admin/reviews/${id}`, { method: 'PATCH', body: JSON.stringify({ status }) }, token)
export const getReports = (token: string) => apiRequest<Page<AdminReport>>('/admin/reports?status=OPEN', {}, token).then((page) => page.content)
export const setReportStatus = (token: string, id: string, status: 'RESOLVED' | 'DISMISSED') => apiRequest<void>(`/admin/reports/${id}`, { method: 'PATCH', body: JSON.stringify({ status }) }, token)
export const getAudits = (token: string) => apiRequest<Page<AuditEntry>>('/admin/audit-logs', {}, token).then((page) => page.content)
export const getCategories = (token: string) => apiRequest<Category[]>('/admin/catalog/categories', {}, token)
export const getProducts = (token: string) => apiRequest<Page<Product>>('/admin/catalog/products', {}, token).then((page) => page.content)
export const setProductStatus = (token: string, id: string, status: Product['status']) => apiRequest<void>(`/admin/catalog/products/${id}`, { method: 'PATCH', body: JSON.stringify({ status }) }, token)
export const createCategory = (token: string, name: string) => apiRequest<Category>('/admin/catalog/categories', { method: 'POST', body: JSON.stringify({ name }) }, token)
export const createProduct = (token: string, categoryId: string, name: string, brand: string) => apiRequest('/admin/catalog/products', { method: 'POST', body: JSON.stringify({ categoryId, name, brand: brand || null }) }, token)
export const createVariant = (token: string, productId: string, label: string, barcode: string) => apiRequest('/admin/catalog/variants', { method: 'POST', body: JSON.stringify({ productId, label, barcode: barcode || null }) }, token)
