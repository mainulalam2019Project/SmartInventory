# System Architecture

## High-Level Diagram

```
┌────────────────────────────────────────────────────────────┐
│  Presentation: Web UI (HTML/CSS/JS) + Swagger             │
└──────────────────────┬─────────────────────────────────────┘
                       │ HTTP/JSON  /api/v1
┌──────────────────────▼─────────────────────────────────────┐
│  Java Application (Spring Boot 3, Java 17)                 │
│  ┌─────────┐┌─────────┐┌─────────┐┌─────────┐┌───────────┐  │
│  │  Auth   ││ Product ││Inventory││Warehouse││  Supplier  │  │
│  └────┬────┘└────┬────┘└────┬────┘└────┬────┘└─────┬─────┘  │
│  ┌────┴────┐┌────┴────┐┌────┴────┐┌────┴────┐┌────┴─────┐  │
│  │   PO    ││  Sales  ││Transfer ││  Alerts ││ Txn Hist │  │
│  └────┬────┘└────┬────┘└────┬────┘└────┬────┘└─────┬─────┘  │
│       └──────────┴──────────┴──────────┴───────────┘        │
│                 Service Layer (@Transactional)              │
│                 Repository Layer (JDBC/JdbcTemplate)        │
│                 Integration Client (RestTemplate) ────────┐ │
└──────────────────────┬────────────────────────────────────┼─┘
                       │ JDBC                               │
┌──────────────────────▼────────────────────────────────────┐ │
│  Database: SQLite (dev) / PostgreSQL (prod)               │ │
└───────────────────────────────────────────────────────────┘ │
                                                             │ HTTP/JSON
┌────────────────────────────────────────────────────────────▼─┐
│  Python Analytics Service (FastAPI, Python 3.11+)            │
│  Pandas/NumPy · scikit-learn · Matplotlib/Seaborn           │
│  Endpoints: performance, turnover, slow-moving, monthly     │
│  sales, reorder-levels, shortage-risk, forecast, reports     │
└──────────────────────────────────────────────────────────────┘
```

## Layering
- **Controller** → REST endpoints, validation, RBAC annotations.
- **Service** → business logic, transaction boundaries.
- **Repository** → JDBC data access (JdbcTemplate).
- **Model/DTO** → domain objects.
- **Config** → security, datasource, JWT, OpenAPI, REST client.

## Security
- JWT bearer tokens (24h expiry).
- BCrypt (cost 10) password hashing.
- RBAC via Spring Security method security.
- Parameterized queries (no SQL injection).

## Concurrency
- Optimistic locking on `inventory.version`.
- DB transactions with rollback on failure.

## Integration
- Java → Python via REST (X-API-Key header).
- Standard response envelope `{success, data, error}`.
- Python returns metrics + evaluated forecast (no unvalidated accuracy claims).
EOF