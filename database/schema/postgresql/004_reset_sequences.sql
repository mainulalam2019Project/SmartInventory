-- ============================================================================
-- PostgreSQL sequence reset after loading 003_seed.sql
-- File: database/schema/postgresql/004_reset_sequences.sql
-- Run after 003_seed.sql on PostgreSQL only.
-- ============================================================================
SELECT setval(pg_get_serial_sequence('roles','id'),              COALESCE((SELECT MAX(id) FROM roles),1), true);
SELECT setval(pg_get_serial_sequence('users','id'),              COALESCE((SELECT MAX(id) FROM users),1), true);
SELECT setval(pg_get_serial_sequence('categories','id'),         COALESCE((SELECT MAX(id) FROM categories),1), true);
SELECT setval(pg_get_serial_sequence('products','id'),           COALESCE((SELECT MAX(id) FROM products),1), true);
SELECT setval(pg_get_serial_sequence('warehouses','id'),         COALESCE((SELECT MAX(id) FROM warehouses),1), true);
SELECT setval(pg_get_serial_sequence('inventory','id'),          COALESCE((SELECT MAX(id) FROM inventory),1), true);
SELECT setval(pg_get_serial_sequence('suppliers','id'),          COALESCE((SELECT MAX(id) FROM suppliers),1), true);
SELECT setval(pg_get_serial_sequence('purchase_orders','id'),    COALESCE((SELECT MAX(id) FROM purchase_orders),1), true);
SELECT setval(pg_get_serial_sequence('purchase_order_items','id'), COALESCE((SELECT MAX(id) FROM purchase_order_items),1), true);
SELECT setval(pg_get_serial_sequence('customers','id'),          COALESCE((SELECT MAX(id) FROM customers),1), true);
SELECT setval(pg_get_serial_sequence('sales_orders','id'),       COALESCE((SELECT MAX(id) FROM sales_orders),1), true);
SELECT setval(pg_get_serial_sequence('sales_order_items','id'),  COALESCE((SELECT MAX(id) FROM sales_order_items),1), true);
SELECT setval(pg_get_serial_sequence('stock_transfers','id'),    COALESCE((SELECT MAX(id) FROM stock_transfers),1), true);
SELECT setval(pg_get_serial_sequence('stock_transfer_items','id'), COALESCE((SELECT MAX(id) FROM stock_transfer_items),1), true);
SELECT setval(pg_get_serial_sequence('stock_transactions','id'), COALESCE((SELECT MAX(id) FROM stock_transactions),1), true);
SELECT setval(pg_get_serial_sequence('low_stock_alerts','id'),   COALESCE((SELECT MAX(id) FROM low_stock_alerts),1), true);
SELECT setval(pg_get_serial_sequence('audit_log','id'),          COALESCE((SELECT MAX(id) FROM audit_log),1), true);
