package com.fooddeliveryapp.controller;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.service.DeliveryService;
import com.fooddeliveryapp.service.OrderService;
import com.fooddeliveryapp.service.UserService;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.type.OrderStatus;
import com.fooddeliveryapp.util.ConsoleInput;
import com.fooddeliveryapp.util.FormatUtil;
import com.fooddeliveryapp.util.TablePrinter;

import java.util.List;

public class DeliveryAgentController {
    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private final UserService userService;

    public DeliveryAgentController(OrderService orderService, DeliveryService deliveryService,
                                   UserService userService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
        this.userService = userService;
    }

    public void start(User user) {
        final Long agentId = user.getId();

        while (true) {
            // Re-fetch agent from DB on every iteration so data is always current
            DeliveryAgent agent = userService.getAllDeliveryAgents().stream()
                    .filter(a -> a.getId().equals(agentId))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Agent with ID " + agentId + " not found in database"));

            System.out.println("\n=======================================");
            System.out.println("       DELIVERY AGENT DASHBOARD      ");
            System.out.println("=======================================");
            System.out.println("1. View Profile & Earnings");
            System.out.println("2. View Assigned Orders");
            System.out.println("3. Update Order Status");
            System.out.println("4. Logout");
            System.out.println("=======================================");

            int choice = ConsoleInput.getInt("Select an option: ");

            try {
                switch (choice) {
                    case 1 -> viewProfile(agent);
                    case 2 -> viewAssignedOrders(agent);
                    case 3 -> updateOrderStatus(agent);
                    case 4 -> {
                        System.out.println("Logging out...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            } catch (FoodDeliveryException | IllegalArgumentException e) {
                System.out.println("Error: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Unexpected error: " + e.getMessage());
            }
        }
    }

    private void viewProfile(DeliveryAgent agent) {
        System.out.println("\n--- Profile ---");
        System.out.println("Name            : " + agent.getName());
        System.out.println("Status          : " + (agent.isAvailable() ? "Available" : "Busy"));
        System.out.println("Rating          : " + String.format("%.1f", agent.getRating()));
        System.out.println("Total Deliveries: " + agent.getTotalDeliveries());
        // Assuming agent gets the delivery fee as earnings
        double earnings = agent.getTotalDeliveries() * com.fooddeliveryapp.config.SystemConfig.getInstance().getDeliveryFee();
        System.out.println("Estimated Earnings: " + FormatUtil.formatCurrency(earnings));
    }

    private void viewAssignedOrders(DeliveryAgent agent) {
        List<Order> myOrders = orderService.getOrdersByDeliveryAgent(agent.getId());
        if (myOrders.isEmpty()) {
            System.out.println("\nNo orders currently assigned to you.");
        } else {
            List<String[]> rows = myOrders.stream()
                    .map(o -> new String[]{
                            String.valueOf(o.getId()),
                            String.valueOf(o.getCustomerId()),
                            o.getStatus().name(),
                            FormatUtil.formatCurrency(o.getFinalAmount())
                    })
                    .toList();
            TablePrinter.print(new String[]{"Order ID", "Customer ID", "Status", "Amount"}, rows);
        }
    }

    private void updateOrderStatus(DeliveryAgent agent) {
        long orderIdLong = ConsoleInput.getLong("Enter Order ID: ");

        Order order = orderService.getOrderById(orderIdLong)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Order not found with ID: " + orderIdLong));

        if (order.getDeliveryAgentId() == null || !order.getDeliveryAgentId().equals(agent.getId())) {
            System.out.println("Error: You are not assigned to this order.");
            return;
        }

        System.out.println("1. Mark Out for Delivery \n2. Mark Delivered");
        int choice = ConsoleInput.getInt("Choose: ");

        if (choice == 1) {
            if (order.getStatus() != OrderStatus.ASSIGNED) {
                System.out.println("Error: Order must be in ASSIGNED status to mark Out for Delivery. Current: " + order.getStatus());
                return;
            }
            deliveryService.markOrderOutForDelivery(orderIdLong);
            System.out.println("Order is out for delivery!");
        } else if (choice == 2) {
            if (order.getStatus() != OrderStatus.OUT_FOR_DELIVERY) {
                System.out.println("Error: Order must be Out for Delivery before marking Delivered. Current: " + order.getStatus());
                return;
            }
            deliveryService.markOrderDelivered(orderIdLong);
            System.out.println("Order delivered successfully! You are now available for new orders.");
        } else {
            System.out.println("Invalid choice.");
        }
    }
}