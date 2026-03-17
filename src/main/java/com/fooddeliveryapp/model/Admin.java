package com.fooddeliveryapp.model;

import com.fooddeliveryapp.type.Role;

public class Admin extends User {

    public Admin(Long id, String name, String phone, String email, String password) {
        super(id, name, phone, email, password);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}