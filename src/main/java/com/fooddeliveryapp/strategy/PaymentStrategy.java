package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.type.PaymentMode;

public interface PaymentStrategy {
    boolean pay(double amount);
    PaymentMode getPaymentType();
}
