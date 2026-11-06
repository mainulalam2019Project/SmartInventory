# Database

## Overview
Normalized relational schema for the Smart Inventory and Supply Chain Management System.
Supports **SQLite** (development) and **PostgreSQL** (production). MySQL is also feasible
with minor dialect adjustments (AUTO_INCREMENT, boolean flags).

## Files
```
database/
├── schema/
│   ├── sqlite/
│   │   └── 001_init.sql          # SQLite DDL
│   ├── postgresql/
│   │   ├── 001_init.sql          # PostgreSQL DDL
│   │   └── 004_reset_sequences.sql
│   ├── 002_indexes.sql           # Indexes (shared)
│   └── 003_seed.sql              # Sample data (shared)
├── sample-data/                  # (reserved for CSV bulk loads)
└── README.md
```

## Initialize (SQLite)
```bash
rm -f inventory.db
sqlite3 inventory.db < database/schema/sqlite/001_init.sql
sqlite3 inventory.db < database/schema/002_indexes.sql
sqlite3 inventory.db < database/schema/003_seed.sql
# enable FK enforcement at runtime:
# PRAGMA foreign_keys = ON;
```

## Initialize (PostgreSQL)
```bash
createdb smart_inventory
psql -d smart_inventory -f database/schema/postgresql/001_init.sql
psql -d smart_inventory -f database/schema/002_indexes.sql
psql -d smart_inventory -f database/schema/003_seed.sql
psql -d smart_inventory -f database/schema/postgresql/004_reset_sequences.sql
```

## Dev credentials (NEVER use in production)
| username | password   | role                 |
|----------|------------|----------------------|
| admin    | Admin@123  | Admin                |
| manager  | Manager@123| Inventory Manager    |
| wstaff   | Staff@123  | Warehouse Staff      |
| sales    | Sales@123  | Sales Representative |
| analyst  | Analyst@123| Analyst              |

Password hashes are bcrypt (cost 10). Replace before production deployment.

## Notes
- Boolean flags are stored as INTEGER (0/1) for cross-database compatibility.
- `stock_transactions` is an append-only ledger; never UPDATE/DELETE rows.
- `inventory.version` is used for optimistic locking.
- Foreign keys must be enabled in SQLite (`PRAGMA foreign_keys = ON;`).
- Full DDL rationale and ER description: see docs/db-schema.md (Phase 2 addendum).
