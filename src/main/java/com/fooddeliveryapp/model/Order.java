package com.fooddeliveryapp.model;

import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.type.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public class Order {

    private Long id;
    private Long customerId;
    private final List<OrderItem> items;

    private String deliveryAddress;

    private double subTotal;
    private double discountAmount;
    private double taxAmount;
    private double deliveryFee;
    private double finalAmount;

    private Long paymentId;
    private Long deliveryAgentId;

    private OrderStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    public Order(Long id, Long customerId, List<OrderItem> items, String deliveryAddress,
                 double subTotal, double discountAmount, double deliveryFee) {

        this.id = id;
        this.customerId = customerId;
        this.items = List.copyOf(items); // immutable copy
        this.deliveryAddress = deliveryAddress;

        this.subTotal = subTotal;
        this.discountAmount = discountAmount;
        this.deliveryFee = deliveryFee;

        calculateAmounts();

        this.status = OrderStatus.CREATED;

        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void calculateAmounts() {
        this.taxAmount = (subTotal * SystemConfig.getInstance().getTaxRate()) / 100;
        this.finalAmount = subTotal - discountAmount + taxAmount + deliveryFee;
    }

    // getters
    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public double getSubTotal() {
        return subTotal;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getDeliveryFee() {
        return deliveryFee;
    }

    public double getFinalAmount() {
        return finalAmount;
    }

    public Long getPaymentId() {
        return paymentId;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Long getDeliveryAgentId() {
        return deliveryAgentId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getDeliveredAt() {
        return deliveredAt;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPaymentId(Long paymentId) {
        this.paymentId = paymentId;
    }

    public void setDeliveryAgentId(Long deliveryAgentId) {
        this.deliveryAgentId = deliveryAgentId;
    }


    public String getOrderNumber() {
        return id != null ? "ORD-" + id : null;
    }

    // business logic
    public void assignDeliveryAgent(Long agentId) {
        this.deliveryAgentId = agentId;
        updateStatus(OrderStatus.ASSIGNED);
    }

    public void attachPayment(Long paymentId) {
        this.paymentId = paymentId;
        updateStatus(OrderStatus.PAID);
    }

    public void markOutForDelivery() {
        updateStatus(OrderStatus.OUT_FOR_DELIVERY);
    }

    public void markDelivered() {
        updateStatus(OrderStatus.DELIVERED);
        this.deliveredAt = LocalDateTime.now();
    }

    public void cancel() {
        updateStatus(OrderStatus.CANCELLED);
    }

    public void updateStatus(OrderStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setDeliveredAt(LocalDateTime deliveredAt) {
        this.deliveredAt = deliveredAt;
    }
}