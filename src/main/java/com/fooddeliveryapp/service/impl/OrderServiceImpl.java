package com.fooddeliveryapp.service.impl;

import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.*;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.repository.OrderRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.service.CartService;
import com.fooddeliveryapp.service.OrderService;
import com.fooddeliveryapp.service.PaymentService;
import com.fooddeliveryapp.strategy.PaymentStrategy;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.type.OrderStatus;

import java.util.List;
import java.util.Optional;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartService cartService;
    private final MenuItemRepository menuItemRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    public OrderServiceImpl(OrderRepository orderRepository, CartService cartService,
            PaymentService paymentService, MenuItemRepository menuItemRepository,
            UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.cartService = cartService;
        this.paymentService = paymentService;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public Order placeOrder(Long customerId, PaymentStrategy paymentStrategy) {
        Cart cart = cartService.getCart(customerId);
        if (cart.isEmpty()) {
            throw new FoodDeliveryException(ErrorType.ORDER_ERROR, "Cannot place order with an empty cart");
        }

        List<OrderItem> orderItems = cart.getItems().stream()
                .map(ci -> {
                    MenuItem menuItem = menuItemRepository.findById(ci.getMenuItemId())
                            .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Menu item not found: " + ci.getMenuItemId()));
                    return new OrderItem(menuItem.getId(), menuItem.getName(), menuItem.getPrice(), ci.getQuantity());
                })
                .toList();

        SystemConfig config = SystemConfig.getInstance();
        double subTotal = orderItems.stream()
                .mapToDouble(OrderItem::getLineTotal)
                .sum();

        // Fetch customer address for delivery
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Customer not found"));
        if (!(user instanceof Customer customer)) {
            throw new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Customer not found");
        }
        String deliveryAddress = customer.getAddress();

        // Temp order to calculate discount
        Order tempOrder = new Order(null, customerId, orderItems, "", subTotal, 0, 0);
        double discount = config.getDiscountStrategy().calculateDiscount(tempOrder);

        // Create actual order
        Order order = new Order(
                null,
                customerId,
                orderItems,
                deliveryAddress,
                subTotal,
                discount,
                config.getDeliveryFee()
        );

        // Save order first to obtain its ID
        orderRepository.save(order);

        // Process Payment (order now has an ID so payment can reference it)
        Payment payment = paymentService.processPayment(order, paymentStrategy);
        order.attachPayment(payment.getPaymentId());

        // Update order to store the payment reference
        orderRepository.update(order);
        cartService.clearCart(customerId);

        return order;
    }

    @Override
    public void assignDeliveryAgent(Long orderId, Long agentId) {
        Order order = getOrderOrThrow(orderId);
        order.assignDeliveryAgent(agentId);
        orderRepository.update(order);
    }

    @Override
    public void markOrderOutForDelivery(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        order.markOutForDelivery();
        orderRepository.update(order);
    }

    @Override
    public void markOrderDelivered(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        order.markDelivered();
        orderRepository.update(order);
    }

    @Override
    public void cancelOrder(Long orderId) {
        Order order = getOrderOrThrow(orderId);
        order.cancel();
        orderRepository.update(order);
    }

    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    @Override
    public List<Order> getOrdersByCustomer(Long customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    @Override
    public List<Order> getOrdersByDeliveryAgent(Long agentId) {
        return orderRepository.findByDeliveryAgentId(agentId);
    }

    @Override
    public List<Order> getOrdersByStatus(OrderStatus status) {
        return orderRepository.findAllByStatus(status);
    }

    @Override
    public List<Order> getOngoingOrders() {
        return orderRepository.findOngoingOrders();
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    private Order getOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Order not found"));
    }
}