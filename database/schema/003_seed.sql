-- ============================================================================
-- Smart Inventory & Supply Chain Management System
-- Sample / Seed Data
-- File: database/schema/003_seed.sql
-- Target: SQLite (dev) and PostgreSQL (prod) - uses INTEGER boolean flags
-- Run AFTER 001_init.sql and 002_indexes.sql
-- Passwords (dev only): Admin@123, Manager@123, Staff@123, Sales@123, Analyst@123
-- ============================================================================

BEGIN;

-- ----------------------------------------------------------------------------
-- Roles
-- ----------------------------------------------------------------------------
INSERT INTO roles (id, name, description) VALUES
 (1, 'Admin',                'Full system access'),
 (2, 'Inventory Manager',    'Products, inventory, warehouses, suppliers, POs, transfers, reports'),
 (3, 'Warehouse Staff',      'Stock counts, receive POs, transfers for assigned warehouses'),
 (4, 'Sales Representative', 'Customers, sales orders, fulfillment; read-only inventory'),
 (5, 'Analyst',              'Read-only access plus analytics dashboards and exports');

-- ----------------------------------------------------------------------------
-- Users  (bcrypt, cost 10)
-- ----------------------------------------------------------------------------
INSERT INTO users (id, username, email, password_hash, role_id, active) VALUES
 (1, 'admin',   'admin@smartinventory.com',   '$2b$10$3ElCTF0G4KasudrXlf8qteuvP2VS7tGa41wTqyXoJYNpsi5uL0uR.', 1, 1),
 (2, 'manager', 'manager@smartinventory.com', '$2b$10$gsG6ml6YoVkkZBAp4V2Fue9XU4GM2ViDZNEDORbYpD7p1Z9ECrTxe', 2, 1),
 (3, 'wstaff',  'staff@smartinventory.com',   '$2b$10$WnzYUSYwA2ASsUhLTs3Yb.rz9YsmV2dkfG50Gm0PAsW3SAEl80Ige',  3, 1),
 (4, 'sales',   'sales@smartinventory.com',   '$2b$10$J.7YOJ26C3CVFP1FzPweN.9L/VEhJ.7j2RoqwE7j5qxOKC9jc4Roa', 4, 1),
 (5, 'analyst', 'analyst@smartinventory.com', '$2b$10$UzAFjVz1EEp6fp5hIuTQN.yIaHl8LSNav3YTE5QWFOIAJq/0LMyZm',  5, 1);

-- ----------------------------------------------------------------------------
-- Categories
-- ----------------------------------------------------------------------------
INSERT INTO categories (id, name, parent_id, description) VALUES
 (1, 'Electronics',        NULL, 'Electronic devices and accessories'),
 (2, 'Computers',          1,    'Laptops, desktops, monitors'),
 (3, 'Accessories',        1,    'Peripherals and accessories'),
 (4, 'Clothing',           NULL, 'Apparel and garments'),
 (5, 'Men''s Clothing',     4,    'Mens apparel'),
 (6, 'Women''s Clothing',   4,    'Womens apparel'),
 (7, 'Food & Beverage',    NULL, 'Consumable food and drinks'),
 (8, 'Office Supplies',    NULL, 'Stationery and office equipment');

