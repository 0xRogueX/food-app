package com.fooddeliveryapp.strategy;

import com.fooddeliveryapp.type.PaymentMode;

public class UPIPayment implements PaymentStrategy {

    private final String upiId;

    public UPIPayment(String upiId) {
        this.upiId = upiId;
    }

    @Override
    public boolean pay(double amount) {

        System.out.println("Processing UPI payment of ₹" + amount);
        System.out.println("UPI ID: " + upiId);

        return true;
    }

    @Override
    public PaymentMode getPaymentType() {
        return PaymentMode.UPI;
    }
}