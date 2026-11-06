# Database Schema Documentation

## Entity-Relationship Summary

```
roles 1──* users
categories 1──* categories (self, parent)
categories 1──* products
products 1──* inventory *──1 warehouses
products 1──* product_suppliers *──1 suppliers
suppliers 1──* purchase_orders *──1 warehouses
purchase_orders 1──* purchase_order_items *──1 products
customers 1──* sales_orders *──1 warehouses
sales_orders 1──* sales_order_items *──1 products
warehouses 1──* stock_transfers (from/to) *──1 warehouses
stock_transfers 1──* stock_transfer_items *──1 products
products 1──* stock_transactions *──1 warehouses
products 1──* low_stock_alerts *──1 warehouses
users 1──* audit_log
```

## Table Purposes

- **roles / users**: Authentication & RBAC. Passwords stored as bcrypt hashes only.
- **categories**: Hierarchical product taxonomy (adjacency list).
- **products**: Master product data including reorder_point and safety_stock.
- **warehouses**: Physical storage locations with capacity.
- **inventory**: Current stock per (product, warehouse). Optimistic locking via `version`.
- **suppliers / product_suppliers**: Supplier master and many-to-many product sourcing with preferred flag and cost.
- **purchase_orders (+ items)**: Procurement lifecycle DRAFT→RECEIVED.
- **customers**: Customer master with type (RETAIL/WHOLESALE/DISTRIBUTOR).
- **sales_orders (+ items)**: Sales lifecycle with fulfillment tracking.
- **stock_transfers (+ items)**: Inter-warehouse movement with shipment/receipt.
- **stock_transactions**: Append-only ledger of every stock movement (audit trail).
- **low_stock_alerts**: Generated alerts with lifecycle OPEN/ACKNOWLEDGED/RESOLVED.
- **audit_log**: Generic action audit with JSON payload.

## Key Constraints
- UNIQUE(product_id, warehouse_id) on inventory.
- UNIQUE(po_id, product_id), UNIQUE(order_id, product_id), UNIQUE(transfer_id, product_id) on line items.
- CHECK constraints enforce non-negative quantities, status enums, and quantity_received <= quantity_ordered, etc.
- FKs with ON DELETE CASCADE for child rows (items, inventory) and RESTRICT/SET NULL elsewhere.

## Indexing Strategy
- All foreign-key columns indexed.
- Composite index on stock_transactions(product_id, occurred_at) for ledger queries.
- Date columns indexed for reporting (order_date, occurred_at).
- Status columns indexed for filtered list views.

## Design Decisions
- Boolean flags stored as INTEGER (0/1) for SQLite/PostgreSQL/MySQL compatibility.
- Monetary values use DECIMAL(12,2) / DECIMAL(14,2).
- Timestamps stored as TIMESTAMP (SQLite stores as text ISO-8601).
- No soft-delete on transactional tables; products use status field.
- Inventory uses optimistic locking (`version`) instead of pessimistic locks for scalability.
