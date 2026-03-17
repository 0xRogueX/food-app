package com.fooddeliveryapp.util;

import java.util.regex.Pattern;

public final class AppConstants {

    private AppConstants() {}

    public static final double DEFAULT_DELIVERY_FEE    = 40.0;
    public static final double DEFAULT_TAX_PERCENT     = 5.0;

    public static final String DEFAULT_ADMIN_NAME      = "Admin";
    public static final String DEFAULT_ADMIN_PHONE     = "1111111111";
    public static final String DEFAULT_ADMIN_EMAIL     = "admin@food.com";
    public static final String DEFAULT_ADMIN_PASSWORD  = "admin123";

    public static final int    PASSWORD_MIN_LENGTH     = 4;

    public static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public static final Pattern PHONE_PATTERN =
            Pattern.compile("^\\d{10}$");
}