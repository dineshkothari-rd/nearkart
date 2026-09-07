# Security strategy

## Authentication

- BCrypt password hashes; credentials are never logged.
- Short-lived signed access JWT (15 minutes by default) containing subject and server-derived roles.
- Rotating opaque refresh tokens (30 days by default), stored as SHA-256 hashes and revocable per session; reuse revokes the token family.
- Browser refresh token in an `HttpOnly`, `SameSite=Strict` cookie (`Secure` in production); access token is held in memory.

## Authorization

- Spring Security protects every non-public route; frontend guards are convenience only.
- Method/service checks enforce role and store ownership before any mutation.
- Admin and owner audit events include actor, action, target, timestamp, request ID, and safe metadata.
- Return `404` where revealing another owner's resource existence would leak data.

## Request and data controls

- Bean Validation plus domain validation for all input; JPA parameter binding only.
- CORS exact allowlist from environment; no wildcard with credentials.
- CSRF: SameSite cookie and origin validation protect refresh/logout; state-changing API calls require bearer access tokens.
- Backend and Vercel responses set CSP, HSTS on HTTPS, nosniff, frame denial, permissions, and restrictive referrer policies.
- Output encoding in React; no unsafe HTML rendering without sanitization.
- Secret values come from environment/secret manager and are excluded from logs and repository.

## Abuse controls

- Fixed-window limits protect login, registration, refresh, search, reviews, reports, and admin writes on the initial single API instance; move limits to an edge/shared store before scaling horizontally.
- OpenAI catalog suggestions run server-side, are limited to five requests per IP per minute, and never claim store inventory, price, or availability.
- One review per user/store, account/store status checks, length limits, moderation workflow, and report throttling.
- Request IDs are accepted only when syntactically safe or generated server-side, returned in `X-Request-ID`, and attached to log context.

## Operations

- Expose only `/actuator/health` publicly; protect detailed health and all other actuator endpoints.
- Structured logs omit tokens, passwords, exact customer coordinates, and sensitive request bodies.
- Dependency scanning, secret scanning, backend tests, and frontend checks run in CI.
- Production uses TLS, managed PostgreSQL backups, least-privilege database credentials, key rotation, and tested restore procedures.

## Threats to test

Horizontal privilege escalation across stores, admin endpoint access, refresh-token replay, mass assignment, stale inventory overwrite, SQL/XSS payloads, enumeration, oversized pagination, and coordinate/radius abuse.