-- ----------------------------------------------------------------------------
-- Products
-- ----------------------------------------------------------------------------
INSERT INTO products (id, sku, name, category_id, unit, barcode, cost_price, selling_price, status, reorder_point, safety_stock) VALUES
 (1,  'SKU-1001', 'Laptop 15" Pro',          2, 'pcs', '1234567890001', 800.00, 1299.99, 'ACTIVE', 10, 5),
 (2,  'SKU-1002', 'Wireless Mouse',          3, 'pcs', '1234567890002',  15.00,   29.99, 'ACTIVE', 50, 20),
 (3,  'SKU-1003', 'Mechanical Keyboard',     3, 'pcs', '1234567890003',  45.00,   89.99, 'ACTIVE', 30, 10),
 (4,  'SKU-1004', '27" 4K Monitor',          2, 'pcs', '1234567890004', 200.00,  349.99, 'ACTIVE', 15, 5),
 (5,  'SKU-1005', 'USB-C Hub 7-in-1',        3, 'pcs', '1234567890005',  25.00,   49.99, 'ACTIVE', 40, 15),
 (6,  'SKU-2001', 'Men''s Cotton T-Shirt',    5, 'pcs', '1234567890006',   5.00,   14.99, 'ACTIVE',100, 30),
 (7,  'SKU-2002', 'Men''s Slim Jeans',        5, 'pcs', '1234567890007',  20.00,   49.99, 'ACTIVE', 60, 20),
 (8,  'SKU-2003', 'Women''s Summer Dress',    6, 'pcs', '1234567890008',  25.00,   59.99, 'ACTIVE', 50, 15),
 (9,  'SKU-2004', 'Winter Jacket',           6, 'pcs', '1234567890009',  60.00,  129.99, 'ACTIVE', 30, 10),
 (10, 'SKU-3001', 'Organic Coffee Beans 1kg',7, 'kg',  '1234567890010',   8.00,   18.99, 'ACTIVE', 80, 25),
 (11, 'SKU-3002', 'Green Tea Box (50 bags)', 7, 'box', '1234567890011',   4.00,    9.99, 'ACTIVE',100, 30),
 (12, 'SKU-3003', 'Snack Bar Pack (12)',     7, 'pack','1234567890012',   2.00,    5.49, 'ACTIVE',200, 50),
 (13, 'SKU-4001', 'Ergonomic Office Chair',  8, 'pcs', '1234567890013', 120.00,  249.99, 'ACTIVE', 10, 3),
 (14, 'SKU-4002', 'Standing Desk 60"',       8, 'pcs', '1234567890014', 250.00,  499.99, 'ACTIVE',  5, 2),
 (15, 'SKU-4003', 'Notebook Set (5)',        8, 'set', '1234567890015',   3.00,    7.99, 'ACTIVE',150, 40),
 (16, 'SKU-4004', 'Pen Pack (12)',           8, 'pack','1234567890016',   2.00,    4.99, 'ACTIVE',200, 50),
 (17, 'SKU-1006', 'Webcam HD 1080p',        3, 'pcs', '1234567890017',  30.00,   69.99, 'ACTIVE', 25, 8),
 (18, 'SKU-1007', 'Noise-Cancelling Headphones', 3, 'pcs', '1234567890018', 40.00,  99.99, 'ACTIVE', 20, 8),
 (19, 'SKU-2005', 'Running Shoes',           5, 'pcs', '1234567890019',  35.00,   79.99, 'ACTIVE', 40, 12),
 (20, 'SKU-3004', 'Bottled Water 24-pack',   7, 'pack','1234567890020',   4.00,    8.99, 'ACTIVE',120, 40);

-- ----------------------------------------------------------------------------
-- Warehouses
-- ----------------------------------------------------------------------------
INSERT INTO warehouses (id, code, name, address, city, country, capacity, active) VALUES
 (1, 'WH-CENTRAL', 'Central Warehouse',  '100 Industrial Blvd', 'New York',     'USA', 50000, 1),
 (2, 'WH-WEST',    'West Coast DC',      '250 Harbor Ave',      'Los Angeles',  'USA', 30000, 1),
 (3, 'WH-EAST',    'East Coast Hub',     '75 Atlantic Rd',      'Boston',       'USA', 20000, 1);

-- ----------------------------------------------------------------------------
-- Suppliers
-- ----------------------------------------------------------------------------
INSERT INTO suppliers (id, name, contact_person, email, phone, address, city, country, lead_time_days, rating, active) VALUES
 (1, 'TechSource Inc',          'Alice Johnson',  'alice@techsource.com',   '+1-555-0101', '10 Tech Park',       'San Jose',     'USA', 14, 4.50, 1),
 (2, 'Global Apparel Co',       'Bob Smith',      'bob@globalapparel.com',  '+1-555-0102', '20 Fashion St',      'New York',     'USA', 21, 4.20, 1),
 (3, 'FreshFoods Distributors', 'Carol Lee',      'carol@freshfoods.com',   '+1-555-0103', '30 Market Rd',       'Chicago',      'USA',  7, 4.80, 1),
 (4, 'OfficePro Supplies',      'David Kim',      'david@officepro.com',    '+1-555-0104', '40 Office Ave',      'Dallas',       'USA', 10, 4.30, 1),
 (5, 'MultiTrade Wholesale',    'Eva Brown',      'eva@multitrade.com',     '+1-555-0105', '50 Trade Center',    'Atlanta',      'USA', 12, 4.00, 1);

