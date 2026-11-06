# Smart Inventory and Supply Chain Management System

A professional, real-world Smart Inventory and Supply Chain Management System built with
**Java (Spring Boot)** for the core application and **Python (FastAPI)** for analytics,
integrated via REST APIs, backed by **SQLite** (dev) / **PostgreSQL** (prod).

## Modules
- User authentication & role management (RBAC, JWT, BCrypt)
- Product & category management
- Inventory management (optimistic locking, adjustments, cycle counts)
- Warehouse management
- Supplier management
- Purchase orders (create/approve/receive)
- Sales orders (confirm/fulfill/cancel, stock reservation)
- Stock transfers (request/approve/ship/receive)
- Low stock alerts
- Transaction history (append-only ledger)
- Python analytics: performance, turnover, slow-moving, monthly sales, reorder levels, shortage risk, forecasting (with evaluation), reports/charts

## Quick Start

### Prerequisites
- Java 17+
- Python 3.11+
- Maven 3.9+
- (optional) PostgreSQL 15+

### 1. Database (SQLite dev)
```bash
cd java-app
../scripts/init-db.sh inventory.db
```

### 2. Java application
```bash
cd java-app
export JAVA_HOME=/path/to/jdk-17
mvn package -DskipTests
DB_PATH=./inventory.db java -jar target/smart-inventory-java-1.0.0.jar
```
API: http://localhost:8080/api/v1  |  Swagger: http://localhost:8080/api/v1/swagger-ui.html  |  UI: http://localhost:8080/api/v1/index.html

Dev users: admin/Admin@123, manager/Manager@123, wstaff/Staff@123, sales/Sales@123, analyst/Analyst@123

### 3. Python analytics service
```bash
cd python-analytics
pip install -r requirements.txt
ANALYTICS_API_KEY=dev-analytics-key uvicorn app.main:app --host 0.0.0.0 --port 8000
```
Docs: http://localhost:8000/docs

### 4. Connect Java to Python
Set `PYTHON_ANALYTICS_URL=http://localhost:8000` and `PYTHON_ANALYTICS_API_KEY=dev-analytics-key` when running Java.

## Project Structure
See `docs/architecture.md`.

## Testing
- Java: `cd java-app && mvn test`
- Python: `cd python-analytics && pytest`
- DB: load `database/schema/*` on SQLite and PostgreSQL (verified).

## Deployment (Docker)
```bash
docker compose -f docker/docker-compose.yml up --build
```

## License
MIT
EOF