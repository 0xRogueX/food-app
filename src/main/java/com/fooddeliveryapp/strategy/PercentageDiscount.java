package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.model.Order;

public class PercentageDiscount implements DiscountStrategy {

    private final double threshold;  // Minimum subtotal to apply discount
    private final double percentage; // Discount in %

    public PercentageDiscount(double threshold, double percentage) {
        this.threshold = threshold;
        this.percentage = percentage;
    }

    @Override
    public double calculateDiscount(Order order) {
        double subTotal = order.getSubTotal();

        if (subTotal >= threshold) {
            return subTotal * (percentage / 100);
        }
        return 0;
    }
}