-- ----------------------------------------------------------------------------
-- Product <-> Supplier links
-- ----------------------------------------------------------------------------
INSERT INTO product_suppliers (product_id, supplier_id, supplier_sku, unit_cost, is_preferred, lead_time_days) VALUES
 (1, 1, 'TS-LAP-15',  780.00, 1, 14),
 (2, 1, 'TS-MOU-01',   12.00, 1, 10),
 (3, 1, 'TS-KBD-01',   40.00, 1, 10),
 (4, 1, 'TS-MON-27',  185.00, 1, 14),
 (5, 1, 'TS-HUB-07',   20.00, 1, 10),
 (17,1, 'TS-WEB-01',   26.00, 1, 10),
 (18,1, 'TS-HP-01',    35.00, 1, 10),
 (6, 2, 'GA-TSH-01',    4.00, 1, 21),
 (7, 2, 'GA-JNS-01',   17.00, 1, 21),
 (8, 2, 'GA-DRS-01',   22.00, 1, 21),
 (9, 2, 'GA-JKT-01',   55.00, 1, 21),
 (19,2, 'GA-SHO-01',   30.00, 1, 21),
 (10,3, 'FF-COF-01',    7.00, 1,  7),
 (11,3, 'FF-TEA-01',    3.20, 1,  7),
 (12,3, 'FF-SNK-01',    1.50, 1,  7),
 (20,3, 'FF-WTR-01',    3.20, 1,  7),
 (13,4, 'OP-CHR-01',  105.00, 1, 10),
 (14,4, 'OP-DSK-01',  225.00, 1, 10),
 (15,4, 'OP-NBK-01',    2.40, 1, 10),
 (16,4, 'OP-PEN-01',    1.60, 1, 10),
 (1, 5, 'MT-LAP-15',  790.00, 0, 12),
 (2, 5, 'MT-MOU-01',   13.00, 0, 12);

-- ----------------------------------------------------------------------------
-- Inventory (stock per product per warehouse)
-- Warehouse 1: all products
-- ----------------------------------------------------------------------------
INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_in_transit, version) VALUES
 (1,1, 25,  3, 10, 1),
 (2,1, 120, 10, 0, 1),
 (3,1,  60, 5,  0, 1),
 (4,1,  18, 2,  0, 1),
 (5,1,  90, 8,  0, 1),
 (6,1, 250, 20, 0, 1),
 (7,1,  80, 6,  0, 1),
 (8,1,  70, 4,  0, 1),
 (9,1,  35, 3,  0, 1),
 (10,1, 150, 10, 0, 1),
 (11,1, 200, 15, 0, 1),
 (12,1, 400, 30, 0, 1),
 (13,1,  12, 1,  0, 1),
 (14,1,   6, 0,  0, 1),
 (15,1, 300, 20, 0, 1),
 (16,1, 500, 40, 0, 1),
 (17,1,  40, 3,  0, 1),
 (18,1,  28, 2,  0, 1),
 (19,1,  55, 5,  0, 1),
 (20,1, 180, 12, 0, 1);

-- Warehouse 2: subset
INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_in_transit, version) VALUES
 (1,2,  8,  1, 0, 1),
 (2,2,  60, 5, 0, 1),
 (4,2,  10, 1, 0, 1),
 (6,2, 120, 8, 0, 1),
 (7,2,  40, 3, 0, 1),
 (10,2,  70, 5, 0, 1),
 (13,2,   5, 0, 0, 1),
 (15,2, 150, 10,0, 1),
 (17,2,  20, 2, 0, 1),
 (18,2,  15, 1, 0, 1),
 (19,2,  30, 3, 0, 1),
 (20,2,  90, 6, 0, 1);

-- Warehouse 3: subset
INSERT INTO inventory (product_id, warehouse_id, quantity_on_hand, quantity_reserved, quantity_in_transit, version) VALUES
 (2,3,  40, 2, 0, 1),
 (5,3,  30, 2, 0, 1),
 (11,3,  80, 5, 0, 1),
 (12,3, 150, 10,0, 1),
 (15,3, 100, 6, 0, 1),
 (16,3, 200, 15,0, 1),
 (18,3,  12, 1, 0, 1),
 (20,3,  60, 4, 0, 1);

