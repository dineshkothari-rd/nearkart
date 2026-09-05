import { Link, Route, Routes } from 'react-router-dom'
import { AuthPage } from '../features/auth/AuthPage'
import { useAuth } from '../features/auth/AuthContext'
import { ProtectedRoute } from '../features/auth/ProtectedRoute'

function HomePage() {
  return (
    <main className="mx-auto flex min-h-screen max-w-5xl flex-col px-5 py-6">
      <nav className="flex items-center justify-between" aria-label="Primary navigation">
        <Link className="text-xl font-bold text-emerald-800" to="/">
          NearKart
        </Link>
        <div className="flex gap-4 text-sm font-semibold">
          <Link className="text-stone-700 hover:text-emerald-800" to="/login">Sign in</Link>
          <Link className="text-emerald-800" to="/register">Create account</Link>
        </div>
      </nav>

      <section className="my-auto max-w-3xl py-20">
        <p className="mb-4 font-semibold text-emerald-700">Local products, without delivery fees</p>
        <h1 className="text-5xl font-bold tracking-tight text-stone-950 sm:text-7xl">
          Find it nearby. Pay less. Skip delivery.
        </h1>
        <p className="mt-6 max-w-2xl text-lg text-stone-600">
          Compare nearby store prices, distance, availability, and how recently stock was updated.
        </p>
        <form className="mt-10 flex max-w-2xl flex-col gap-3 sm:flex-row" role="search">
          <label className="sr-only" htmlFor="product-search">
            Search for a product
          </label>
          <input
            className="min-h-12 flex-1 rounded-xl border border-stone-300 bg-white px-4 text-stone-950 shadow-sm outline-none focus:border-emerald-700 focus:ring-2 focus:ring-emerald-200"
            id="product-search"
            name="q"
            placeholder="Search for a product..."
            type="search"
          />
          <button
            className="min-h-12 rounded-xl bg-emerald-700 px-6 font-semibold text-white hover:bg-emerald-800 focus:outline-none focus:ring-2 focus:ring-emerald-500 focus:ring-offset-2"
            type="submit"
          >
            Search nearby
          </button>
        </form>
      </section>
    </main>
  )
}

function AccountPage() {
  const { user, logout } = useAuth()
  return (
    <main className="mx-auto min-h-screen max-w-3xl px-5 py-10">
      <Link className="text-xl font-bold text-emerald-800" to="/">NearKart</Link>
      <section className="mt-16 rounded-2xl border border-stone-200 bg-white p-6 shadow-sm">
        <p className="text-sm font-semibold uppercase tracking-wide text-emerald-700">Signed in</p>
        <h1 className="mt-2 text-3xl font-bold text-stone-950">Hello, {user?.displayName}</h1>
        <p className="mt-2 text-stone-600">{user?.email}</p>
        <button className="mt-8 rounded-xl border border-stone-300 px-4 py-2 font-semibold" onClick={() => void logout()} type="button">
          Sign out
        </button>
      </section>
    </main>
  )
}

export function App() {
  return (
    <Routes>
      <Route element={<HomePage />} path="/" />
      <Route element={<AuthPage mode="login" />} path="/login" />
      <Route element={<AuthPage mode="register" />} path="/register" />
      <Route element={<ProtectedRoute />}>
        <Route element={<AccountPage />} path="/account" />
      </Route>
    </Routes>
  )
}
