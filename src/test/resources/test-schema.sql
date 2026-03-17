-- ============================================================
-- H2 test schema (PostgreSQL compatibility mode)
-- ============================================================

-- Enum-like domains so that CAST(? AS order_status) compiles
CREATE DOMAIN IF NOT EXISTS order_status   AS VARCHAR(50);
CREATE DOMAIN IF NOT EXISTS payment_mode   AS VARCHAR(50);
CREATE DOMAIN IF NOT EXISTS payment_status AS VARCHAR(50);

-- Users
CREATE TABLE IF NOT EXISTS users (
    id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    phone      VARCHAR(20),
    email      VARCHAR(255) UNIQUE NOT NULL,
    password   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Customer profiles
CREATE TABLE IF NOT EXISTS customers (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    address VARCHAR(500)
);

-- Delivery agent profiles
CREATE TABLE IF NOT EXISTS delivery_agents (
    user_id            BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
    available          BOOLEAN      NOT NULL DEFAULT TRUE,
    average_rating     DECIMAL(4,2) NOT NULL DEFAULT 0.0,
    total_ratings      INT          NOT NULL DEFAULT 0,
    total_deliveries   INT          NOT NULL DEFAULT 0,
    last_assigned_time TIMESTAMP
);

-- Admin profiles
CREATE TABLE IF NOT EXISTS admins (
    user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- Categories
CREATE TABLE IF NOT EXISTS categories (
    id        BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name      VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);

-- Menu items
CREATE TABLE IF NOT EXISTS menu_items (
    id           BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    category_id  BIGINT REFERENCES categories(id),
    name         VARCHAR(255) NOT NULL,
    price        DECIMAL(10,2) NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE
);

-- Orders
CREATE TABLE IF NOT EXISTS orders (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id       BIGINT,
    delivery_address  VARCHAR(500),
    subtotal          DECIMAL(10,2),
    discount_amount   DECIMAL(10,2) DEFAULT 0,
    tax_amount        DECIMAL(10,2) DEFAULT 0,
    delivery_fee      DECIMAL(10,2) DEFAULT 0,
    final_amount      DECIMAL(10,2),
    payment_id        BIGINT,
    delivery_agent_id BIGINT,
    status            order_status DEFAULT 'CREATED',
    created_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    delivered_at      TIMESTAMP
);

-- Order items (immutable snapshot of items at order time)
CREATE TABLE IF NOT EXISTS order_items (
    id                BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id          BIGINT REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id      BIGINT,
    menu_item_name    VARCHAR(255),
    price_at_add_time DECIMAL(10,2),
    quantity          INT
);

-- Payments (order_id has no FK so tests can use arbitrary order IDs)
CREATE TABLE IF NOT EXISTS payments (
    id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id        BIGINT,
    amount          DECIMAL(10,2),
    mode            payment_mode,
    status          payment_status DEFAULT 'PENDING',
    transaction_ref VARCHAR(255),
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    paid_at         TIMESTAMP
);

-- Carts (one row per customer; cart_id == customer_id by design)
CREATE TABLE IF NOT EXISTS carts (
    customer_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE
);

-- Cart items
CREATE TABLE IF NOT EXISTS cart_items (
    cart_id      BIGINT NOT NULL REFERENCES carts(customer_id) ON DELETE CASCADE,
    menu_item_id BIGINT NOT NULL REFERENCES menu_items(id),
    quantity     INT    NOT NULL DEFAULT 1,
    CONSTRAINT uq_cart_item UNIQUE (cart_id, menu_item_id)
);
