package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.config.DbConnectionManager;
import com.fooddeliveryapp.repository.*;

public class JdbcRepositoryFactory implements RepositoryFactory {

    private final DbConnectionManager connectionManager;

    private final UserRepository userRepository;
    private final DeliveryAgentRepository deliveryAgentRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CartRepository cartRepository;

    public JdbcRepositoryFactory() {
        this.connectionManager = new DbConnectionManager();
        this.userRepository = new JdbcUserRepository(connectionManager);
        this.categoryRepository = new JdbcCategoryRepository(connectionManager);
        this.menuItemRepository = new JdbcMenuItemRepository(connectionManager, categoryRepository);
        this.orderRepository = new JdbcOrderRepository(connectionManager);
        this.paymentRepository = new JdbcPaymentRepository(connectionManager);
        this.deliveryAgentRepository = new JdbcDeliveryAgentRepository(userRepository);
        this.cartRepository = new JdbcCartRepository(connectionManager);
    }

    @Override
    public UserRepository userRepository() {
        return userRepository;
    }

    @Override
    public DeliveryAgentRepository deliveryAgentRepository() {
        return deliveryAgentRepository;
    }

    @Override
    public CategoryRepository categoryRepository() {
        return categoryRepository;
    }

    @Override
    public MenuItemRepository menuItemRepository() {
        return menuItemRepository;
    }

    @Override
    public OrderRepository orderRepository() {
        return orderRepository;
    }

    @Override
    public PaymentRepository paymentRepository() {
        return paymentRepository;
    }

    @Override
    public CartRepository cartRepository() {
        return cartRepository;
    }

    public DbConnectionManager getConnectionManager() {
        return connectionManager;
    }
}

