package com.fooddeliveryapp.repository;

import com.fooddeliveryapp.model.Cart;

public interface CartRepository {

    Cart findOrCreateCart(Long customerId);

    void addOrUpdateItem(Long customerId, Long menuItemId, String menuItemName, double price, int quantity);

    void updateItemQuantity(Long customerId, Long menuItemId, int quantity);

    void removeItem(Long customerId, Long menuItemId);

    void clearCart(Long customerId);
}
