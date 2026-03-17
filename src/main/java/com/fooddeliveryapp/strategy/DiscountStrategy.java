package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.model.Order;

public interface DiscountStrategy {

    double calculateDiscount(Order order);
}
