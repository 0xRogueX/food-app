package com.fooddeliveryapp.model;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private final Long customerId;
    private List<CartItem> items = new ArrayList<>();

    public Cart(Long  customerId) {
        this.customerId = customerId;
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalItems() {
        return items.stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
    }

    public double getSubTotal() {
        return items.stream()
                .mapToDouble(CartItem::getLineTotal)
                .sum();
    }

}