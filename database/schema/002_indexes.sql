-- ============================================================================
-- Smart Inventory & Supply Chain Management System
-- Indexes - SQLite (development)  (shared with PostgreSQL)
-- File: database/schema/002_indexes.sql
-- ============================================================================

-- Users
CREATE INDEX IF NOT EXISTS idx_users_role_id      ON users(role_id);
CREATE INDEX IF NOT EXISTS idx_users_active       ON users(active);

-- Categories
CREATE INDEX IF NOT EXISTS idx_categories_parent  ON categories(parent_id);

-- Products
CREATE INDEX IF NOT EXISTS idx_products_category  ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_status    ON products(status);
CREATE INDEX IF NOT EXISTS idx_products_sku       ON products(sku);

-- Inventory
CREATE INDEX IF NOT EXISTS idx_inventory_product   ON inventory(product_id);
CREATE INDEX IF NOT EXISTS idx_inventory_warehouse ON inventory(warehouse_id);

-- Product suppliers
CREATE INDEX IF NOT EXISTS idx_ps_supplier        ON product_suppliers(supplier_id);

-- Purchase orders
CREATE INDEX IF NOT EXISTS idx_po_supplier        ON purchase_orders(supplier_id);
CREATE INDEX IF NOT EXISTS idx_po_warehouse       ON purchase_orders(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_po_status          ON purchase_orders(status);
CREATE INDEX IF NOT EXISTS idx_po_order_date      ON purchase_orders(order_date);
CREATE INDEX IF NOT EXISTS idx_poi_po            ON purchase_order_items(po_id);
CREATE INDEX IF NOT EXISTS idx_poi_product        ON purchase_order_items(product_id);

-- Customers
CREATE INDEX IF NOT EXISTS idx_customers_type     ON customers(type);
CREATE INDEX IF NOT EXISTS idx_customers_active   ON customers(active);

-- Sales orders
CREATE INDEX IF NOT EXISTS idx_so_customer        ON sales_orders(customer_id);
CREATE INDEX IF NOT EXISTS idx_so_warehouse       ON sales_orders(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_so_status          ON sales_orders(status);
CREATE INDEX IF NOT EXISTS idx_so_order_date      ON sales_orders(order_date);
CREATE INDEX IF NOT EXISTS idx_soi_order          ON sales_order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_soi_product        ON sales_order_items(product_id);

-- Stock transfers
CREATE INDEX IF NOT EXISTS idx_st_from_wh         ON stock_transfers(from_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_st_to_wh           ON stock_transfers(to_warehouse_id);
CREATE INDEX IF NOT EXISTS idx_st_status          ON stock_transfers(status);
CREATE INDEX IF NOT EXISTS idx_sti_transfer       ON stock_transfer_items(transfer_id);
CREATE INDEX IF NOT EXISTS idx_sti_product        ON stock_transfer_items(product_id);

-- Stock transactions (audit ledger) - heavily queried
CREATE INDEX IF NOT EXISTS idx_stx_product        ON stock_transactions(product_id);
CREATE INDEX IF NOT EXISTS idx_stx_warehouse      ON stock_transactions(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_stx_type           ON stock_transactions(type);
CREATE INDEX IF NOT EXISTS idx_stx_occurred        ON stock_transactions(occurred_at);
CREATE INDEX IF NOT EXISTS idx_stx_product_date   ON stock_transactions(product_id, occurred_at);
CREATE INDEX IF NOT EXISTS idx_stx_reference      ON stock_transactions(reference_type, reference_id);

-- Low stock alerts
CREATE INDEX IF NOT EXISTS idx_lsa_product        ON low_stock_alerts(product_id);
CREATE INDEX IF NOT EXISTS idx_lsa_warehouse      ON low_stock_alerts(warehouse_id);
CREATE INDEX IF NOT EXISTS idx_lsa_status         ON low_stock_alerts(status);

-- Audit log
CREATE INDEX IF NOT EXISTS idx_audit_actor        ON audit_log(actor_id);
CREATE INDEX IF NOT EXISTS idx_audit_entity       ON audit_log(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_occurred     ON audit_log(occurred_at);
