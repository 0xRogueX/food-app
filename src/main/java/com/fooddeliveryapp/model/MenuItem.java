package com.fooddeliveryapp.model;

public class MenuItem {

    private Long id;
    private String name;
    private double price;
    private Long categoryId;
    private boolean available;

    public MenuItem(Long id, String name, double price, Long categoryId) {

        this.id = id;
        this.name = name;
        this.price = price;
        this.categoryId = categoryId;
        this.available = true;
    }

    public Long getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public double getPrice() {
        return price;
    }
    public Long getCategoryId() {
        return categoryId;
    }
    public boolean isAvailable() {
        return available;
    }

    public void updatePrice(double newPrice) {
        this.price = newPrice;
    }
    public void rename(String newName) {
        this.name = newName;
    }
    public void changeAvailability(boolean status) {
        this.available = status;
    }
    public void changeCategory(Long newCategoryId) {
        this.categoryId = newCategoryId;
    }

    public void setId(Long id) {
        this.id = id;
    }

}