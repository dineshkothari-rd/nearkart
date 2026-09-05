# Development roadmap

Each phase ends with runnable checks and updated documentation before the next begins.

## Phase 0 — complete

Environment inventory, repository skeleton, product boundary, module architecture, ER/schema plan, API outline, authentication, inventory freshness, ranking, security, testing, deployment, assumptions, and risks.

## Phase 1 — complete

Generate React/Vite and Spring Boot projects, Maven wrapper, lint/format config, PostgreSQL Compose service, Flyway baseline, health endpoint, `.env.example`, and CI checks. Exit: UI and API start, DB connects, migration runs, health is green.

## Phase 2 — complete

Users/roles/refresh-token migrations; register/login/refresh/logout; BCrypt, JWT, RBAC, protected UI; integration tests for auth and privilege failure.

## Phase 3 — stores

Store submission, approval, profile, coordinates, hours, owner checks, admin workflow, audit events.

## Phase 4 — catalog, pricing, inventory

Categories, products/variants/aliases, store listings, current/history price, inventory states, optimistic concurrency, freshness.

## Phase 5 — search and ranking

PostgreSQL partial search, candidate query, Haversine distance, filters/pagination, score/explanation, ranking tests, realistic Indian seed data.

## Phase 6 — customer experience

Mobile-first home/search/results/product/store, location input, directions, favorites, history, reviews, reports, accessible states.

## Phase 7 — owner dashboard

Inventory/price editing, store profile/hours, summary analytics, bulk UI updates (not CSV import).

## Phase 8 — admin

Users, stores, approvals, catalog, reviews, reports, audit logs, metrics.

## Phase 9 — journey testing

Customer and owner critical flows, API integration tests, frontend interaction tests, failure/concurrency cases.

## Phase 10 — hardening

Threat review, query plans/indexes, performance budgets, structured logging, backups/restore, deployment config, accessibility audit, operational runbook.
