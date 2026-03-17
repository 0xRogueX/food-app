package com.fooddeliveryapp.config;


import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.service.AuthService;
import com.fooddeliveryapp.strategy.DiscountStrategy;
import com.fooddeliveryapp.strategy.NoDiscount;
import com.fooddeliveryapp.util.AppConstants;

public class SystemConfig {

    // Singleton (Eager Initialization)
    private static final SystemConfig INSTANCE = new SystemConfig();

    public static SystemConfig getInstance() {
        return INSTANCE;
    }

    // Runtime Configurable Fields
    private DiscountStrategy discountStrategy;
    private double taxRate;
    private double deliveryFee;

    // Private Constructor
    private SystemConfig() {
        this.discountStrategy = new NoDiscount();
        this.taxRate = AppConstants.DEFAULT_TAX_PERCENT;
        this.deliveryFee = AppConstants.DEFAULT_DELIVERY_FEE;
    }

    // SYSTEM INITIALIZATION
    public void initializeSystemDefaults(AuthService authService) {
        try {
            if (!authService.adminExists(AppConstants.DEFAULT_ADMIN_EMAIL)) {
                authService.registerAdmin(
                        AppConstants.DEFAULT_ADMIN_NAME,
                        AppConstants.DEFAULT_ADMIN_PHONE,
                        AppConstants.DEFAULT_ADMIN_EMAIL,
                        AppConstants.DEFAULT_ADMIN_PASSWORD
                );
                System.out.println("System Boot: Default Admin account initialized from System.");
            } else {
                System.out.println("Default admin already exists, skipping creation.");
            }
        } catch (FoodDeliveryException e) {
            System.out.println("System Boot Error: " + e.getMessage()); // unexpected error during boot
        }
    }

    // DiscountStrategy getter/setter
    public DiscountStrategy getDiscountStrategy() {
        return discountStrategy;
    }

    public void setDiscountStrategy(DiscountStrategy discountStrategy) {
        if (discountStrategy == null) {
            throw new IllegalArgumentException("DiscountStrategy cannot be null");
        }
        this.discountStrategy = discountStrategy;
    }

    // Tax rate getter/setter
    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        if (taxRate < 0) {
            throw new IllegalArgumentException("Tax rate must be >= 0");
        }
        this.taxRate = taxRate;
    }

    // Delivery fee getter/setter
    public double getDeliveryFee() {
        return deliveryFee;
    }

    public void setDeliveryFee(double deliveryFee) {
        if (deliveryFee < 0) {
            throw new IllegalArgumentException("Delivery fee must be >= 0");
        }
        this.deliveryFee = deliveryFee;
    }
}