package com.fooddeliveryapp.service.impl;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Cart;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.repository.CartRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.service.CartService;
import com.fooddeliveryapp.type.ErrorType;

public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final MenuItemRepository menuItemRepository;

    public CartServiceImpl(CartRepository cartRepository, MenuItemRepository menuItemRepository) {
        this.cartRepository = cartRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public Cart getCart(Long customerId) {
        return cartRepository.findOrCreateCart(customerId);
    }

    @Override
    public void addItem(Long customerId, Long menuItemId, int quantity) {
        if (quantity <= 0) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, "Quantity must be at least 1");
        }

        MenuItem menuItem = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Menu item not found: " + menuItemId));

        if (!menuItem.isAvailable()) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, menuItem.getName() + " is currently unavailable");
        }

        cartRepository.addOrUpdateItem(customerId, menuItemId, menuItem.getName(), menuItem.getPrice(), quantity);
    }

    @Override
    public void removeItem(Long customerId, Long menuItemId) {
        Cart cart = cartRepository.findOrCreateCart(customerId);
        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getMenuItemId().equals(menuItemId));

        if (!exists) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, "Item not found in cart");
        }

        cartRepository.removeItem(customerId, menuItemId);
    }

    @Override
    public void updateItemQuantity(Long customerId, Long menuItemId, int quantity) {
        if (quantity <= 0) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, "Quantity must be at least 1");
        }

        Cart cart = cartRepository.findOrCreateCart(customerId);
        boolean exists = cart.getItems().stream()
                .anyMatch(item -> item.getMenuItemId().equals(menuItemId));

        if (!exists) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, "Item not found in cart");
        }

        cartRepository.updateItemQuantity(customerId, menuItemId, quantity);
    }

    @Override
    public void clearCart(Long customerId) {
        cartRepository.clearCart(customerId);
    }

    @Override
    public double getTotalPrice(Long customerId) {
        Cart cart = cartRepository.findOrCreateCart(customerId);
        return cart.getItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    @Override
    public int getTotalItems(Long customerId) {
        return cartRepository.findOrCreateCart(customerId).getTotalItems();
    }
}