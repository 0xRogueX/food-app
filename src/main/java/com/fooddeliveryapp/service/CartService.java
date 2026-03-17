package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.Cart;

public interface CartService {

    Cart getCart(Long customerId);

    void addItem(Long customerId, Long menuItemId, int quantity);

    void removeItem(Long customerId, Long menuItemId);

    void updateItemQuantity(Long customerId, Long menuItemId, int quantity);

    void clearCart(Long customerId);

    double getTotalPrice(Long customerId);

    int getTotalItems(Long customerId);
}