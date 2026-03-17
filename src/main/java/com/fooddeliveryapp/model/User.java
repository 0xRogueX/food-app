package com.fooddeliveryapp.model;

import com.fooddeliveryapp.type.Role;

import java.time.LocalDateTime;

public abstract class User {

    protected Long id;
    protected String name;
    protected String phone;
    protected String email;
    protected String password;
    protected LocalDateTime createdAt;

    protected User(Long id, String name, String phone, String email, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.password = password;
        this.createdAt = LocalDateTime.now();
    }

    public abstract Role getRole();

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // setters for persistence frameworks / JDBC mapping
    public void setId(Long id) {
        this.id = id;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}