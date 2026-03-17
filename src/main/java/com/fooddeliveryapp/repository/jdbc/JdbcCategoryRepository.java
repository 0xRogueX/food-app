package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.repository.CategoryRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcCategoryRepository implements CategoryRepository {

    private final DbConnectionManager connectionManager;

    public JdbcCategoryRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Category save(Category category) {
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }

        if (category.getId() == null) {
            insert(category);
        } else {
            update(category);
        }
        return category;
    }

    private void insert(Category category) {
        String sql = "INSERT INTO categories(name, is_active) VALUES (?, ?) RETURNING id";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isActive());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    category.setId(rs.getLong("id"));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert category", e);
        }
    }

    private void update(Category category) {
        String sql = "UPDATE categories SET name = ?, is_active = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category.getName());
            ps.setBoolean(2, category.isActive());
            ps.setLong(3, category.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category", e);
        }
    }

    @Override
    public Optional<Category> findById(Long id) {
        if (id == null) return Optional.empty();

        String sql = "SELECT id, name, is_active FROM categories WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find category by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<Category> findByName(String name) {
        if (name == null || name.isBlank()) return Optional.empty();

        String sql = "SELECT id, name, is_active FROM categories WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find category by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Category> findAll() {
        String sql = "SELECT id, name, is_active FROM categories";
        List<Category> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all categories", e);
        }
        return result;
    }

    @Override
    public List<Category> findAllActive() {
        String sql = "SELECT id, name, is_active FROM categories WHERE is_active = TRUE";
        List<Category> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find active categories", e);
        }
        return result;
    }

    @Override
    public List<Category> findAllInactive() {
        String sql = "SELECT id, name, is_active FROM categories WHERE is_active = FALSE";
        List<Category> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find inactive categories", e);
        }
        return result;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;
        String sql = "UPDATE categories SET is_active = FALSE WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to soft-delete category", e);
        }
    }

    @Override
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) return false;
        String sql = "SELECT 1 FROM categories WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check category name existence", e);
        }
    }

    @Override
    public boolean existsById(Long categoryId) {
        if (categoryId == null) return false;
        String sql = "SELECT 1 FROM categories WHERE id = ? AND is_active = TRUE";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check category id existence", e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        String name = rs.getString("name");
        boolean active = rs.getBoolean("is_active");

        Category category = new Category(id, name);
        if (!active) {
            category.deactivate();
        }
        return category;
    }
}

