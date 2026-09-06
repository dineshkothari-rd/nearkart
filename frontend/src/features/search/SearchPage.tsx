import { useQuery } from '@tanstack/react-query'
import { useState, type FormEvent } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { nearbySearch } from './api'
import { useAuth } from '../auth/AuthContext'
import { trackDirections } from '../stores/api'

export function SearchPage() {
	const { accessToken } = useAuth()
  const [params, setParams] = useSearchParams()
  const q = params.get('q')?.trim() ?? ''
  const latitude = Number(params.get('latitude'))
  const longitude = Number(params.get('longitude'))
  const hasLocation = Number.isFinite(latitude) && Number.isFinite(longitude) && params.has('latitude')
  const [locationError, setLocationError] = useState('')
  const offers = useQuery({
    queryKey: ['nearby-search', q, latitude, longitude],
    queryFn: () => nearbySearch(q, latitude, longitude, accessToken ?? undefined),
    enabled: q.length >= 2 && hasLocation,
  })

  function useMyLocation() {
    setLocationError('')
    navigator.geolocation.getCurrentPosition(
      ({ coords }) => setParams({ q, latitude: String(coords.latitude), longitude: String(coords.longitude) }),
      () => setLocationError('Location was unavailable. Enter coordinates manually.'),
      { enableHighAccuracy: false, timeout: 10000, maximumAge: 300000 },
    )
  }

  function setManualLocation(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const data = new FormData(event.currentTarget)
    setParams({ q, latitude: String(data.get('latitude')), longitude: String(data.get('longitude')) })
  }

  return (
    <main className="mx-auto min-h-dvh w-full max-w-5xl px-4 py-5 sm:px-6 sm:py-8">
      <Link className="text-xl font-bold text-emerald-800" to="/">NearKart</Link>
      <h1 className="mt-8 break-words text-3xl font-bold sm:text-4xl">Nearby results for “{q}”</h1>
      {!hasLocation && (
        <section className="mt-8 rounded-2xl border bg-white p-4 sm:p-6">
          <h2 className="text-xl font-bold">Choose your location</h2>
          <button className="mt-4 min-h-12 w-full rounded-xl bg-emerald-700 px-4 font-semibold text-white sm:w-auto" onClick={useMyLocation} type="button">Use my location</button>
          <form className="mt-5 grid gap-3 sm:grid-cols-[1fr_1fr_auto]" onSubmit={setManualLocation}>
            <input aria-label="Latitude" className="min-h-12 min-w-0 rounded-xl border px-3" name="latitude" placeholder="Latitude" required type="number" min="-90" max="90" step="any" />
            <input aria-label="Longitude" className="min-h-12 min-w-0 rounded-xl border px-3" name="longitude" placeholder="Longitude" required type="number" min="-180" max="180" step="any" />
            <button className="min-h-12 rounded-xl border px-5 font-semibold">Search here</button>
          </form>
          {locationError && <p className="mt-3 text-red-700" role="alert">{locationError}</p>}
        </section>
      )}
      {offers.isLoading && <p className="mt-8" role="status">Finding nearby stores…</p>}
      {offers.error && <p className="mt-8 text-red-700" role="alert">{offers.error.message}</p>}
      {hasLocation && offers.data?.length === 0 && <p className="mt-8 text-stone-600">No matching stores found nearby.</p>}
      <ul className="mt-8 grid gap-4 md:grid-cols-2">{offers.data?.map((offer) => (
        <li className="min-w-0 rounded-2xl border bg-white p-5 shadow-sm" key={`${offer.storeId}-${offer.productId}-${offer.variant}`}>
          <div className="flex flex-col gap-2 sm:flex-row sm:items-start sm:justify-between">
            <div className="min-w-0"><h2 className="break-words text-xl font-bold"><Link className="hover:text-emerald-800" to={`/products/${offer.productId}`}>{offer.productName}</Link> <span className="font-normal text-stone-600">{offer.variant}</span></h2><Link className="mt-1 inline-block text-stone-700 hover:text-emerald-800" to={`/stores/${offer.storeId}`}>{offer.storeName}</Link></div>
            <p className="shrink-0 text-xl font-bold">₹{offer.amount}</p>
          </div>
          <div className="mt-4 flex flex-wrap gap-x-4 gap-y-2 text-sm text-stone-600"><span>{formatDistance(offer.distanceMeters)}</span><span>{offer.availability.replaceAll('_', ' ')}</span><span>{offer.freshness.replaceAll('_', ' ')}</span></div>
          <p className="mt-4 text-sm font-medium text-emerald-800">{offer.recommendation.reason}</p>
          <div className="mt-5 grid grid-cols-2 gap-2"><Link className="flex min-h-11 items-center justify-center rounded-xl border font-semibold" to={`/stores/${offer.storeId}`}>View store</Link><a className="flex min-h-11 items-center justify-center rounded-xl border font-semibold" href={`https://www.google.com/maps/search/?api=1&query=${offer.storeLatitude},${offer.storeLongitude}`} onClick={() => void trackDirections(offer.storeId).catch(() => undefined)} target="_blank" rel="noreferrer">Directions</a></div>
        </li>
      ))}</ul>
    </main>
  )
}

const formatDistance = (meters: number) => meters < 1000 ? `${meters} m away` : `${(meters / 1000).toFixed(1)} km away`
