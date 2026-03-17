package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.model.Order;

public class NoDiscount implements DiscountStrategy {

    @Override
    public double calculateDiscount(Order order) {
        return 0; // no discount
    }
}