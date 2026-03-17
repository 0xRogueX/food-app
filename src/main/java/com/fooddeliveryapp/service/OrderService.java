package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.strategy.PaymentStrategy;
import com.fooddeliveryapp.type.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    // order creation
    Order placeOrder(Long customerId, PaymentStrategy paymentStrategy);

    // order status
    void assignDeliveryAgent(Long orderId, Long agentId);
    void markOrderOutForDelivery(Long orderId);
    void markOrderDelivered(Long orderId);
    void cancelOrder(Long orderId);

    // retrieval
    Optional<Order> getOrderById(Long orderId);
    List<Order> getOrdersByCustomer(Long customerId);
    List<Order> getOrdersByDeliveryAgent(Long agentId);
    List<Order> getOrdersByStatus(OrderStatus status);
    List<Order> getOngoingOrders(); // Returns CREATED, PAID, ASSIGNED, OUT_FOR_DELIVERY
    List<Order> getAllOrders();
}
