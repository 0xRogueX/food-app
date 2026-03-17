package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.repository.OrderRepository;
import com.fooddeliveryapp.type.OrderStatus;

import java.util.*;

public class InMemoryOrderRepository implements OrderRepository {

    private final Map<Long, Order> store = new HashMap<>();        // id -> Order
    private final Map<Long, Long> paymentIndex = new HashMap<>(); // paymentId -> orderId
    private long sequence = 1L;

    @Override
    public Order save(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order cannot be null");
        }

        if (order.getId() == null) {
            order.setId(sequence++);
        }

        store.put(order.getId(), order);

        if (order.getPaymentId() != null) {
            paymentIndex.put(order.getPaymentId(), order.getId());
        }

        return order;
    }

    @Override
    public Optional<Order> findById(Long id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Order> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Order> findAllByStatus(OrderStatus status) {
        if (status == null) return List.of();
        return store.values().stream()
                .filter(order -> order.getStatus() == status)
                .toList();
    }

    @Override
    public List<Order> findByCustomerId(Long customerId) {
        if (customerId == null) return List.of();
        return store.values().stream()
                .filter(order -> customerId.equals(order.getCustomerId()))
                .toList();
    }

    @Override
    public List<Order> findOngoingOrders() {
        return store.values().stream()
                .filter(order -> {
                    OrderStatus status = order.getStatus();
                    return status == OrderStatus.CREATED ||
                            status == OrderStatus.PAID ||
                            status == OrderStatus.ASSIGNED ||
                            status == OrderStatus.OUT_FOR_DELIVERY;
                })
                .toList();
    }

    @Override
    public List<Order> findByDeliveryAgentId(Long agentId) {
        if (agentId == null) return List.of();
        return store.values().stream()
                .filter(order -> agentId.equals(order.getDeliveryAgentId()))
                .toList();
    }

    @Override
    public Optional<Order> findByPaymentId(Long paymentId) {
        if (paymentId == null) return Optional.empty();
        Long orderId = paymentIndex.get(paymentId);
        if (orderId == null) return Optional.empty();
        return Optional.ofNullable(store.get(orderId));
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) return false;
        return store.containsKey(id);
    }

    @Override
    public void update(Order order) {
        if (order == null || order.getId() == null) {
            throw new IllegalArgumentException("Invalid order for update");
        }

        if (!store.containsKey(order.getId())) {
            throw new IllegalArgumentException("Order does not exist. Cannot update.");
        }

        // Remove old paymentIndex if paymentId changed
        Order old = store.get(order.getId());
        Long oldPaymentId = old.getPaymentId();
        if (oldPaymentId != null && !oldPaymentId.equals(order.getPaymentId())) {
            paymentIndex.remove(oldPaymentId);
        }

        store.put(order.getId(), order);

        if (order.getPaymentId() != null) {
            paymentIndex.put(order.getPaymentId(), order.getId());
        }
    }
}