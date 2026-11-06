# Installation Guide

## Java Application

### Build
```bash
cd java-app
mvn clean package -DskipTests
```

### Run (SQLite)
```bash
cd java-app
DB_PATH=./inventory.db java -jar target/smart-inventory-java-1.0.0.jar
```

### Run (PostgreSQL)
```bash
cd java-app
SPRING_PROFILES_ACTIVE=postgres \
DB_HOST=localhost DB_PORT=5432 DB_NAME=smart_inventory DB_USER=postgres DB_PASSWORD=postgres \
java -jar target/smart-inventory-java-1.0.0.jar
```

### Environment Variables
| Variable | Default | Description |
|----------|---------|-------------|
| SPRING_PROFILES_ACTIVE | sqlite | sqlite or postgres |
| DB_PATH | ./inventory.db | SQLite file path |
| DB_HOST/PORT/NAME/USER/PASSWORD | localhost/5432/smart_inventory/postgres/postgres | Postgres connection |
| JWT_SECRET | (dev default) | JWT signing secret (change in prod) |
| JWT_EXPIRATION_MS | 86400000 | Token expiry |
| SERVER_PORT | 8080 | HTTP port |
| PYTHON_ANALYTICS_URL | http://localhost:8000 | Python service URL |
| PYTHON_ANALYTICS_API_KEY | dev-analytics-key | Shared API key |
| CORS_ORIGINS | http://localhost:3000,... | Allowed origins |

## Python Analytics Service

```bash
cd python-analytics
pip install -r requirements.txt
ANALYTICS_API_KEY=dev-analytics-key uvicorn app.main:app --host 0.0.0.0 --port 8000
```

## Database Initialization

SQLite:
```bash
./scripts/init-db.sh inventory.db
```

PostgreSQL:
```bash
createdb smart_inventory
psql -d smart_inventory -f database/schema/postgresql/001_init.sql
psql -d smart_inventory -f database/schema/002_indexes.sql
psql -d smart_inventory -f database/schema/003_seed.sql
psql -d smart_inventory -f database/schema/postgresql/004_reset_sequences.sql
```

## Docker

```bash
docker compose -f docker/docker-compose.yml up --build
```

## Default Credentials (dev only)
admin / Admin@123 (Admin), manager / Manager@123 (Inventory Manager),
wstaff / Staff@123 (Warehouse Staff), sales / Sales@123 (Sales Representative),
analyst / Analyst@123 (Analyst).
EOF