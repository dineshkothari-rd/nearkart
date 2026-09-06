import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import type { FormEvent, ReactNode } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../auth/AuthContext'
import { inputClass } from '../stores/StorePages'
import { listAdminStores } from '../stores/api'
import {
  createCategory, createProduct, createVariant, getAudits, getCategories, getDashboard, getProducts,
  getReports, getReviews, getUsers, setProductStatus, setReportStatus, setReviewStatus, setUserStatus,
} from './api'

const button = 'min-h-11 rounded-lg border border-stone-300 px-3 py-2 text-sm font-semibold disabled:opacity-50'
const primary = `${button} border-emerald-700 bg-emerald-700 text-white`

export function AdminPage() {
  const { accessToken, user } = useAuth()
  const token = accessToken!
  const queryClient = useQueryClient()
  const dashboard = useQuery({ queryKey: ['admin-dashboard'], queryFn: () => getDashboard(token) })
  const users = useQuery({ queryKey: ['admin-users'], queryFn: () => getUsers(token) })
  const stores = useQuery({ queryKey: ['admin-stores'], queryFn: () => listAdminStores(token) })
  const reviews = useQuery({ queryKey: ['admin-reviews'], queryFn: () => getReviews(token) })
  const reports = useQuery({ queryKey: ['admin-reports'], queryFn: () => getReports(token) })
  const audits = useQuery({ queryKey: ['admin-audits'], queryFn: () => getAudits(token) })
  const categories = useQuery({ queryKey: ['admin-categories'], queryFn: () => getCategories(token) })
  const products = useQuery({ queryKey: ['admin-products'], queryFn: () => getProducts(token) })
  const action = useMutation({ mutationFn: (run: () => Promise<unknown>) => run(), onSuccess: () => void queryClient.invalidateQueries({ queryKey: ['admin'] }) })
  const errors = [dashboard, users, stores, reviews, reports, audits, categories, products].map((query) => query.error?.message).filter(Boolean)

  function form(event: FormEvent<HTMLFormElement>, run: (data: FormData) => Promise<unknown>) {
    event.preventDefault()
    const target = event.currentTarget
    action.mutate(() => run(new FormData(target)), { onSuccess: () => target.reset() })
  }

  const metrics = dashboard.data ? [
    ['Users', dashboard.data.users], ['Stores', dashboard.data.stores], ['Pending stores', dashboard.data.pendingStores],
    ['Products', dashboard.data.products], ['Pending reviews', dashboard.data.pendingReviews],
    ['Open reports', dashboard.data.openReports], ['30-day searches', dashboard.data.searches],
  ] : []

  return <main className="mx-auto min-h-dvh w-full max-w-6xl px-4 py-6 sm:px-6 sm:py-10">
    <Link className="font-bold text-emerald-800" to="/account">← Account</Link>
    <div className="mt-6 flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
      <div><p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">Operations</p><h1 className="text-3xl font-bold">Admin dashboard</h1></div>
      <Link className={primary} to="/admin/stores">Review store approvals</Link>
    </div>
    <nav aria-label="Admin sections" className="mt-6 flex gap-2 overflow-x-auto pb-2 text-sm font-semibold">
      {['overview', 'users', 'stores', 'catalog', 'reviews', 'reports', 'audit'].map((name) => <a className="shrink-0 rounded-full border bg-white px-3 py-2 capitalize" href={`#${name}`} key={name}>{name}</a>)}
    </nav>
    {errors.length > 0 && <p className="mt-4 text-red-700" role="alert">{errors[0]}</p>}
    {action.error && <p className="mt-4 text-red-700" role="alert">{action.error.message}</p>}

    <Section id="overview" title="Overview"><div className="grid grid-cols-2 gap-3 sm:grid-cols-4">{metrics.map(([label, value]) => <article className="rounded-xl border bg-white p-4" key={label}><p className="text-sm text-stone-600">{label}</p><p className="mt-1 text-2xl font-bold">{value}</p></article>)}</div></Section>

    <Section id="users" title="Users"><Cards empty="No users found.">{users.data?.map((item) => <Card key={item.id} title={item.displayName} aside={item.status}>
      <p className="break-all text-sm text-stone-600">{item.email}</p><p className="mt-1 text-sm">{item.roles}</p>
      <button className={`${button} mt-3`} disabled={action.isPending || item.id === user?.id} onClick={() => action.mutate(() => setUserStatus(token, item.id, item.status === 'ACTIVE' ? 'SUSPENDED' : 'ACTIVE'))} type="button">{item.status === 'ACTIVE' ? 'Suspend' : 'Activate'}</button>
    </Card>)}</Cards></Section>

    <Section id="stores" title="Stores"><Cards empty="No stores found.">{stores.data?.map((item) => <Card key={item.id} title={item.name} aside={item.status}><p className="text-sm text-stone-600">{item.location.locality}, {item.location.city}</p></Card>)}</Cards></Section>

    <Section id="catalog" title="Catalog">
      <div className="grid gap-3 lg:grid-cols-3">
        <form className="grid content-start gap-3 rounded-xl border bg-white p-4" onSubmit={(event) => form(event, (data) => createCategory(token, String(data.get('name'))))}><h3 className="font-bold">Add category</h3><input className={inputClass} name="name" placeholder="Category name" required maxLength={100} /><button className={primary} disabled={action.isPending}>Add category</button></form>
        <form className="grid content-start gap-3 rounded-xl border bg-white p-4" onSubmit={(event) => form(event, (data) => createProduct(token, String(data.get('categoryId')), String(data.get('name')), String(data.get('brand'))))}><h3 className="font-bold">Add product</h3><select className={inputClass} name="categoryId" required><option value="">Choose category</option>{categories.data?.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><input className={inputClass} name="name" placeholder="Product name" required maxLength={180} /><input className={inputClass} name="brand" placeholder="Brand (optional)" maxLength={120} /><button className={primary} disabled={action.isPending}>Add product</button></form>
        <form className="grid content-start gap-3 rounded-xl border bg-white p-4" onSubmit={(event) => form(event, (data) => createVariant(token, String(data.get('productId')), String(data.get('label')), String(data.get('barcode'))))}><h3 className="font-bold">Add variant</h3><select className={inputClass} name="productId" required><option value="">Choose product</option>{products.data?.map((item) => <option key={item.id} value={item.id}>{item.name}</option>)}</select><input className={inputClass} name="label" placeholder="Variant, e.g. 500 g" required maxLength={120} /><input className={inputClass} name="barcode" placeholder="Barcode (optional)" maxLength={64} /><button className={primary} disabled={action.isPending}>Add variant</button></form>
      </div>
      <Cards empty="No products found.">{products.data?.map((item) => <Card key={item.id} title={item.name} aside={item.status}><p className="text-sm text-stone-600">{item.brand || 'No brand'} · {item.category} · {item.variants} variants</p><button className={`${button} mt-3`} disabled={action.isPending} onClick={() => action.mutate(() => setProductStatus(token, item.id, item.status === 'ACTIVE' ? 'INACTIVE' : 'ACTIVE'))} type="button">Mark {item.status === 'ACTIVE' ? 'inactive' : 'active'}</button></Card>)}</Cards>
    </Section>

    <Section id="reviews" title="Pending reviews"><Cards empty="No reviews waiting.">{reviews.data?.map((item) => <Card key={item.id} title={`${item.storeName} · ${item.rating}/5`} aside={item.displayName}><p className="text-sm text-stone-700">{item.text}</p><Actions><button className={primary} onClick={() => action.mutate(() => setReviewStatus(token, item.id, 'APPROVED'))} type="button">Approve</button><button className={button} onClick={() => action.mutate(() => setReviewStatus(token, item.id, 'REJECTED'))} type="button">Reject</button></Actions></Card>)}</Cards></Section>
    <Section id="reports" title="Open reports"><Cards empty="No open reports.">{reports.data?.map((item) => <Card key={item.id} title={item.type.replaceAll('_', ' ')} aside={item.storeName}><p className="text-sm text-stone-700">{item.details || 'No additional details'}</p><Actions><button className={primary} onClick={() => action.mutate(() => setReportStatus(token, item.id, 'RESOLVED'))} type="button">Resolve</button><button className={button} onClick={() => action.mutate(() => setReportStatus(token, item.id, 'DISMISSED'))} type="button">Dismiss</button></Actions></Card>)}</Cards></Section>
    <Section id="audit" title="Audit logs"><Cards empty="No audit entries.">{audits.data?.map((item) => <Card key={item.id} title={item.action.replaceAll('_', ' ')} aside={new Date(item.createdAt).toLocaleString()}><p className="break-all text-sm text-stone-600">{item.actor || 'System'} · {item.targetType} · {item.targetId}</p></Card>)}</Cards></Section>
  </main>
}

function Section({ id, title, children }: { id: string; title: string; children: ReactNode }) { return <section className="scroll-mt-4 pt-10" id={id}><h2 className="mb-4 text-xl font-bold sm:text-2xl">{title}</h2>{children}</section> }
function Cards({ children, empty }: { children?: ReactNode[]; empty: string }) { return children?.length ? <div className="grid gap-3 sm:grid-cols-2">{children}</div> : <p className="text-stone-600">{empty}</p> }
function Card({ title, aside, children }: { title: string; aside: string; children: ReactNode }) { return <article className="rounded-xl border bg-white p-4"><div className="flex flex-col gap-1 sm:flex-row sm:justify-between"><h3 className="break-words font-bold">{title}</h3><span className="shrink-0 text-sm font-semibold text-emerald-800">{aside}</span></div><div className="mt-2">{children}</div></article> }
function Actions({ children }: { children: ReactNode }) { return <div className="mt-3 flex flex-wrap gap-2">{children}</div> }
