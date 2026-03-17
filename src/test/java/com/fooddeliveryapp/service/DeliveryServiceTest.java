package com.fooddeliveryapp.service;

import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.repository.CartRepository;
import com.fooddeliveryapp.repository.DeliveryAgentRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.repository.OrderRepository;
import com.fooddeliveryapp.repository.PaymentRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryCartRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryCategoryRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryDeliveryAgentRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryOrderRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryPaymentRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryUserRepository;
import com.fooddeliveryapp.service.impl.CartServiceImpl;
import com.fooddeliveryapp.service.impl.DeliveryServiceImpl;
import com.fooddeliveryapp.service.impl.OrderServiceImpl;
import com.fooddeliveryapp.service.impl.PaymentServiceImpl;
import com.fooddeliveryapp.strategy.CashPayment;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.type.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DeliveryServiceTest {

    private DeliveryService deliveryService;
    private OrderService orderService;
    private CartService cartService;
    private UserRepository userRepository;
    private DeliveryAgentRepository agentRepository;
    private Customer customer;
    private DeliveryAgent agent;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        SystemConfig config = SystemConfig.getInstance();
        config.setTaxRate(5.0);
        config.setDeliveryFee(40.0);

        userRepository = new InMemoryUserRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();
        MenuItemRepository menuItemRepo = new InMemoryMenuItemRepository(categoryRepo);
        CartRepository cartRepo = new InMemoryCartRepository(userRepository);
        OrderRepository orderRepo = new InMemoryOrderRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();
        agentRepository = new InMemoryDeliveryAgentRepository(userRepository);

        PaymentService paymentService = new PaymentServiceImpl(paymentRepo);
        cartService = new CartServiceImpl(cartRepo, menuItemRepo);
        orderService = new OrderServiceImpl(orderRepo, cartService, paymentService, menuItemRepo, userRepository);
        deliveryService = new DeliveryServiceImpl(agentRepository, userRepository, orderService);

        // Seed category and menu item
        Category category = new Category(null, "Pizza");
        categoryRepo.save(category);
        menuItem = new MenuItem(null, "Margherita", 200.0, category.getId());
        menuItemRepo.save(menuItem);

        // Seed customer
        customer = new Customer(null, "Alice", "9876543210", "alice@example.com", "123 Main St", "pass123");
        userRepository.save(customer);

        // Seed delivery agent
        agent = new DeliveryAgent(null, "Bob", "9876543210", "bob@example.com", "pass123");
        userRepository.save(agent);
    }

    // ── assignAgentToOrder ────────────────────────────────────────────────────

    @Test
    void assignAgentToOrder_success_agentAssignedAndMarkedBusy() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        Optional<DeliveryAgent> assigned = deliveryService.assignAgentToOrder(order.getId());

        assertTrue(assigned.isPresent());
        assertEquals(agent.getId(), assigned.get().getId());

        // Order must reference the agent
        Order updated = orderService.getOrderById(order.getId()).get();
        assertEquals(agent.getId(), updated.getDeliveryAgentId());
        assertEquals(OrderStatus.ASSIGNED, updated.getStatus());

        // Agent must be marked busy
        assertFalse(agentRepository.findById(agent.getId()).get().isAvailable());
    }

    @Test
    void assignAgentToOrder_noAgentAvailable_returnsEmpty() {
        agent.markBusy();
        userRepository.save(agent);

        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        Optional<DeliveryAgent> result = deliveryService.assignAgentToOrder(order.getId());

        assertTrue(result.isEmpty());
    }

    // ── markOrderOutForDelivery ───────────────────────────────────────────────

    @Test
    void markOrderOutForDelivery_updatesStatusCorrectly() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        deliveryService.assignAgentToOrder(order.getId());

        deliveryService.markOrderOutForDelivery(order.getId());

        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                orderService.getOrderById(order.getId()).get().getStatus());
    }

    // ── markOrderDelivered ────────────────────────────────────────────────────

    @Test
    void markOrderDelivered_orderStatusBecomesDelivered() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        deliveryService.assignAgentToOrder(order.getId());
        deliveryService.markOrderOutForDelivery(order.getId());

        deliveryService.markOrderDelivered(order.getId());

        assertEquals(OrderStatus.DELIVERED,
                orderService.getOrderById(order.getId()).get().getStatus());
    }

    @Test
    void markOrderDelivered_agentBecomesAvailableAgain() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        deliveryService.assignAgentToOrder(order.getId());
        deliveryService.markOrderOutForDelivery(order.getId());

        deliveryService.markOrderDelivered(order.getId());

        assertTrue(agentRepository.findById(agent.getId()).get().isAvailable());
    }

    @Test
    void markOrderDelivered_incrementsAgentDeliveryCount() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        deliveryService.assignAgentToOrder(order.getId());
        deliveryService.markOrderOutForDelivery(order.getId());

        deliveryService.markOrderDelivered(order.getId());

        assertEquals(1, agentRepository.findById(agent.getId()).get().getTotalDeliveries());
    }

    // ── rateDeliveryAgent ─────────────────────────────────────────────────────

    @Test
    void rateDeliveryAgent_validRating_updatesAverageRating() {
        deliveryService.rateDeliveryAgent(agent.getId(), 4.5);

        DeliveryAgent updated = agentRepository.findById(agent.getId()).get();
        assertEquals(4.5, updated.getRating(), 0.001);
        assertEquals(1, updated.getTotalRatings());
    }

    @Test
    void rateDeliveryAgent_multipleRatings_computesCorrectAverage() {
        deliveryService.rateDeliveryAgent(agent.getId(), 4.0);
        deliveryService.rateDeliveryAgent(agent.getId(), 5.0);

        // average = (4 + 5) / 2 = 4.5
        assertEquals(4.5, agentRepository.findById(agent.getId()).get().getRating(), 0.001);
    }

    @Test
    void rateDeliveryAgent_notFound_throwsResourceNotFound() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                deliveryService.rateDeliveryAgent(999L, 4.0));

        assertEquals(ErrorType.RESOURCE_NOT_FOUND, ex.getErrorType());
    }
}
