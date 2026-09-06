# Production operations

## Release checklist

1. Require green backend, frontend, API journey, and bundle-budget CI checks.
2. Create a database backup before migrations and retain the previous application image/deployment.
3. Deploy the Render API, wait for `/actuator/health`, then deploy the Vercel frontend.
4. Run `./scripts/api-smoke.sh` against the production API with a dedicated non-human test admin and remove its test records.
5. Check structured logs by `requestId`, error rate, response latency, database connections, and disk growth.

## Backup and restore

Enable scheduled backups and retention with the managed PostgreSQL provider. Before a risky release, create an additional encrypted logical backup from a trusted machine:

```sh
DATABASE_URL='postgresql://...' ./scripts/backup-db.sh /secure/existing/path/nearkart.dump
```

Practice restore into a non-production database. Restoring replaces matching objects and therefore requires an explicit confirmation value:

```sh
DATABASE_URL='postgresql://...' CONFIRM_RESTORE=nearkart ./scripts/restore-db.sh /secure/path/nearkart.dump
```

Never commit, upload, or paste database URLs or dump files. Verify the restored health endpoint and API journey before declaring recovery complete.

## Rollback

- Application-only failure: roll Render and Vercel back to the previous successful deployment; Flyway migrations must remain forward-compatible.
- Bad data migration: stop writes, take a forensic backup, restore the last verified backup, deploy the prior application, then validate health and journeys.
- Compromised JWT secret: rotate `JWT_SECRET` and redeploy; all access tokens become invalid. Revoke refresh tokens in the database if account sessions may be compromised.
- Compromised database credential: rotate it in the provider and Render, redeploy, and verify old credentials no longer connect.

## Incident response

1. Record start time, affected surface, deployment version, and a safe request ID; never copy tokens or passwords.
2. Contain by pausing a deployment, disabling the affected account/store, or temporarily blocking the route at the edge.
3. Inspect structured API logs and database/provider metrics, reproduce with non-production data, and restore service.
4. Run the API journey and targeted regression test, monitor recovery, and document cause plus prevention.

## Capacity and retention

- CI caps compiled JavaScript at 500 KB and CSS at 100 KB; raise a budget only with measured user benefit.
- Search bounds database candidates at 500 and public pagination at 100. Inspect `EXPLAIN (ANALYZE, BUFFERS)` when p95 search latency exceeds 500 ms.
- Review `store_events`, `search_history`, `audit_logs`, and `price_history` growth monthly. Add partitioning/aggregation only when retention or query measurements require it.
- The built-in rate limiter protects the initial single API instance. Move limits to the edge or a shared store before horizontal scaling.
