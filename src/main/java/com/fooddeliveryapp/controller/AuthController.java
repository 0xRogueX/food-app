package com.fooddeliveryapp.controller;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.service.AuthService;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.util.ConsoleInput;
import com.fooddeliveryapp.util.InputUtil;

public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    public User login() {
        System.out.println("\n--- LOGIN ---");
        int attempts = 0;
        final int MAX_ATTEMPTS = 3;

        while (attempts < MAX_ATTEMPTS) {
            String email = ConsoleInput.getString("Email     : ");
            String password = ConsoleInput.getString("Password  : ");
            try {
                return authService.login(email, password);
            } catch (FoodDeliveryException e) {
                attempts++;
                int remaining = MAX_ATTEMPTS - attempts;
                if (remaining > 0) {
                    System.out.println("Error: " + e.getMessage() + " (" + remaining + " attempt(s) remaining)");
                } else {
                    System.out.println("Error: " + e.getMessage() + ". Returning to main menu.");
                }
            }
        }
        throw new FoodDeliveryException(ErrorType.AUTHENTICATION_ERROR, "Too many failed login attempts");
    }

    public void registerCustomer() {
        System.out.println("\n--- REGISTER CUSTOMER ---");
        while (true) {
            try {
                String name = InputUtil.requireNonBlank(ConsoleInput.getString("Name                  : "), "Name");
                String phone = InputUtil.validatePhone(ConsoleInput.getString("Phone (10 digits)     : "));
                String email = InputUtil.validateEmail(ConsoleInput.getString("Email                 : "));
                String password = InputUtil.validatePassword(ConsoleInput.getString("Password (min 4 chars): "));
                String address = InputUtil.requireNonBlank(ConsoleInput.getString("Address               : "), "Address");
                authService.registerCustomer(name, phone, email, password, address);
                System.out.println("Customer registration successful! You can now login.");
                return;
            } catch (FoodDeliveryException | IllegalArgumentException e) {
                System.out.println("Registration Failed: " + e.getMessage());
                String retry = ConsoleInput.getString("Try again? (y/n): ");
                if (!retry.equalsIgnoreCase("y")) return;
            }
        }
    }

    public void registerDeliveryAgent() {
        System.out.println("\n--- REGISTER DELIVERY AGENT ---");
        while (true) {
            try {
                String name = InputUtil.requireNonBlank(ConsoleInput.getString("Name                  : "), "Name");
                String phone = InputUtil.validatePhone(ConsoleInput.getString("Phone (10 digits)     : "));
                String email = InputUtil.validateEmail(ConsoleInput.getString("Email                 : "));
                String password = InputUtil.validatePassword(ConsoleInput.getString("Password (min 4 chars): "));
                authService.registerDeliveryAgent(name, phone, email, password);
                System.out.println("Delivery Agent registration successful! You can now login.");
                return;
            } catch (FoodDeliveryException | IllegalArgumentException e) {
                System.out.println("Registration Failed: " + e.getMessage());
                String retry = ConsoleInput.getString("Try again? (y/n): ");
                if (!retry.equalsIgnoreCase("y")) return;
            }
        }
    }
}