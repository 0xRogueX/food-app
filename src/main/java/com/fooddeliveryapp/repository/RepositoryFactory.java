package com.fooddeliveryapp.repository;

public interface RepositoryFactory {

    UserRepository userRepository();
    DeliveryAgentRepository deliveryAgentRepository();
    CategoryRepository categoryRepository();
    MenuItemRepository menuItemRepository();
    OrderRepository orderRepository();
    PaymentRepository paymentRepository();
    CartRepository cartRepository();
}

