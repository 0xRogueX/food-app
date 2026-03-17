package com.fooddeliveryapp.service.impl;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.*;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.service.AuthService;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.util.InputUtil;

public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;

    public AuthServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.AUTHENTICATION_ERROR, "User not found with this email"));

        if (!user.getPassword().equals(password)) {
            throw new FoodDeliveryException(ErrorType.AUTHENTICATION_ERROR, "Invalid password");
        }
        return user;
    }

    @Override
    public Customer registerCustomer(String name, String phone, String email, String password, String address) {
        // Validate FIRST to prevent wasting IDs
        InputUtil.requireNonBlank(name, "Name");
        InputUtil.validatePhone(phone);
        InputUtil.validateEmail(email);
        InputUtil.validatePassword(password);
        InputUtil.requireNonBlank(address, "Address");
        checkEmailExists(email);

        Customer customer = new Customer(null, name, phone, email, address, password);
        return (Customer) userRepository.save(customer);
    }

    @Override
    public DeliveryAgent registerDeliveryAgent(String name, String phone, String email, String password) {
        InputUtil.requireNonBlank(name, "Name");
        InputUtil.validatePhone(phone);
        InputUtil.validateEmail(email);
        InputUtil.validatePassword(password);
        checkEmailExists(email);

        DeliveryAgent agent = new DeliveryAgent(null, name, phone, email, password);
        return (DeliveryAgent) userRepository.save(agent);
    }

    @Override
    public Admin registerAdmin(String name, String phone, String email, String password) {
        checkEmailExists(email);
        Admin admin = new Admin(null, name, phone, email, password);
        return (Admin) userRepository.save(admin);
    }

    @Override
    public boolean adminExists(String defaultAdminEmail) {
        return userRepository.findByEmail(defaultAdminEmail)
                .filter(user -> user instanceof Admin)
                .isPresent();
    }

    private void checkEmailExists(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new FoodDeliveryException(ErrorType.USER_ALREADY_EXISTS, "Email is already registered");
        }
    }
}