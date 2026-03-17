package com.fooddeliveryapp.model;

public class CartItem {

    private Long menuItemId;
    private String menuItemName;
    private double price;
    private int quantity;

    public CartItem(Long menuItemId, String menuItemName, double price, int quantity) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.price = price;
        this.quantity = quantity;
    }

    public Long getMenuItemId() {
        return menuItemId;
    }

    public String getMenuItemName() {
        return menuItemName;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be greater than zero");
        this.quantity = quantity;
    }

    public double getLineTotal() {
        return price * quantity;
    }
}
