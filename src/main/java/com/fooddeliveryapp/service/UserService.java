package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.type.Role;

import java.util.List;
import java.util.Optional;

public interface UserService {

    // Generic User Queries
    Optional<User> getUserById(Long id);
    Optional<User> getUserByEmail(String email);
    List<User> getAllUsers();
    boolean existsById(Long id);
    void deleteUserById(Long id);

    // Role-based Queries
    List<User> getUsersByRole(Role role);

    // Delivery Agent Specific
    List<DeliveryAgent> getAllDeliveryAgents();
    List<DeliveryAgent> getAvailableDeliveryAgents();
    Optional<DeliveryAgent> getNextAvailableDeliveryAgent();
}