-- ----------------------------------------------------------------------------
-- Customers
-- ----------------------------------------------------------------------------
INSERT INTO customers (id, name, email, phone, address, city, country, type, active) VALUES
 (1, 'John Smith',     'john.smith@email.com',   '+1-555-0201', '12 Oak St',      'New York',    'USA', 'RETAIL',      1),
 (2, 'Acme Corporation','orders@acmecorp.com',    '+1-555-0202', '99 Business Ave','New York',    'USA', 'WHOLESALE',   1),
 (3, 'Jane Doe',       'jane.doe@email.com',     '+1-555-0203', '45 Pine Rd',     'Boston',      'USA', 'RETAIL',      1),
 (4, 'Beta Industries', 'procurement@beta.com',  '+1-555-0204', '7 Industrial Way','Los Angeles','USA', 'DISTRIBUTOR', 1),
 (5, 'Charlie Brown',  'charlie@email.com',      '+1-555-0205', '8 Maple Dr',     'Chicago',     'USA', 'RETAIL',      1);

-- ----------------------------------------------------------------------------
-- Purchase Orders
-- ----------------------------------------------------------------------------
INSERT INTO purchase_orders (id, po_number, supplier_id, warehouse_id, status, order_date, expected_date, total_amount, created_by, approved_by, notes) VALUES
 (1, 'PO-2026-0001', 1, 1, 'APPROVED',            '2026-10-01', '2026-10-15', 9500.00, 2, 1, 'Tech hardware restock'),
 (2, 'PO-2026-0002', 3, 1, 'RECEIVED',            '2026-09-15', '2026-09-22', 1350.00, 2, 1, 'Food & beverage restock'),
 (3, 'PO-2026-0003', 2, 2, 'DRAFT',               '2026-10-20', '2026-11-10', 5800.00, 2, NULL, 'Apparel restock for west coast');

INSERT INTO purchase_order_items (po_id, product_id, quantity_ordered, quantity_received, unit_cost) VALUES
 (1, 1,  10,  0, 780.00),
 (1, 2,  50,  0,  12.00),
 (1, 3,  30,  0,  40.00),
 (2, 10, 100, 100,  7.00),
 (2, 11, 150, 150,  3.20),
 (2, 12, 300, 300,  1.50),
 (3, 6,  200,   0,  4.00),
 (3, 7,  100,   0, 17.00);

-- ----------------------------------------------------------------------------
-- Sales Orders
-- ----------------------------------------------------------------------------
INSERT INTO sales_orders (id, order_number, customer_id, warehouse_id, status, order_date, total_amount, created_by, notes) VALUES
 (1, 'SO-2026-0001', 1, 1, 'DELIVERED', '2026-10-05',  1359.97, 4, 'Laptop + accessories'),
 (2, 'SO-2026-0002', 2, 1, 'SHIPPED',   '2026-10-12',  3249.85, 4, 'Bulk monitor order'),
 (3, 'SO-2026-0003', 3, 2, 'PENDING',   '2026-10-18',   114.94, 4, 'Apparel order');

INSERT INTO sales_order_items (order_id, product_id, quantity, unit_price, quantity_fulfilled) VALUES
 (1, 1, 1, 1299.99, 1),
 (1, 2, 2,   29.99, 2),
 (2, 4, 5,  349.99, 5),
 (2, 3, 10,  89.99, 10),
 (2, 5, 15,  49.99, 15),
 (3, 6,  5,  14.99, 0),
 (3, 7,  2,  49.99, 0);

-- ----------------------------------------------------------------------------
-- Stock Transfer
-- ----------------------------------------------------------------------------
INSERT INTO stock_transfers (id, transfer_number, from_warehouse_id, to_warehouse_id, status, requested_by, approved_by, shipped_at, received_at, notes) VALUES
 (1, 'TR-2026-0001', 1, 2, 'SHIPPED', 2, 1, '2026-10-16 10:30:00', NULL, 'Mouse & keyboard restock to west coast');

INSERT INTO stock_transfer_items (transfer_id, product_id, quantity_requested, quantity_shipped, quantity_received) VALUES
 (1, 2, 20, 20, 0),
 (1, 3, 10, 10, 0);

