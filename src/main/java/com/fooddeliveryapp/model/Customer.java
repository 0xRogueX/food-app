package com.fooddeliveryapp.model;

import com.fooddeliveryapp.type.Role;

public class Customer extends User {

    private String address;
    private Cart activeCart;

    public Customer(Long id, String name, String phone, String email, String address, String password) {
        super(id, name, phone, email, password);
        this.address = address;
        this.activeCart = new Cart(id);
    }

    @Override
    public Role getRole() {
        return Role.CUSTOMER;
    }

    @Override
    public void setId(Long id) {
        super.setId(id);
        this.activeCart = new Cart(id);
    }

    public String getAddress() { return address; }
    public Cart getActiveCart() { return activeCart; }
}