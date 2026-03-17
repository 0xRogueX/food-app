package com.fooddeliveryapp;

import com.fooddeliveryapp.config.DbConfig;
import com.fooddeliveryapp.config.PersistenceMode;
import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.controller.*;
import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.*;
import com.fooddeliveryapp.repository.inmemory.InMemoryRepositoryFactory;
import com.fooddeliveryapp.repository.jdbc.JdbcRepositoryFactory;
import com.fooddeliveryapp.service.*;
import com.fooddeliveryapp.service.impl.*;
import com.fooddeliveryapp.type.Role;
import com.fooddeliveryapp.util.ConsoleInput;

import java.util.Properties;

public class Application {

    // Controllers
    private static AuthController authController;
    private static AdminController adminController;
    private static CustomerController customerController;
    private static DeliveryAgentController agentController;

    public static void main(String[] args) {

        initializeDependencies();

        // 5. Main Application Loop
        while (true) {
            System.out.println("\n=======================================");
            System.out.println("        WELCOME TO FOOD APP        ");
            System.out.println("=======================================");
            System.out.println("1. Login");
            System.out.println("2. Register as Customer");
            System.out.println("3. Register as Delivery Agent");
            System.out.println("4. Exit");
            System.out.println("=======================================");

            int choice = ConsoleInput.getInt("Select an option: ");

            try {
                switch (choice) {
                    case 1 -> {
                        User user = authController.login();
                        System.out.println("Login Successful! Welcome, " + user.getName());

                        // Route to the correct dashboard based on Role
                        if      (user.getRole() == Role.ADMIN)          adminController.start(user);
                        else if (user.getRole() == Role.CUSTOMER)       customerController.start(user);
                        else if (user.getRole() == Role.DELIVERY_AGENT) agentController.start(user);
                    }
                    case 2 -> authController.registerCustomer();
                    case 3 -> authController.registerDeliveryAgent();
                    case 4 -> {
                        System.out.println("Goodbye! Have a great day!");
                        System.exit(0);
                    }
                    default -> System.out.println("Invalid choice. Please try again.");
                }
            } catch (FoodDeliveryException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("System Error: " + e.getMessage());
            }
        }
    }

    public static void initializeDependencies() {
        // 1. Choose persistence mode and initialize repositories via factory
        Properties dbProps = DbConfig.loadProperties();
        String modeStr = dbProps.getProperty("persistence.mode", "JDBC").trim().toUpperCase();
        PersistenceMode mode;
        try {
            mode = PersistenceMode.valueOf(modeStr);
        } catch (IllegalArgumentException e) {
            System.out.println("[WARN] Invalid persistence.mode in db.properties: " + modeStr + ". Defaulting to IN_MEMORY.");
            mode = PersistenceMode.IN_MEMORY;
        }
        System.out.println("[INFO] Persistence mode: " + mode);

        RepositoryFactory factory = switch (mode) {
            case IN_MEMORY -> new InMemoryRepositoryFactory();
            case JDBC -> new JdbcRepositoryFactory();
        };

        UserRepository          userRepo     = factory.userRepository();
        DeliveryAgentRepository agentRepo    = factory.deliveryAgentRepository();
        CategoryRepository      categoryRepo = factory.categoryRepository();
        MenuItemRepository      menuRepo     = factory.menuItemRepository();
        OrderRepository         orderRepo    = factory.orderRepository();
        PaymentRepository       paymentRepo  = factory.paymentRepository();
        CartRepository          cartRepo     = factory.cartRepository();

        // 2. Initialize Services
        AuthService     authService     = new AuthServiceImpl(userRepo);
        UserService     userService     = new UserServiceImpl(userRepo, agentRepo);
        MenuService     menuService     = new MenuServiceImpl(categoryRepo, menuRepo);
        CartService     cartService     = new CartServiceImpl(cartRepo, menuRepo);
        PaymentService  paymentService  = new PaymentServiceImpl(paymentRepo);
        OrderService    orderService    = new OrderServiceImpl(orderRepo, cartService, paymentService, menuRepo, userRepo);
        DeliveryService deliveryService = new DeliveryServiceImpl(agentRepo, userRepo, orderService);

        // 3. Initialize Controllers
        authController      = new AuthController(authService);
        adminController     = new AdminController(menuService, userService, orderService, paymentService);
        customerController  = new CustomerController(menuService, cartService, orderService, paymentService, deliveryService);
        agentController     = new DeliveryAgentController(orderService, deliveryService, userService);


        // 4. Seed Default System Data directly via SystemConfig
        SystemConfig.getInstance().initializeSystemDefaults(authService);
    }
}