-- ----------------------------------------------------------------------------
-- Stock Transactions (ledger)
-- ----------------------------------------------------------------------------
INSERT INTO stock_transactions (product_id, warehouse_id, type, quantity, reference_type, reference_id, performed_by, occurred_at, notes) VALUES
 (10, 1, 'RECEIPT',      100, 'PURCHASE_ORDER', 2, 3, '2026-09-22 09:00:00', 'PO-2026-0002 received'),
 (11, 1, 'RECEIPT',      150, 'PURCHASE_ORDER', 2, 3, '2026-09-22 09:05:00', 'PO-2026-0002 received'),
 (12, 1, 'RECEIPT',      300, 'PURCHASE_ORDER', 2, 3, '2026-09-22 09:10:00', 'PO-2026-0002 received'),
 (1,  1, 'ISSUE',         -1, 'SALES_ORDER',    1, 4, '2026-10-05 11:00:00', 'SO-2026-0001 fulfilled'),
 (2,  1, 'ISSUE',         -2, 'SALES_ORDER',    1, 4, '2026-10-05 11:00:00', 'SO-2026-0001 fulfilled'),
 (4,  1, 'ISSUE',         -5, 'SALES_ORDER',    2, 4, '2026-10-12 14:00:00', 'SO-2026-0002 fulfilled'),
 (3,  1, 'ISSUE',        -10, 'SALES_ORDER',    2, 4, '2026-10-12 14:00:00', 'SO-2026-0002 fulfilled'),
 (5,  1, 'ISSUE',        -15, 'SALES_ORDER',    2, 4, '2026-10-12 14:00:00', 'SO-2026-0002 fulfilled'),
 (2,  1, 'TRANSFER_OUT', -20, 'STOCK_TRANSFER', 1, 2, '2026-10-16 10:30:00', 'TR-2026-0001 shipped'),
 (3,  1, 'TRANSFER_OUT', -10, 'STOCK_TRANSFER', 1, 2, '2026-10-16 10:30:00', 'TR-2026-0001 shipped'),
 (2,  2, 'TRANSFER_IN',   20, 'STOCK_TRANSFER', 1, 2, '2026-10-16 10:30:00', 'TR-2026-0001 shipped'),
 (3,  2, 'TRANSFER_IN',   10, 'STOCK_TRANSFER', 1, 2, '2026-10-16 10:30:00', 'TR-2026-0001 shipped'),
 (14, 1, 'ADJUSTMENT',   -1, 'MANUAL',         NULL, 3, '2026-10-18 16:00:00', 'Damaged unit write-off'),
 (13, 1, 'COUNT',         0, 'STOCK_COUNT',    NULL, 3, '2026-10-19 08:00:00', 'Cycle count - no variance');

-- ----------------------------------------------------------------------------
-- Low Stock Alerts
-- ----------------------------------------------------------------------------
INSERT INTO low_stock_alerts (product_id, warehouse_id, current_qty, threshold, status, created_at) VALUES
 (14, 1, 6,  5,  'OPEN',          '2026-10-20 08:00:00'),
 (1,  2, 8,  10, 'OPEN',          '2026-10-20 08:05:00'),
 (13, 2, 5,  10, 'OPEN',          '2026-10-20 08:10:00'),
 (18, 3, 12, 20, 'ACKNOWLEDGED',  '2026-10-18 09:00:00'),
 (4,  1, 18, 15, 'RESOLVED',      '2026-10-10 09:00:00');

UPDATE low_stock_alerts SET resolved_at = '2026-10-12 10:00:00' WHERE status = 'RESOLVED';

-- ----------------------------------------------------------------------------
-- Audit Log
-- ----------------------------------------------------------------------------
INSERT INTO audit_log (actor_id, action, entity_type, entity_id, payload_json, occurred_at) VALUES
 (1, 'CREATE', 'USER',    4, '{"username":"sales"}', '2026-09-01 10:00:00'),
 (2, 'CREATE', 'PRODUCT', 1, '{"sku":"SKU-1001"}',   '2026-09-02 11:00:00'),
 (2, 'CREATE', 'PO',      1, '{"po_number":"PO-2026-0001"}', '2026-10-01 09:00:00'),
 (1, 'APPROVE','PO',      1, '{"po_number":"PO-2026-0001"}', '2026-10-01 09:30:00'),
 (4, 'CREATE', 'SO',      2, '{"order_number":"SO-2026-0002"}', '2026-10-12 13:45:00'),
 (2, 'SHIP',   'TRANSFER',1, '{"transfer_number":"TR-2026-0001"}', '2026-10-16 10:30:00');

COMMIT;
