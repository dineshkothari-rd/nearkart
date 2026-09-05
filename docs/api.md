# API contract outline

Base path: `/api/v1`. JSON uses camelCase and ISO-8601 UTC timestamps. Collections default to 20 and cap at 100 items.

## Envelope

```json
{"success":true,"data":{},"message":null,"meta":{"page":0,"size":20,"totalElements":1}}
```

Errors use `success:false`, `data:null`, a safe `message`, `errors` with field/code details, and a request ID. Status codes remain authoritative.

## Public/customer

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/auth/register` | Create customer or store-owner account |
| `POST` | `/auth/login` | Issue access and refresh credentials |
| `POST` | `/auth/refresh` | Rotate the HttpOnly refresh cookie |
| `POST` | `/auth/logout` | Revoke and clear the refresh cookie |
| `GET` | `/search` | Product suggestions by `q`, category, page, size |
| `GET` | `/search/nearby` | Offers by `q`, latitude, longitude, radiusKm, filters, sort, page, size |
| `GET` | `/products/{id}` | Product and variants |
| `GET` | `/products/{id}/nearby-stores` | Comparable nearby offers |
| `GET` | `/stores/{id}` | Approved store detail and hours (implemented) |
| `GET` | `/stores/{id}/products` | Paginated active listings |
| `GET` | `/stores/{id}/reviews` | Paginated approved reviews |
| `GET` | `/me` | Authenticated profile (implemented in Phase 2) |
| `GET/POST/DELETE` | `/me/favorites[/{id}]` | Product/store favorites |
| `GET/DELETE` | `/me/search-history` | Read or clear history |
| `POST` | `/reviews` | One review per customer/store |
| `POST` | `/reports` | Report incorrect data |

Nearby result item:

```json
{
  "product":{"id":"uuid","name":"Amul Butter","variant":"500 g"},
  "store":{"id":"uuid","name":"Sharma General Store","rating":4.4},
  "price":{"amount":"285.00","currency":"INR"},
  "availability":"AVAILABLE",
  "inventory":{"observedAt":"2026-09-05T08:00:00Z","freshness":"FRESH"},
  "distanceMeters":350,
  "recommendation":{"score":0.86,"reason":"Nearby with fresh availability"}
}
```

## Store owner

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/owner/stores` | Submit store for approval |
| `GET` | `/owner/stores` | List owned stores |
| `GET/PATCH` | `/owner/stores/{id}` | Read/update owned store |
| `PUT` | `/owner/stores/{id}/hours` | Replace validated hours |
| `GET/POST` | `/owner/stores/{id}/products` | List/add catalog link |
| `PATCH` | `/owner/store-products/{id}/inventory` | Quantity/state/source with expected version |
| `PATCH` | `/owner/store-products/{id}/price` | Price/currency with expected version |
| `GET` | `/owner/stores/{id}/analytics` | Bounded summary |

Store submission, listing, profile updates, and hours are implemented. All `{id}` resources are authorized against the authenticated owner; client-supplied owner/user IDs are ignored.

## Admin

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/admin/dashboard` | Core metrics |
| `GET` | `/admin/stores` | Approval/moderation queue (implemented) |
| `PATCH` | `/admin/stores/{id}/approval` | Approve/reject with reason (implemented) |
| `GET/PATCH` | `/admin/users[/{id}]` | Search/suspend users |
| `GET/PATCH` | `/admin/reviews[/{id}]` | Moderation |
| `GET/PATCH` | `/admin/reports[/{id}]` | Resolution workflow |
| `GET` | `/admin/audit-logs` | Filtered paginated audit trail |

## Validation and status semantics

- `400` malformed input; `401` missing/invalid auth; `403` role/ownership failure; `404` hidden or absent resource; `409` duplicate/stale version; `422` valid syntax but invalid domain transition; `429` rate limit.
- Coordinates: latitude `[-90,90]`, longitude `[-180,180]`; radius `0.1–25 km`; prices positive; quantities nonnegative.
- Search query is trimmed, normalized, 2–120 characters; blank queries are rejected.
