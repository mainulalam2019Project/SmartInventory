#!/usr/bin/env bash
# Initialize a SQLite database for the Smart Inventory System.
# Usage: ./scripts/init-db.sh [db_path]
set -euo pipefail

DB_PATH="${1:-inventory.db}"
ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"

echo "Creating SQLite database at: $DB_PATH"
rm -f "$DB_PATH"
sqlite3 "$DB_PATH" < "$ROOT_DIR/database/schema/sqlite/001_init.sql"
sqlite3 "$DB_PATH" < "$ROOT_DIR/database/schema/002_indexes.sql"
sqlite3 "$DB_PATH" < "$ROOT_DIR/database/schema/003_seed.sql"
echo "Done. Tables:"
sqlite3 "$DB_PATH" "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name;"
