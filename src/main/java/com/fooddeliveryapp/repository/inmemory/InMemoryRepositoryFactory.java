package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.repository.*;

public class InMemoryRepositoryFactory implements RepositoryFactory {

    private final InMemoryUserRepository userRepo;
    private final InMemoryDeliveryAgentRepository agentRepo;
    private final InMemoryCategoryRepository categoryRepo;
    private final InMemoryMenuItemRepository menuRepo;
    private final InMemoryOrderRepository orderRepo;
    private final InMemoryPaymentRepository paymentRepo;
    private final InMemoryCartRepository cartRepo;

    public InMemoryRepositoryFactory() {
        this.userRepo     = new InMemoryUserRepository();
        this.categoryRepo = new InMemoryCategoryRepository();
        this.menuRepo     = new InMemoryMenuItemRepository(categoryRepo);
        this.orderRepo    = new InMemoryOrderRepository();
        this.paymentRepo  = new InMemoryPaymentRepository();
        this.agentRepo    = new InMemoryDeliveryAgentRepository(userRepo);
        this.cartRepo     = new InMemoryCartRepository(userRepo);
    }

    @Override
    public UserRepository userRepository() {
        return userRepo;
    }

    @Override
    public DeliveryAgentRepository deliveryAgentRepository() {
        return agentRepo;
    }

    @Override
    public CategoryRepository categoryRepository() {
        return categoryRepo;
    }

    @Override
    public MenuItemRepository menuItemRepository() {
        return menuRepo;
    }

    @Override
    public OrderRepository orderRepository() {
        return orderRepo;
    }

    @Override
    public PaymentRepository paymentRepository() {
        return paymentRepo;
    }

    @Override
    public CartRepository cartRepository() {
        return cartRepo;
    }
}

