package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Cart;
import com.fooddeliveryapp.model.CartItem;
import com.fooddeliveryapp.model.Customer;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.CartRepository;
import com.fooddeliveryapp.repository.UserRepository;
import com.fooddeliveryapp.type.ErrorType;

public class InMemoryCartRepository implements CartRepository {

    private final UserRepository userRepository;

    public InMemoryCartRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private Cart getCart(Long customerId) {
        User user = userRepository.findById(customerId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Customer not found"));
        if (!(user instanceof Customer customer)) {
            throw new FoodDeliveryException(ErrorType.CART_ERROR, "Invalid operation: User is not a customer");
        }
        return customer.getActiveCart();
    }

    @Override
    public Cart findOrCreateCart(Long customerId) {
        return getCart(customerId);
    }

    @Override
    public void addOrUpdateItem(Long customerId, Long menuItemId, String menuItemName, double price, int quantity) {
        Cart cart = getCart(customerId);
        cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .ifPresentOrElse(
                        existing -> existing.setQuantity(existing.getQuantity() + quantity),
                        () -> cart.getItems().add(new CartItem(menuItemId, menuItemName, price, quantity))
                );
    }

    @Override
    public void updateItemQuantity(Long customerId, Long menuItemId, int quantity) {
        Cart cart = getCart(customerId);
        cart.getItems().stream()
                .filter(item -> item.getMenuItemId().equals(menuItemId))
                .findFirst()
                .ifPresent(item -> item.setQuantity(quantity));
    }

    @Override
    public void removeItem(Long customerId, Long menuItemId) {
        getCart(customerId).getItems().removeIf(item -> item.getMenuItemId().equals(menuItemId));
    }

    @Override
    public void clearCart(Long customerId) {
        getCart(customerId).clear();
    }
}
