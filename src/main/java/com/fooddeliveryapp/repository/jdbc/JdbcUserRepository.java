package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Admin;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.UserRepository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcUserRepository implements UserRepository {

    private final DbConnectionManager connectionManager;

    public JdbcUserRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        if (user.getId() == null) {
            insertUser(user);
        } else {
            updateUser(user);
        }
        return user;
    }

    private void insertUser(User user) {
        String sql = "INSERT INTO users(name, phone, email, password) VALUES (?,?,?,?) RETURNING id, created_at";

        try (Connection conn = connectionManager.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, user.getName());
                    ps.setString(2, user.getPhone());
                    ps.setString(3, user.getEmail());
                    ps.setString(4, user.getPassword());

                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            long id = rs.getLong("id");
                            Timestamp created = rs.getTimestamp("created_at");
                            user.setId(id);
                            if (created != null) {
                                user.setCreatedAt(created.toLocalDateTime());
                            }
                        }
                    }
                }

                insertProfile(conn, user);
                conn.commit();
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ignored) {}
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert user", e);
        }
    }

    private void updateUser(User user) {
        String sql = "UPDATE users SET name = ?, phone = ?, email = ?, password = ? WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getName());
            ps.setString(2, user.getPhone());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setLong(5, user.getId());

            ps.executeUpdate();

            upsertProfile(conn, user);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    private void insertProfile(Connection conn, User user) throws SQLException {
        if (user instanceof Customer customer) {
            String sql = "INSERT INTO customers(user_id, address) VALUES (?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, customer.getId());
                ps.setString(2, customer.getAddress());
                ps.executeUpdate();
            }
        } else if (user instanceof DeliveryAgent agent) {
            String sql = "INSERT INTO delivery_agents(user_id, available, average_rating, total_ratings, total_deliveries, last_assigned_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, agent.getId());
                ps.setBoolean(2, agent.isAvailable());
                ps.setDouble(3, agent.getRating());
                ps.setInt(4, agent.getTotalRatings());
                ps.setInt(5, agent.getTotalDeliveries());
                LocalDateTime lastAssigned = agent.getLastAssignedTime();
                if (lastAssigned != null) {
                    ps.setTimestamp(6, Timestamp.valueOf(lastAssigned));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }
                ps.executeUpdate();
            }
        } else if (user instanceof Admin admin) {
            String sql = "INSERT INTO admins(user_id) VALUES (?)";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, admin.getId());
                ps.executeUpdate();
            }
        }
    }

    private void upsertProfile(Connection conn, User user) throws SQLException {
        if (user instanceof Customer customer) {
            String sql = """
                    INSERT INTO customers(user_id, address)
                    VALUES (?, ?)
                    ON CONFLICT (user_id) DO UPDATE SET address = EXCLUDED.address
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, customer.getId());
                ps.setString(2, customer.getAddress());
                ps.executeUpdate();
            }
        } else if (user instanceof DeliveryAgent agent) {
            String sql = """
                    INSERT INTO delivery_agents(user_id, available, average_rating, total_ratings, total_deliveries, last_assigned_time)
                    VALUES (?, ?, ?, ?, ?, ?)
                    ON CONFLICT (user_id) DO UPDATE SET
                        available = EXCLUDED.available,
                        average_rating = EXCLUDED.average_rating,
                        total_ratings = EXCLUDED.total_ratings,
                        total_deliveries = EXCLUDED.total_deliveries,
                        last_assigned_time = EXCLUDED.last_assigned_time
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, agent.getId());
                ps.setBoolean(2, agent.isAvailable());
                ps.setDouble(3, agent.getRating());
                ps.setInt(4, agent.getTotalRatings());
                ps.setInt(5, agent.getTotalDeliveries());
                LocalDateTime lastAssigned = agent.getLastAssignedTime();
                if (lastAssigned != null) {
                    ps.setTimestamp(6, Timestamp.valueOf(lastAssigned));
                } else {
                    ps.setNull(6, Types.TIMESTAMP);
                }
                ps.executeUpdate();
            }
        } else if (user instanceof Admin admin) {
            String sql = """
                    INSERT INTO admins(user_id)
                    VALUES (?)
                    ON CONFLICT (user_id) DO NOTHING
                    """;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, admin.getId());
                ps.executeUpdate();
            }
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        if (id == null) return Optional.empty();

        String sql = """
                SELECT u.id, u.name, u.phone, u.email, u.password, u.created_at,
                       c.address,
                       da.available, da.average_rating, da.total_ratings, da.total_deliveries, da.last_assigned_time,
                       ad.user_id AS admin_id
                FROM users u
                LEFT JOIN customers c ON c.user_id = u.id
                LEFT JOIN delivery_agents da ON da.user_id = u.id
                LEFT JOIN admins ad ON ad.user_id = u.id
                WHERE u.id = ?
                """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by id", e);
        }
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) return Optional.empty();

        String sql = """
                SELECT u.id, u.name, u.phone, u.email, u.password, u.created_at,
                       c.address,
                       da.available, da.average_rating, da.total_ratings, da.total_deliveries, da.last_assigned_time,
                       ad.user_id AS admin_id
                FROM users u
                LEFT JOIN customers c ON c.user_id = u.id
                LEFT JOIN delivery_agents da ON da.user_id = u.id
                LEFT JOIN admins ad ON ad.user_id = u.id
                WHERE u.email = ?
                """;

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUser(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find user by email", e);
        }
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        String sql = """
                SELECT u.id, u.name, u.phone, u.email, u.password, u.created_at,
                       c.address,
                       da.available, da.average_rating, da.total_ratings, da.total_deliveries, da.last_assigned_time,
                       ad.user_id AS admin_id
                FROM users u
                LEFT JOIN customers c ON c.user_id = u.id
                LEFT JOIN delivery_agents da ON da.user_id = u.id
                LEFT JOIN admins ad ON ad.user_id = u.id
                """;

        List<User> users = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all users", e);
        }
        return users;
    }

    @Override
    public void deleteById(Long id) {
        if (id == null) return;

        String sql = "DELETE FROM users WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;

        String sql = "SELECT 1 FROM users WHERE id = ?";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String phone = rs.getString("phone");
        String email = rs.getString("email");
        String password = rs.getString("password");
        Timestamp createdTs = rs.getTimestamp("created_at");
        LocalDateTime createdAt = createdTs != null ? createdTs.toLocalDateTime() : null;

        String address = rs.getString("address");
        Boolean available = (Boolean) rs.getObject("available");
        BigDecimal avgRatingDecimal = rs.getBigDecimal("average_rating");
        Double avgRating = avgRatingDecimal != null ? avgRatingDecimal.doubleValue() : null;
        Integer totalRatings = (Integer) rs.getObject("total_ratings");
        Integer totalDeliveries = (Integer) rs.getObject("total_deliveries");
        Timestamp lastAssignedTs = rs.getTimestamp("last_assigned_time");
        Long adminId = (Long) rs.getObject("admin_id");

        User user;
        if (address != null) {
            Customer customer = new Customer(id, name, phone, email, address, password);
            user = customer;
        } else if (available != null || avgRating != null || totalRatings != null || totalDeliveries != null || lastAssignedTs != null) {
            DeliveryAgent agent = new DeliveryAgent(id, name, phone, email, password);
            agent.setAvailability(available != null ? available : true);
            if (avgRating != null) {
                agent.setAverageRating(avgRating);
            }
            if (totalRatings != null) {
                agent.setTotalRatings(totalRatings);
            }
            if (totalDeliveries != null) {
                agent.setTotalDeliveries(totalDeliveries);
            }
            if (lastAssignedTs != null) {
                agent.setLastAssignedTime(lastAssignedTs.toLocalDateTime());
            }
            user = agent;
        } else if (adminId != null) {
            Admin admin = new Admin(id, name, phone, email, password);
            user = admin;
        } else {
            // Default to Customer without address if no profile row; should not normally happen
            Customer customer = new Customer(id, name, phone, email, "", password);
            user = customer;
        }

        if (createdAt != null) {
            user.setCreatedAt(createdAt);
        }
        return user;
    }
}

