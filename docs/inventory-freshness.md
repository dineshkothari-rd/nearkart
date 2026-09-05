# Inventory freshness

Inventory is an observation, not a guarantee. `observedAt` records when the source confirmed the state; `updatedAt` records database mutation time.

| Age | Label | Score | Customer wording |
|---|---|---:|---|
| 0–30 minutes | `FRESH` | 1.00 | Updated N minutes ago |
| >30–120 minutes | `RECENT` | 0.80 | Recently updated |
| >2–24 hours | `POSSIBLY_STALE` | 0.40 | Availability may have changed |
| >24 hours | `STALE` | 0.10 | Availability uncertain |

Thresholds live in typed backend configuration and must be strictly increasing. The API returns both raw `observedAt` and derived label; the frontend renders relative time from the timestamp.

## Update rules

- Allowed sources: `OWNER`, `ADMIN`, `IMPORT`, `POS` (future). The server derives actor/source permissions.
- Quantity is optional because some stores only report state. If present, it is nonnegative and zero forces `OUT_OF_STOCK`.
- Owner updates require ownership and an expected entity version.
- Every change emits `INVENTORY_UPDATED` audit data without sensitive payloads.
- Search never changes `STALE` to `AVAILABLE`; it displays uncertainty and lowers rank.

The MVP does not poll or predict stock. Add automated expiry or confidence modelling only when real update-volume data justifies it.
