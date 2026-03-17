package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.Admin;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.DeliveryAgent;
import com.fooddeliveryapp.model.User;

public interface AuthService {

    User login(String email, String password);

    Customer registerCustomer(String name, String phone, String email, String password, String address);

    DeliveryAgent registerDeliveryAgent(String name, String phone, String email, String password);

    Admin registerAdmin(String name, String phone, String email, String password);

    boolean adminExists(String defaultAdminEmail);
}