package com.fooddeliveryapp.repository;


import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.type.OrderStatus;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {

    Order save(Order order);

    Optional<Order> findById(Long id);
    List<Order> findAll();
    List<Order> findAllByStatus(OrderStatus status);
    List<Order> findByCustomerId(Long customerId);
    List<Order> findOngoingOrders(); // CREATED, PAID, ASSIGNED, OUT_FOR_DELIVERY
    List<Order> findByDeliveryAgentId(Long agentId);
    Optional<Order> findByPaymentId(Long paymentId);

    boolean existsById(Long id);

    void update(Order order); // future proof
}