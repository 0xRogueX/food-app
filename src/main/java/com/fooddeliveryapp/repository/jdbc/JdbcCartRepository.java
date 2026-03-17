package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Cart;
import com.fooddeliveryapp.model.CartItem;
import com.fooddeliveryapp.repository.CartRepository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcCartRepository implements CartRepository {

    private final DbConnectionManager connectionManager;

    public JdbcCartRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Cart findOrCreateCart(Long customerId) {
        ensureCartExists(customerId);
        return loadCart(customerId);
    }

    private void ensureCartExists(Long customerId) {
        String sql = """
                INSERT INTO carts(customer_id)
                VALUES (?)
                ON CONFLICT (customer_id) DO NOTHING
                """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure cart exists", e);
        }
    }

    private Cart loadCart(Long customerId) {
        String sql = """
                SELECT ci.menu_item_id, m.name AS menu_item_name, m.price, ci.quantity
                FROM cart_items ci
                JOIN menu_items m ON m.id = ci.menu_item_id
                WHERE ci.cart_id = ?
                """;
        Cart cart = new Cart(customerId);
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    long menuItemId = rs.getLong("menu_item_id");
                    String name = rs.getString("menu_item_name");
                    BigDecimal priceDecimal = rs.getBigDecimal("price");
                    double price = priceDecimal != null ? priceDecimal.doubleValue() : 0.0;
                    int quantity = rs.getInt("quantity");
                    cart.getItems().add(new CartItem(menuItemId, name, price, quantity));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load cart", e);
        }
        return cart;
    }

    @Override
    public void addOrUpdateItem(Long customerId, Long menuItemId, String menuItemName, double price, int quantity) {
        ensureCartExists(customerId);
        String sql = """
                INSERT INTO cart_items(cart_id, menu_item_id, quantity)
                VALUES (?, ?, ?)
                ON CONFLICT (cart_id, menu_item_id) DO UPDATE
                    SET quantity = cart_items.quantity + EXCLUDED.quantity
                """;
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, menuItemId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add or update cart item", e);
        }
    }

    @Override
    public void updateItemQuantity(Long customerId, Long menuItemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE cart_id = ? AND menu_item_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setLong(2, customerId);
            ps.setLong(3, menuItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update cart item quantity", e);
        }
    }

    @Override
    public void removeItem(Long customerId, Long menuItemId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ? AND menu_item_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.setLong(2, menuItemId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove cart item", e);
        }
    }

    @Override
    public void clearCart(Long customerId) {
        String sql = "DELETE FROM cart_items WHERE cart_id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, customerId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to clear cart", e);
        }
    }
}
