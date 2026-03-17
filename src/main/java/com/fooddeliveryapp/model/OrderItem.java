package com.fooddeliveryapp.model;

public class OrderItem {

    private final Long menuItemId;
    private final String menuItemName;
    private final double priceAtPurchase;
    private final int quantity;

    public OrderItem(Long menuItemId, String menuItemName, double priceAtPurchase, int quantity) {
        this.menuItemId = menuItemId;
        this.menuItemName = menuItemName;
        this.priceAtPurchase = priceAtPurchase;
        this.quantity = quantity;
    }

    public Long getFoodItemId() {
        return menuItemId;
    }

    public String getFoodItemName() {
        return menuItemName;
    }

    public double getPriceAtPurchase() {
        return priceAtPurchase;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getLineTotal() {
        return priceAtPurchase * quantity;
    }
}