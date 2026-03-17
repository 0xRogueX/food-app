package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.repository.CategoryRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcMenuItemRepository implements MenuItemRepository {

    private final DbConnectionManager connectionManager;
    private final CategoryRepository categoryRepository;

    public JdbcMenuItemRepository(DbConnectionManager connectionManager, CategoryRepository categoryRepository) {
        this.connectionManager = connectionManager;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public MenuItem save(MenuItem item) {
        if (item == null) {
            throw new IllegalArgumentException("MenuItem cannot be null");
        }

        if (item.getId() == null) {
            insert(item);
        } else {
            update(item);
        }
        return item;
    }

    private void insert(MenuItem item) {
        String sql = "INSERT INTO menu_items(category_id, name, price, is_available) VALUES (?, ?, ?, ?) RETURNING id";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (item.getCategoryId() != null) {
                ps.setLong(1, item.getCategoryId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    item.setId(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert menu item", e);
        }
    }

    private void update(MenuItem item) {
        String sql = "UPDATE menu_items SET category_id = ?, name = ?, price = ?, is_available = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            if (item.getCategoryId() != null) {
                ps.setLong(1, item.getCategoryId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setString(2, item.getName());
            ps.setDouble(3, item.getPrice());
            ps.setBoolean(4, item.isAvailable());
            ps.setLong(5, item.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update menu item", e);
        }
    }

    @Override
    public Optional<MenuItem> findById(Long id) {
        if (id == null) return Optional.empty();
        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find menu item by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<MenuItem> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find menu item by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<MenuItem> findAll() {
        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items";
        List<MenuItem> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all menu items", e);
        }
        return result;
    }

    @Override
    public List<MenuItem> findAllActive() {
        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items WHERE is_available = TRUE";
        List<MenuItem> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active menu items", e);
        }
        return result;
    }

    @Override
    public List<MenuItem> findAllInactive() {
        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items WHERE is_available = FALSE";
        List<MenuItem> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find inactive menu items", e);
        }
        return result;
    }

    @Override
    public List<MenuItem> findByCategoryId(Long categoryId) {
        if (categoryId == null) return List.of();

        // Ensure category is active
        Optional<Category> cat = categoryRepository.findById(categoryId);
        if (cat.isEmpty() || !cat.get().isActive()) {
            return List.of();
        }

        String sql = "SELECT id, category_id, name, price, is_available FROM menu_items WHERE category_id = ? AND is_available = TRUE";
        List<MenuItem> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find menu items by category id", e);
        }
        return result;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        // soft delete: mark unavailable
        String sql = "UPDATE menu_items SET is_available = FALSE WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft-delete menu item", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        String sql = "SELECT 1 FROM menu_items m JOIN categories c ON c.id = m.category_id " +
                "WHERE m.id = ? AND m.is_available = TRUE AND c.is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check menu item existence", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) return false;
        String sql = "SELECT 1 FROM menu_items WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check menu item name existence", e);
        }
    }

    private MenuItem map(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        Long categoryId = (Long) rs.getObject("category_id");
        String name = rs.getString("name");
        double price = rs.getDouble("price");
        boolean available = rs.getBoolean("is_available");

        MenuItem item = new MenuItem(id, name, price, categoryId);
        item.changeAvailability(available);
        return item;
    }
}

