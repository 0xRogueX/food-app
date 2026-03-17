package com.fooddeliveryapp.model;

import com.fooddeliveryapp.type.PaymentMode;
import com.fooddeliveryapp.type.PaymentStatus;

import java.time.LocalDateTime;

public class Payment {

    private Long id;
    private Long orderId;
    private double amount;
    private PaymentMode mode;

    private PaymentStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime paidAt;

    public Payment(Long id, Long orderId, double amount, PaymentMode mode) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.mode = mode;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }


    // getters
    public Long getPaymentId() {
        return id;
    }
    public Long getOrderId() {
        return orderId;
    }
    public double getAmount() {
        return amount;
    }
    public PaymentMode getMode() {
        return mode;
    }
    public PaymentStatus getStatus() {
        return status;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    // setters for JDBC / persistence frameworks
    public void setId(Long id) {
        this.id = id;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    // business logic
    public void markSuccess() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already processed");
        }

        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    public void markFailed() {
        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException("Payment already processed");
        }

        this.status = PaymentStatus.FAILED;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }
}