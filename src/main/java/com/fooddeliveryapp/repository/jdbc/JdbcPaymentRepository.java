package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.model.Payment;
import com.fooddeliveryapp.repository.PaymentRepository;
import com.fooddeliveryapp.type.PaymentMode;
import com.fooddeliveryapp.type.PaymentStatus;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPaymentRepository implements PaymentRepository {

    private final DbConnectionManager connectionManager;

    public JdbcPaymentRepository(DbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        if (payment.getPaymentId() == null) {
            insert(payment);
        } else {
            update(payment);
        }
        return payment;
    }

    private void insert(Payment payment) {
        String sql = "INSERT INTO payments(order_id, amount, mode, status, transaction_ref, created_at, paid_at) " +
                "VALUES (?, ?, CAST(? AS payment_mode), CAST(? AS payment_status), ?, ?, ?) " +
                "RETURNING id, created_at, paid_at, status";

        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (payment.getOrderId() != null) {
                ps.setLong(1, payment.getOrderId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMode().name());
            ps.setString(4, payment.getStatus().name());
            ps.setNull(5, Types.VARCHAR); // transaction_ref not modeled yet
            ps.setTimestamp(6, Timestamp.valueOf(payment.getCreatedAt()));
            if (payment.getPaidAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(payment.getPaidAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    payment.setId(rs.getLong("id"));
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) {
                        payment.setCreatedAt(created.toLocalDateTime());
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert payment", e);
        }
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        if (paymentId == null) return Optional.empty();

        String sql = "SELECT id, amount, mode, status, order_id, created_at, paid_at FROM payments WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payment by id", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Payment> findAll() {
        String sql = "SELECT id, amount, mode, status, order_id, created_at, paid_at FROM payments";
        List<Payment> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                result.add(map(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find all payments", e);
        }
        return result;
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        if (orderId == null) return List.of();

        String sql = "SELECT id, amount, mode, status, order_id, created_at, paid_at FROM payments WHERE order_id = ?";
        List<Payment> result = new ArrayList<>();
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(map(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find payments by order id", e);
        }
        return result;
    }

    @Override
    public void update(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            throw new IllegalArgumentException("Invalid payment for update");
        }

        String sql = "UPDATE payments SET order_id = ?, amount = ?, mode = CAST(? AS payment_mode), status = CAST(? AS payment_status), paid_at = ? WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (payment.getOrderId() != null) {
                ps.setLong(1, payment.getOrderId());
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getMode().name());
            ps.setString(4, payment.getStatus().name());
            LocalDateTime paidAt = payment.getPaidAt();
            if (paidAt != null) {
                ps.setTimestamp(5, Timestamp.valueOf(paidAt));
            } else {
                ps.setNull(5, Types.TIMESTAMP);
            }
            ps.setLong(6, payment.getPaymentId());

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update payment", e);
        }
    }

    @Override
    public boolean existsById(Long paymentId) {
        if (paymentId == null) return false;

        String sql = "SELECT 1 FROM payments WHERE id = ?";
        try (Connection conn = connectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check payment existence", e);
        }
    }

    private Payment map(ResultSet rs) throws SQLException {
        Long id = rs.getLong("id");
        double amount = rs.getDouble("amount");
        String modeStr = rs.getString("mode");
        String statusStr = rs.getString("status");
        Long orderId = (Long) rs.getObject("order_id");
        Timestamp createdTs = rs.getTimestamp("created_at");
        Timestamp paidTs = rs.getTimestamp("paid_at");

        Payment payment = new Payment(id, orderId, amount, PaymentMode.valueOf(modeStr));
        if (createdTs != null) {
            payment.setCreatedAt(createdTs.toLocalDateTime());
        }
        if (statusStr != null) {
            payment.setStatus(PaymentStatus.valueOf(statusStr));
        }
        if (paidTs != null) {
            payment.setPaidAt(paidTs.toLocalDateTime());
        }
        return payment;
    }
}

