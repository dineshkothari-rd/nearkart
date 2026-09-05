# Ranking engine

Ranking operates only on eligible offers: approved/active store, active product/listing, inside radius, valid current price, and availability not `OUT_OF_STOCK` when `availableNow=true`.

## MVP score

Each component is normalized to `[0,1]` within the candidate set. Higher is better.

```text
score = 0.30 distance + 0.25 price + 0.25 freshness + 0.15 availability + 0.05 rating
```

- `distance = 1 - min(distanceMeters / radiusMeters, 1)`
- `price = (maxPrice - price) / (maxPrice - minPrice)`, or `1` when all prices match
- freshness uses the configured tier score in [inventory-freshness](inventory-freshness.md)
- availability: `AVAILABLE=1`, `LOW_STOCK=0.65`, `UNKNOWN=0.25`, `OUT_OF_STOCK=0`
- rating: Bayesian-adjusted store rating divided by 5; unrated stores use the platform prior

Weights are application configuration validated to total `1.0`. Results return the dominant positive factors as a human-readable reason, never an objective “best” claim. Sponsored placement is outside MVP and may never modify this score without clear labeling.

## Tie-breaking

Score descending, freshness descending, distance ascending, price ascending, stable store-product UUID. This makes pagination deterministic.

## Required checks

1. Cheapest and nearest ranks highly.
2. A ₹10 saving does not automatically beat several extra kilometres.
3. A slightly dearer fresh offer can beat a stale cheap offer.
4. Equal prices do not divide by zero.
5. Identical scores have deterministic ordering.

Weights are hypotheses. Adjust them only from measured conversion/user research, with the old/new values recorded in `decisions.md`.
