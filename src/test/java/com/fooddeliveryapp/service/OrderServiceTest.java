package com.fooddeliveryapp.service;

import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.repository.CartRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.repository.OrderRepository;
import com.fooddeliveryapp.repository.PaymentRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryCartRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryCategoryRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryOrderRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryPaymentRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryUserRepository;
import com.fooddeliveryapp.service.impl.CartServiceImpl;
import com.fooddeliveryapp.service.impl.OrderServiceImpl;
import com.fooddeliveryapp.service.impl.PaymentServiceImpl;
import com.fooddeliveryapp.strategy.CashPayment;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.type.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    private OrderService orderService;
    private CartService cartService;
    private Customer customer;
    private MenuItem menuItem;

    @BeforeEach
    void setUp() {
        // Ensure predictable SystemConfig state
        SystemConfig config = SystemConfig.getInstance();
        config.setTaxRate(5.0);
        config.setDeliveryFee(40.0);

        UserRepository userRepo = new InMemoryUserRepository();
        InMemoryCategoryRepository categoryRepo = new InMemoryCategoryRepository();
        MenuItemRepository menuItemRepo = new InMemoryMenuItemRepository(categoryRepo);
        CartRepository cartRepo = new InMemoryCartRepository(userRepo);
        OrderRepository orderRepo = new InMemoryOrderRepository();
        PaymentRepository paymentRepo = new InMemoryPaymentRepository();

        PaymentService paymentService = new PaymentServiceImpl(paymentRepo);
        cartService = new CartServiceImpl(cartRepo, menuItemRepo);
        orderService = new OrderServiceImpl(orderRepo, cartService, paymentService, menuItemRepo, userRepo);

        // Seed data
        Category category = new Category(null, "Pizza");
        categoryRepo.save(category);
        menuItem = new MenuItem(null, "Margherita", 200.0, category.getId());
        menuItemRepo.save(menuItem);

        customer = new Customer(null, "Alice", "9876543210", "alice@example.com", "123 Main St", "pass123");
        userRepo.save(customer);
    }

    // ── placeOrder ────────────────────────────────────────────────────────────

    @Test
    void placeOrder_success_orderIsPaidWithCorrectAmounts() {
        cartService.addItem(customer.getId(), menuItem.getId(), 2);

        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        assertNotNull(order.getId());
        assertEquals(customer.getId(), order.getCustomerId());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertNotNull(order.getPaymentId());
        assertEquals(400.0, order.getSubTotal());           // 200 × 2
        assertEquals(20.0, order.getTaxAmount(), 0.001);    // 400 × 5%
        assertEquals(460.0, order.getFinalAmount(), 0.001); // 400 + 20 + 40
    }

    @Test
    void placeOrder_emptyCart_throwsOrderError() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                orderService.placeOrder(customer.getId(), new CashPayment()));

        assertEquals(ErrorType.ORDER_ERROR, ex.getErrorType());
    }

    @Test
    void placeOrder_clearsCartAfterSuccess() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        orderService.placeOrder(customer.getId(), new CashPayment());

        assertTrue(cartService.getCart(customer.getId()).isEmpty());
    }

    @Test
    void placeOrder_deliveryAddressMatchesCustomerAddress() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);

        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        assertEquals("123 Main St", order.getDeliveryAddress());
    }

    // ── cancelOrder ───────────────────────────────────────────────────────────

    @Test
    void cancelOrder_success_statusBecomeCancelled() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        orderService.cancelOrder(order.getId());

        assertEquals(OrderStatus.CANCELLED, orderService.getOrderById(order.getId()).get().getStatus());
    }

    @Test
    void cancelOrder_notFound_throwsException() {
        assertThrows(FoodDeliveryException.class, () -> orderService.cancelOrder(999L));
    }

    // ── assignDeliveryAgent ───────────────────────────────────────────────────

    @Test
    void assignDeliveryAgent_success_statusBecomesAssigned() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        orderService.assignDeliveryAgent(order.getId(), 100L);

        Order updated = orderService.getOrderById(order.getId()).get();
        assertEquals(OrderStatus.ASSIGNED, updated.getStatus());
        assertEquals(100L, updated.getDeliveryAgentId());
    }

    // ── markOrderOutForDelivery ───────────────────────────────────────────────

    @Test
    void markOrderOutForDelivery_statusBecomesOutForDelivery() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        orderService.assignDeliveryAgent(order.getId(), 100L);

        orderService.markOrderOutForDelivery(order.getId());

        assertEquals(OrderStatus.OUT_FOR_DELIVERY,
                orderService.getOrderById(order.getId()).get().getStatus());
    }

    // ── markOrderDelivered ────────────────────────────────────────────────────

    @Test
    void markOrderDelivered_statusBecomesDelivered() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());
        orderService.assignDeliveryAgent(order.getId(), 100L);
        orderService.markOrderOutForDelivery(order.getId());

        orderService.markOrderDelivered(order.getId());

        assertEquals(OrderStatus.DELIVERED,
                orderService.getOrderById(order.getId()).get().getStatus());
    }

    // ── getOrderById ─────────────────────────────────────────────────────────

    @Test
    void getOrderById_found_returnsOrder() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order = orderService.placeOrder(customer.getId(), new CashPayment());

        Optional<Order> found = orderService.getOrderById(order.getId());

        assertTrue(found.isPresent());
        assertEquals(order.getId(), found.get().getId());
    }

    @Test
    void getOrderById_notFound_returnsEmpty() {
        assertTrue(orderService.getOrderById(999L).isEmpty());
    }

    // ── getOrdersByCustomer ───────────────────────────────────────────────────

    @Test
    void getOrdersByCustomer_returnsOnlyCustomerOrders() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        orderService.placeOrder(customer.getId(), new CashPayment());

        List<Order> orders = orderService.getOrdersByCustomer(customer.getId());

        assertEquals(1, orders.size());
        assertEquals(customer.getId(), orders.get(0).getCustomerId());
    }

    // ── getOrdersByStatus ─────────────────────────────────────────────────────

    @Test
    void getOrdersByStatus_returnsPaidOrders() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        orderService.placeOrder(customer.getId(), new CashPayment());

        List<Order> paidOrders = orderService.getOrdersByStatus(OrderStatus.PAID);

        assertEquals(1, paidOrders.size());
        assertEquals(OrderStatus.PAID, paidOrders.get(0).getStatus());
    }

    // ── getAllOrders ──────────────────────────────────────────────────────────

    @Test
    void getAllOrders_returnsAllPlacedOrders() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        orderService.placeOrder(customer.getId(), new CashPayment());
        cartService.addItem(customer.getId(), menuItem.getId(), 2);
        orderService.placeOrder(customer.getId(), new CashPayment());

        assertEquals(2, orderService.getAllOrders().size());
    }

    // ── getOngoingOrders ──────────────────────────────────────────────────────

    @Test
    void getOngoingOrders_excludesCancelledAndDelivered() {
        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order1 = orderService.placeOrder(customer.getId(), new CashPayment());

        cartService.addItem(customer.getId(), menuItem.getId(), 1);
        Order order2 = orderService.placeOrder(customer.getId(), new CashPayment());
        orderService.cancelOrder(order2.getId());

        List<Order> ongoing = orderService.getOngoingOrders();

        assertEquals(1, ongoing.size());
        assertEquals(order1.getId(), ongoing.get(0).getId());
    }
}
