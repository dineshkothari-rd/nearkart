# NearKart

**Search Nearby. Compare Prices. Shop Yourself.**

NearKart helps customers find products in nearby physical stores, compare price, distance, availability, and inventory freshness, then open external directions. The MVP has no delivery, cart, checkout, or payments.

## Status

Phase 3 stores is complete: owners can submit and manage store profiles and hours, admins can approve or reject them, and approved profiles are public. Authentication, ownership checks, and audit events protect the workflow.

## Architecture

- React + TypeScript + Vite customer and operations UI
- Java 17 + Spring Boot 4.1 modular monolith
- PostgreSQL with Flyway migrations
- REST APIs under `/api/v1`
- Stateless short-lived access JWTs with rotated refresh tokens

See [architecture](docs/architecture.md), [database](docs/database.md), [API](docs/api.md), and [roadmap](docs/development-roadmap.md).

## Repository

```text
nearkart/
├── frontend/        # React application
├── backend/         # Spring Boot application
├── docs/            # Product and engineering decisions
├── docker/          # Optional container support files
├── scripts/         # Small cross-project developer scripts
└── docker-compose.yml
```

## Prerequisites

- Java 17
- Node.js 24 LTS and npm 11
- Docker 29+ with Compose
- Git

Maven and PostgreSQL do not need global installation: use the committed Maven wrapper and Docker service.

## Setup, development, testing, and deployment

```sh
cp .env.example .env
docker compose up -d postgres
```

Then run these in separate terminals:

```sh
cd backend && set -a && source ../.env && set +a && ./mvnw spring-boot:run
cd frontend && npm ci && npm run dev
```

The frontend runs at `http://localhost:5173`; backend health is `http://localhost:8080/actuator/health`.

Tests and builds:

```sh
(cd backend && ./mvnw test)
(cd frontend && npm test && npm run lint && npm run build)
```

## Environment variables

Copy `.env.example`, replace `JWT_SECRET` with at least 32 random bytes, and load it before starting the backend. Production must set `AUTH_SECURE_COOKIE=true` and use a secret manager.

## Troubleshooting

- `mvn: command not found`: use `backend/mvnw`.
- `psql: command not found`: use the PostgreSQL container through Docker Compose.
- Port conflict: the planned defaults are frontend `5173`, backend `8080`, PostgreSQL `5432`.
