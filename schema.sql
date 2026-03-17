-- ====================================================================
-- FOOD DELIVERY SYSTEM DATABASE - Single Cloud Kitchen Application
-- ====================================================================

CREATE TYPE order_status AS ENUM ('CREATED','PAID','ASSIGNED','OUT_FOR_DELIVERY','DELIVERED','CANCELLED');
CREATE TYPE payment_status AS ENUM ('PENDING','COMPLETED','FAILED');
CREATE TYPE payment_mode AS ENUM ('CASH','UPI','CARD','NET_BANKING');

-- Function to auto-update 'updated_at' columns
CREATE OR REPLACE FUNCTION update_modified_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 1. Users
CREATE TABLE users (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_users_email_format
        CHECK (email ~* '^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),

    CONSTRAINT chk_users_phone_format
        CHECK (phone ~ '^[0-9]{10}$')
);

-- 2. Profiles
CREATE TABLE customers (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    address TEXT
);

CREATE TABLE delivery_agents (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    available BOOLEAN DEFAULT TRUE,
    average_rating DECIMAL(3,2) DEFAULT 0.0,
    total_ratings INT DEFAULT 0,
    total_deliveries INT DEFAULT 0,
    last_assigned_time TIMESTAMP NULL
);

CREATE TABLE admins (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- 3. Categories
CREATE TABLE categories (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL,
    is_active BOOLEAN DEFAULT TRUE
);

-- 4. Menu
CREATE TABLE menu_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) UNIQUE NOT NULL,
    price DECIMAL(10,2) NOT NULL CHECK(price > 0),
    is_available BOOLEAN DEFAULT TRUE
);

-- 5. Payments
CREATE TABLE payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT,
    amount DECIMAL(10,2) NOT NULL CHECK(amount > 0),
    mode payment_mode NOT NULL,
    status payment_status NOT NULL DEFAULT 'PENDING',
    transaction_ref VARCHAR(100) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at TIMESTAMP
);

-- 6. Orders
CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT REFERENCES customers(user_id) ON DELETE CASCADE,
    payment_id BIGINT,
    delivery_agent_id BIGINT REFERENCES delivery_agents(user_id) ON DELETE CASCADE,

    delivery_address TEXT NOT NULL,

    subtotal DECIMAL(10,2) NOT NULL,
    discount_amount DECIMAL(10,2) DEFAULT 0.0,
    tax_amount DECIMAL(10,2) DEFAULT 0.0,
    delivery_fee DECIMAL(10,2) DEFAULT 0.0,
    final_amount DECIMAL(10,2) NOT NULL,

    status order_status DEFAULT 'CREATED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at TIMESTAMP,

    CONSTRAINT chk_orders_amounts CHECK (
        subtotal >= 0 AND
        discount_amount >= 0 AND
        tax_amount >= 0 AND
        delivery_fee >= 0 AND
        final_amount >= 0
    )
);

-- Trigger to auto-update orders.updated_at
CREATE TRIGGER update_orders_modtime
BEFORE UPDATE ON orders
FOR EACH ROW EXECUTE FUNCTION update_modified_column();

-- Add FK from payments.order_id -> orders.id (after orders table is created)
ALTER TABLE payments
    ADD CONSTRAINT fk_payments_order_id FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE;

-- 7. Order Items
CREATE TABLE order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id BIGINT REFERENCES menu_items(id) ON DELETE CASCADE,
    menu_item_name VARCHAR(100) NOT NULL,
    price_at_add_time DECIMAL(10,2) NOT NULL CHECK(price_at_add_time >= 0),
    quantity INT NOT NULL CHECK(quantity > 0),

    CONSTRAINT uq_order_items UNIQUE(order_id, menu_item_id)
);

-- 8. Cart
CREATE TABLE carts (
    customer_id BIGINT PRIMARY KEY REFERENCES customers(user_id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Trigger to auto-update carts.updated_at
CREATE TRIGGER update_carts_modtime
BEFORE UPDATE ON carts
FOR EACH ROW EXECUTE FUNCTION update_modified_column();

CREATE TABLE cart_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cart_id BIGINT REFERENCES carts(customer_id) ON DELETE CASCADE,
    menu_item_id BIGINT REFERENCES menu_items(id) ON DELETE CASCADE,
    quantity INT NOT NULL CHECK(quantity > 0),

    CONSTRAINT uq_cart_items UNIQUE(cart_id, menu_item_id)
);

-- ==========================================
-- INDEXES FOR PERFORMANCE OPTIMIZATION
-- ==========================================

-- Foreign Keys
CREATE INDEX idx_menu_items_category ON menu_items(category_id);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);

-- Order Queries
CREATE INDEX idx_orders_customer_date ON orders(customer_id, created_at DESC);
CREATE INDEX idx_orders_agent_status ON orders(delivery_agent_id, status);
CREATE INDEX idx_orders_status_date ON orders(status, created_at ASC);

-- Delivery Agent Round Robin
CREATE INDEX idx_agents_available_assignment ON delivery_agents(available, last_assigned_time ASC);

-- Menu & Payments
CREATE INDEX idx_menu_items_category_available ON menu_items(category_id, is_available);
CREATE INDEX idx_payments_transaction_ref ON payments(transaction_ref);

-- 9. System Configuration
CREATE TABLE system_config (
    key VARCHAR(50) PRIMARY KEY,
    value VARCHAR(255) NOT NULL
);
-- Seed defaults:
INSERT INTO system_config(key, value) VALUES
    ('delivery_fee', '40.0'),
    ('tax_rate', '5.0'),
    ('discount_type', 'NONE'),
    ('discount_threshold', '0.0'),
    ('discount_value', '0.0');


-- DROP SCHEMA public CASCADE;
-- CREATE SCHEMA public;