package com.fooddeliveryapp.service;

import com.fooddeliveryapp.JdbcTestBase;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Cart;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.repository.CartRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcCartRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcCategoryRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcMenuItemRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcUserRepository;
import com.fooddeliveryapp.service.impl.CartServiceImpl;
import com.fooddeliveryapp.type.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartServiceTest extends JdbcTestBase {

    private CartService cartService;
    private UserRepository userRepository;
    private MenuItemRepository menuItemRepository;
    private Customer customer;
    private MenuItem availableItem;

    @BeforeEach
    void setUp() {
        userRepository = new JdbcUserRepository(connectionManager);
        JdbcCategoryRepository categoryRepo = new JdbcCategoryRepository(connectionManager);
        menuItemRepository = new JdbcMenuItemRepository(connectionManager, categoryRepo);
        CartRepository cartRepository = new JdbcCartRepository(connectionManager);
        cartService = new CartServiceImpl(cartRepository, menuItemRepository);

        // Seed a category and menu item
        Category category = new Category(null, "Pizza");
        categoryRepo.save(category);
        availableItem = new MenuItem(null, "Margherita", 200.0, category.getId());
        menuItemRepository.save(availableItem);

        // Seed a customer
        customer = new Customer(null, "Alice", "9876543210", "alice@example.com", "123 Main St", "pass123");
        userRepository.save(customer);
    }

    // ── getCart ───────────────────────────────────────────────────────────────

    @Test
    void getCart_emptyOnFirstAccess() {
        Cart cart = cartService.getCart(customer.getId());

        assertNotNull(cart);
        assertTrue(cart.isEmpty());
    }

    // ── addItem ───────────────────────────────────────────────────────────────

    @Test
    void addItem_success_itemAppearsInCart() {
        cartService.addItem(customer.getId(), availableItem.getId(), 2);
        Cart cart = cartService.getCart(customer.getId());

        assertFalse(cart.isEmpty());
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_addSameItemTwice_quantityAccumulates() {
        cartService.addItem(customer.getId(), availableItem.getId(), 2);
        cartService.addItem(customer.getId(), availableItem.getId(), 3);

        Cart cart = cartService.getCart(customer.getId());
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_zeroQuantity_throwsCartError() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                cartService.addItem(customer.getId(), availableItem.getId(), 0));

        assertEquals(ErrorType.CART_ERROR, ex.getErrorType());
    }

    @Test
    void addItem_negativeQuantity_throwsCartError() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                cartService.addItem(customer.getId(), availableItem.getId(), -1));

        assertEquals(ErrorType.CART_ERROR, ex.getErrorType());
    }

    @Test
    void addItem_unavailableItem_throwsCartError() {
        availableItem.changeAvailability(false); // mark unavailable and persist to DB
        menuItemRepository.save(availableItem);

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                cartService.addItem(customer.getId(), availableItem.getId(), 1));

        assertEquals(ErrorType.CART_ERROR, ex.getErrorType());
    }

    @Test
    void addItem_nonExistentItem_throwsResourceNotFound() {
        assertThrows(FoodDeliveryException.class, () ->
                cartService.addItem(customer.getId(), 999L, 1));
    }

    // ── removeItem ────────────────────────────────────────────────────────────

    @Test
    void removeItem_success_cartBecomesEmpty() {
        cartService.addItem(customer.getId(), availableItem.getId(), 1);
        cartService.removeItem(customer.getId(), availableItem.getId());

        assertTrue(cartService.getCart(customer.getId()).isEmpty());
    }

    @Test
    void removeItem_itemNotInCart_throwsCartError() {
        assertThrows(FoodDeliveryException.class, () ->
                cartService.removeItem(customer.getId(), availableItem.getId()));
    }

    // ── updateItemQuantity ────────────────────────────────────────────────────

    @Test
    void updateItemQuantity_success_updatesQuantity() {
        cartService.addItem(customer.getId(), availableItem.getId(), 1);
        cartService.updateItemQuantity(customer.getId(), availableItem.getId(), 5);

        assertEquals(5, cartService.getCart(customer.getId()).getItems().get(0).getQuantity());
    }

    @Test
    void updateItemQuantity_zeroQuantity_throwsCartError() {
        cartService.addItem(customer.getId(), availableItem.getId(), 1);

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                cartService.updateItemQuantity(customer.getId(), availableItem.getId(), 0));

        assertEquals(ErrorType.CART_ERROR, ex.getErrorType());
    }

    @Test
    void updateItemQuantity_itemNotInCart_throwsCartError() {
        assertThrows(FoodDeliveryException.class, () ->
                cartService.updateItemQuantity(customer.getId(), availableItem.getId(), 3));
    }

    // ── clearCart ─────────────────────────────────────────────────────────────

    @Test
    void clearCart_removesAllItems() {
        cartService.addItem(customer.getId(), availableItem.getId(), 3);
        cartService.clearCart(customer.getId());

        assertTrue(cartService.getCart(customer.getId()).isEmpty());
    }

    // ── getTotalPrice ─────────────────────────────────────────────────────────

    @Test
    void getTotalPrice_calculatesCorrectTotal() {
        cartService.addItem(customer.getId(), availableItem.getId(), 3);

        // 200.0 × 3 = 600.0
        assertEquals(600.0, cartService.getTotalPrice(customer.getId()));
    }

    @Test
    void getTotalPrice_emptyCart_returnsZero() {
        assertEquals(0.0, cartService.getTotalPrice(customer.getId()));
    }

    // ── getTotalItems ─────────────────────────────────────────────────────────

    @Test
    void getTotalItems_returnsCorrectCount() {
        cartService.addItem(customer.getId(), availableItem.getId(), 4);

        assertEquals(4, cartService.getTotalItems(customer.getId()));
    }

    @Test
    void getTotalItems_emptyCart_returnsZero() {
        assertEquals(0, cartService.getTotalItems(customer.getId()));
    }
}
