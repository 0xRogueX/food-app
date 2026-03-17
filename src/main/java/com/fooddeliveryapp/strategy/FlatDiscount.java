package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.model.Order;

public class FlatDiscount implements DiscountStrategy {

    private final double threshold;      // Minimum subtotal to apply discount
    private final double discountAmount; // Fixed discount amount

    public FlatDiscount(double threshold, double discountAmount) {
        this.threshold = threshold;
        this.discountAmount = discountAmount;
    }

    @Override
    public double calculateDiscount(Order order) {
        if (order.getSubTotal() >= threshold) {
            return discountAmount;
        }
        return 0;
    }
}