package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.DeliveryAgentRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryDeliveryAgentRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryUserRepository;
import com.fooddeliveryapp.service.impl.UserServiceImpl;
import com.fooddeliveryapp.type.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository = new InMemoryUserRepository();
        DeliveryAgentRepository agentRepository = new InMemoryDeliveryAgentRepository(userRepository);
        userService = new UserServiceImpl(userRepository, agentRepository);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Customer saveCustomer(String email) {
        Customer c = new Customer(null, "Alice", "9876543210", email, "123 Main St", "pass123");
        return (Customer) userRepository.save(c);
    }

    private DeliveryAgent saveAgent(String email) {
        DeliveryAgent a = new DeliveryAgent(null, "Bob", "9876543210", email, "pass123");
        return (DeliveryAgent) userRepository.save(a);
    }

    // ── getUserById ───────────────────────────────────────────────────────────

    @Test
    void getUserById_found_returnsUser() {
        Customer c = saveCustomer("alice@example.com");

        Optional<User> result = userService.getUserById(c.getId());

        assertTrue(result.isPresent());
        assertEquals("alice@example.com", result.get().getEmail());
    }

    @Test
    void getUserById_notFound_returnsEmpty() {
        assertTrue(userService.getUserById(999L).isEmpty());
    }

    // ── getUserByEmail ────────────────────────────────────────────────────────

    @Test
    void getUserByEmail_found_returnsUser() {
        saveCustomer("alice@example.com");

        assertTrue(userService.getUserByEmail("alice@example.com").isPresent());
    }

    @Test
    void getUserByEmail_notFound_returnsEmpty() {
        assertTrue(userService.getUserByEmail("nobody@example.com").isEmpty());
    }

    // ── getAllUsers ───────────────────────────────────────────────────────────

    @Test
    void getAllUsers_returnsSavedUsers() {
        saveCustomer("alice@example.com");
        saveCustomer("bob@example.com");

        assertEquals(2, userService.getAllUsers().size());
    }

    // ── existsById ────────────────────────────────────────────────────────────

    @Test
    void existsById_trueForSavedUser() {
        Customer c = saveCustomer("alice@example.com");
        assertTrue(userService.existsById(c.getId()));
    }

    @Test
    void existsById_falseForUnknownId() {
        assertFalse(userService.existsById(999L));
    }

    // ── deleteUserById ────────────────────────────────────────────────────────

    @Test
    void deleteUserById_removesUser() {
        Customer c = saveCustomer("alice@example.com");
        userService.deleteUserById(c.getId());

        assertFalse(userService.existsById(c.getId()));
    }

    // ── getUsersByRole ────────────────────────────────────────────────────────

    @Test
    void getUsersByRole_customer_returnsOnlyCustomers() {
        saveCustomer("alice@example.com");
        saveAgent("bob@example.com");

        List<User> customers = userService.getUsersByRole(Role.CUSTOMER);

        assertEquals(1, customers.size());
        assertEquals(Role.CUSTOMER, customers.get(0).getRole());
    }

    @Test
    void getUsersByRole_deliveryAgent_returnsOnlyAgents() {
        saveCustomer("alice@example.com");
        saveAgent("bob@example.com");

        List<User> agents = userService.getUsersByRole(Role.DELIVERY_AGENT);

        assertEquals(1, agents.size());
        assertEquals(Role.DELIVERY_AGENT, agents.get(0).getRole());
    }

    // ── getAllDeliveryAgents ──────────────────────────────────────────────────

    @Test
    void getAllDeliveryAgents_returnsOnlyAgents() {
        saveCustomer("alice@example.com");
        saveAgent("bob@example.com");

        List<DeliveryAgent> agents = userService.getAllDeliveryAgents();

        assertEquals(1, agents.size());
    }

    // ── getAvailableDeliveryAgents ────────────────────────────────────────────

    @Test
    void getAvailableDeliveryAgents_returnsAvailableAgent() {
        saveAgent("bob@example.com");

        List<DeliveryAgent> available = userService.getAvailableDeliveryAgents();

        assertEquals(1, available.size());
        assertTrue(available.get(0).isAvailable());
    }

    @Test
    void getAvailableDeliveryAgents_excludesBusyAgent() {
        DeliveryAgent agent = saveAgent("bob@example.com");
        agent.markBusy();
        userRepository.save(agent);

        assertTrue(userService.getAvailableDeliveryAgents().isEmpty());
    }

    // ── getNextAvailableDeliveryAgent ─────────────────────────────────────────

    @Test
    void getNextAvailableDeliveryAgent_returnsFirstAvailable() {
        DeliveryAgent agent = saveAgent("bob@example.com");

        Optional<DeliveryAgent> next = userService.getNextAvailableDeliveryAgent();

        assertTrue(next.isPresent());
        assertEquals(agent.getId(), next.get().getId());
    }

    @Test
    void getNextAvailableDeliveryAgent_noAgents_returnsEmpty() {
        assertTrue(userService.getNextAvailableDeliveryAgent().isEmpty());
    }
}
