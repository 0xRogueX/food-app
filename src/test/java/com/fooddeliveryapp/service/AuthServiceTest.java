package com.fooddeliveryapp.service;

import com.fooddeliveryapp.JdbcTestBase;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Admin;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcUserRepository;
import com.fooddeliveryapp.service.impl.AuthServiceImpl;
import com.fooddeliveryapp.type.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceTest extends JdbcTestBase {

    private AuthService authService;

    @BeforeEach
    void setUp() {
        UserRepository userRepository = new JdbcUserRepository(connectionManager);
        authService = new AuthServiceImpl(userRepository);
    }

    // ── registerCustomer ──────────────────────────────────────────────────────

    @Test
    void registerCustomer_success_returnsCustomerWithId() {
        Customer c = authService.registerCustomer(
                "Alice", "9876543210", "alice@example.com", "pass123", "123 Main St");

        assertNotNull(c.getId());
        assertEquals("Alice", c.getName());
        assertEquals("alice@example.com", c.getEmail());
        assertEquals("123 Main St", c.getAddress());
    }

    @Test
    void registerCustomer_duplicateEmail_throwsUserAlreadyExists() {
        authService.registerCustomer("Alice", "9876543210", "alice@example.com", "pass123", "123 Main St");

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                authService.registerCustomer("Alice2", "9876543211", "alice@example.com", "pass456", "456 Side St"));

        assertEquals(ErrorType.USER_ALREADY_EXISTS, ex.getErrorType());
    }

    @Test
    void registerCustomer_invalidEmail_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerCustomer("Alice", "9876543210", "not-an-email", "pass123", "123 Main St"));
    }

    @Test
    void registerCustomer_invalidPhone_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerCustomer("Alice", "12345", "alice@example.com", "pass123", "123 Main St"));
    }

    @Test
    void registerCustomer_shortPassword_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerCustomer("Alice", "9876543210", "alice@example.com", "ab", "123 Main St"));
    }

    @Test
    void registerCustomer_blankName_throwsIllegalArgument() {
        assertThrows(IllegalArgumentException.class, () ->
                authService.registerCustomer("  ", "9876543210", "alice@example.com", "pass123", "123 Main St"));
    }

    // ── registerDeliveryAgent ─────────────────────────────────────────────────

    @Test
    void registerDeliveryAgent_success_returnsAgentWithId() {
        DeliveryAgent agent = authService.registerDeliveryAgent(
                "Bob", "9876543210", "bob@example.com", "pass123");

        assertNotNull(agent.getId());
        assertEquals("Bob", agent.getName());
        assertEquals("bob@example.com", agent.getEmail());
        assertTrue(agent.isAvailable());
    }

    @Test
    void registerDeliveryAgent_duplicateEmail_throwsUserAlreadyExists() {
        authService.registerDeliveryAgent("Bob", "9876543210", "bob@example.com", "pass123");

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                authService.registerDeliveryAgent("Bob2", "9876543211", "bob@example.com", "pass456"));

        assertEquals(ErrorType.USER_ALREADY_EXISTS, ex.getErrorType());
    }

    // ── registerAdmin ─────────────────────────────────────────────────────────

    @Test
    void registerAdmin_success_returnsAdminWithId() {
        Admin admin = authService.registerAdmin("Admin", "1111111111", "admin@food.com", "admin123");

        assertNotNull(admin.getId());
        assertEquals("Admin", admin.getName());
    }

    // ── adminExists ───────────────────────────────────────────────────────────

    @Test
    void adminExists_returnsFalse_whenNoAdminRegistered() {
        assertFalse(authService.adminExists("admin@food.com"));
    }

    @Test
    void adminExists_returnsTrue_afterAdminRegistered() {
        authService.registerAdmin("Admin", "1111111111", "admin@food.com", "admin123");
        assertTrue(authService.adminExists("admin@food.com"));
    }

    @Test
    void adminExists_returnsFalse_forCustomerEmail() {
        authService.registerCustomer("Alice", "9876543210", "alice@example.com", "pass123", "123 Main St");
        assertFalse(authService.adminExists("alice@example.com"));
    }

    // ── login ─────────────────────────────────────────────────────────────────

    @Test
    void login_success_returnsUser() {
        authService.registerCustomer("Alice", "9876543210", "alice@example.com", "pass123", "123 Main St");

        User user = authService.login("alice@example.com", "pass123");

        assertNotNull(user);
        assertEquals("alice@example.com", user.getEmail());
    }

    @Test
    void login_wrongEmail_throwsAuthenticationError() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                authService.login("unknown@example.com", "pass123"));

        assertEquals(ErrorType.AUTHENTICATION_ERROR, ex.getErrorType());
    }

    @Test
    void login_wrongPassword_throwsAuthenticationError() {
        authService.registerCustomer("Alice", "9876543210", "alice@example.com", "pass123", "123 Main St");

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                authService.login("alice@example.com", "wrongpass"));

        assertEquals(ErrorType.AUTHENTICATION_ERROR, ex.getErrorType());
    }
}
