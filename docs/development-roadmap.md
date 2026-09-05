# Development roadmap

Each phase ends with runnable checks and updated documentation before the next begins.

## Phase 0 — complete

Environment inventory, repository skeleton, product boundary, module architecture, ER/schema plan, API outline, authentication, inventory freshness, ranking, security, testing, deployment, assumptions, and risks.

## Phase 1 — complete

Generate React/Vite and Spring Boot projects, Maven wrapper, lint/format config, PostgreSQL Compose service, Flyway baseline, health endpoint, `.env.example`, and CI checks. Exit: UI and API start, DB connects, migration runs, health is green.

## Phase 2 — complete

Users/roles/refresh-token migrations; register/login/refresh/logout; BCrypt, JWT, RBAC, protected UI; integration tests for auth and privilege failure.

## Phase 3 — complete

Store submission, approval, profile, coordinates, hours, owner checks, admin workflow, audit events, and minimal owner/admin UI.

## Phase 4 — complete

Categories, products/variants, store listings, current/history price, inventory states, optimistic concurrency, configurable freshness, ownership checks, and audit events.

## Phase 5 — complete

PostgreSQL trigram search, bounded candidate query, Haversine distance, radius/sort/pagination, freshness-weighted score/explanation, ranking tests, realistic Indian catalog data, and responsive customer UI.

## Phase 6 — complete

Mobile-first home/search/results/product/store, location input, directions, favorites, history, reviews, reports, accessible states.

## Phase 7 — owner dashboard

Inventory/price editing, store profile/hours, summary analytics, bulk UI updates (not CSV import).

## Phase 8 — admin

Users, stores, approvals, catalog, reviews, reports, audit logs, metrics.

## Phase 9 — journey testing

Customer and owner critical flows, API integration tests, frontend interaction tests, failure/concurrency cases.

## Phase 10 — hardening

Threat review, query plans/indexes, performance budgets, structured logging, backups/restore, deployment config, accessibility audit, operational runbook.
