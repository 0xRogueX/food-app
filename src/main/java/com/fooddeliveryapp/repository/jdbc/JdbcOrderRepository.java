package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.model.OrderItem;
import com.fooddeliveryapp.repository.OrderRepository;
import com.fooddeliveryapp.type.OrderStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcOrderRepository implements OrderRepository {

    private final DbConnectionManager connectionManager;

    public JdbcOrderRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }
        if (order.getId() == null) {
            insert(order);
        } else {
            update(order);
        }
        return order;
    }

    private void insert(Order order) {
        String sql = """
                INSERT INTO orders(
                    customer_id,
                    delivery_address,
                    subtotal,
                    discount_amount,
                    tax_amount,
                    delivery_fee,
                    final_amount,
                    status,
                    created_at,
                    updated_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, CAST(? AS order_status), ?, ?)
                RETURNING id, created_at, updated_at
                """;

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, order.getCustomerId());
                    ps.setString(2, order.getDeliveryAddress() != null ? order.getDeliveryAddress() : "");
                    ps.setDouble(3, order.getSubTotal());
                    ps.setDouble(4, order.getDiscountAmount());
                    ps.setDouble(5, order.getTaxAmount());
                    ps.setDouble(6, order.getDeliveryFee());
                    ps.setDouble(7, order.getFinalAmount());
                    ps.setString(8, order.getStatus().name());
                    LocalDateTime now = LocalDateTime.now();
                    ps.setTimestamp(9, Timestamp.valueOf(now));
                    ps.setTimestamp(10, Timestamp.valueOf(now));

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long id = rs.getLong("id");
                            order.setId(id);
                        }
                    }
                }

                insertOrderItems(conn, order);

                conn.commit();
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw new RuntimeException("Failed to insert order", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert order", e);
        }
    }

    private void insertOrderItems(Connection conn, Order order) throws SQLException {
        String sqlItems = """
                INSERT INTO order_items(
                    order_id,
                    menu_item_id,
                    menu_item_name,
                    price_at_add_time,
                    quantity
                )
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conn.prepareStatement(sqlItems)) {
            for (OrderItem item : order.getItems()) {
                ps.setLong(1, order.getId());
                ps.setLong(2, item.getFoodItemId() != null ? item.getFoodItemId() : 0L);
                ps.setString(3, item.getFoodItemName());
                ps.setDouble(4, item.getPriceAtPurchase());
                ps.setInt(5, item.getQuantity());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT * FROM orders WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = mapOrder(rs, loadItems(conn, id));
                    return Optional.of(order);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Order> findAll() {
        String sql = "SELECT * FROM orders";
        List<Order> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                result.add(mapOrder(rs, loadItems(conn, id)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all orders", e);
        }
        return result;
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        if (status == null) return List.of();
        String sql = "SELECT * FROM orders WHERE status = CAST(? AS order_status)";
        List<Order> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    result.add(mapOrder(rs, loadItems(conn, id)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by status", e);
        }
        return result;
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        if (customerId == null) return List.of();
        String sql = "SELECT * FROM orders WHERE customer_id = ?";
        List<Order> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    result.add(mapOrder(rs, loadItems(conn, id)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by customer id", e);
        }
        return result;
    }

    @Override
    public List<Order> findOngoingOrders() {
        String sql = """
                SELECT * FROM orders
                WHERE status IN ('CREATED','PAID','ASSIGNED','OUT_FOR_DELIVERY')
                """;
        List<Order> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                long id = rs.getLong("id");
                result.add(mapOrder(rs, loadItems(conn, id)));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find ongoing orders", e);
        }
        return result;
    }

    @Override
    public List<Order> findByDeliveryAgentId(Long agentId) {
        if (agentId == null) return List.of();
        String sql = "SELECT * FROM orders WHERE delivery_agent_id = ?";
        List<Order> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, agentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    result.add(mapOrder(rs, loadItems(conn, id)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find orders by delivery agent id", e);
        }
        return result;
    }

    @Override
    public Optional<Order> findByPaymentId(Long paymentId) {
        if (paymentId == null) return Optional.empty();
        String sql = "SELECT * FROM orders WHERE payment_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    long id = rs.getLong("id");
                    return Optional.of(mapOrder(rs, loadItems(conn, id)));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find order by payment id", e);
        }
        return Optional.empty();
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        String sql = "SELECT 1 FROM orders WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check order existence", e);
        }
    }

    @Override
    public void update(Order order) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Invalid order for update");
        }

        String sql = """
                UPDATE orders
                SET customer_id = ?,
                    subtotal = ?,
                    discount_amount = ?,
                    tax_amount = ?,
                    delivery_fee = ?,
                    final_amount = ?,
                    payment_id = ?,
                    delivery_agent_id = ?,
                    status = CAST(? AS order_status),
                    updated_at = ?
                WHERE id = ?
                """;

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setLong(1, order.getCustomerId());
                    ps.setDouble(2, order.getSubTotal());
                    ps.setDouble(3, order.getDiscountAmount());
                    ps.setDouble(4, order.getTaxAmount());
                    ps.setDouble(5, order.getDeliveryFee());
                    ps.setDouble(6, order.getFinalAmount());
                    if (order.getPaymentId() != null) {
                        ps.setLong(7, order.getPaymentId());
                    } else {
                        ps.setNull(7, Types.BIGINT);
                    }
                    if (order.getDeliveryAgentId() != null) {
                        ps.setLong(8, order.getDeliveryAgentId());
                    } else {
                        ps.setNull(8, Types.BIGINT);
                    }
                    ps.setString(9, order.getStatus().name());
                    ps.setTimestamp(10, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setLong(11, order.getId());

                    ps.executeUpdate();
                }
                // For simplicity, we do not update order_items here; they are immutable snapshot
                conn.commit();
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw new RuntimeException("Failed to update order", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update order", e);
        }
    }

    private List<OrderItem> loadItems(Connection conn, Long orderId) throws SQLException {
        String sql = "SELECT menu_item_id, menu_item_name, price_at_add_time, quantity FROM order_items WHERE order_id = ?";
        List<OrderItem> items = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Long menuItemId = (Long) rs.getObject("menu_item_id");
                    String name = rs.getString("menu_item_name");
                    double price = rs.getDouble("price_at_add_time");
                    int qty = rs.getInt("quantity");
                    items.add(new OrderItem(
                            menuItemId,
                            name,
                            price,
                            qty
                    ));
                }
            }
        }
        return items;
    }

    private Order mapOrder(ResultSet rs, List<OrderItem> items) throws SQLException {
        Long id = rs.getLong("id");
        Long customerId = (Long) rs.getObject("customer_id");
        String deliveryAddress = rs.getString("delivery_address");
        double subtotal = rs.getDouble("subtotal");
        double discount = rs.getDouble("discount_amount");
        double tax = rs.getDouble("tax_amount");
        double deliveryFee = rs.getDouble("delivery_fee");
        double finalAmount = rs.getDouble("final_amount");
        String statusStr = rs.getString("status");
        Long paymentId = (Long) rs.getObject("payment_id");
        Long deliveryAgentId = (Long) rs.getObject("delivery_agent_id");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp updatedTs = rs.getTimestamp("updated_at");
        Timestamp deliveredTs = rs.getTimestamp("delivered_at");

        Order order = new Order(id, customerId, items, deliveryAddress, subtotal, discount, deliveryFee);
        if (paymentId != null) {
            order.setPaymentId(paymentId);
        }
        if (deliveryAgentId != null) {
            order.setDeliveryAgentId(deliveryAgentId);
        }
        if (statusStr != null) {
            order.updateStatus(OrderStatus.valueOf(statusStr));
        }
        if (createdTs != null) {
            order.setCreatedAt(createdTs.toLocalDateTime());
        }
        if (updatedTs != null) {
            order.setUpdatedAt(updatedTs.toLocalDateTime());
        }
        if (deliveredTs != null) {
            order.setDeliveredAt(deliveredTs.toLocalDateTime());
        }
        return order;
    }
}

