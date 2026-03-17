package com.fooddeliveryapp.util;

import java.util.Objects;

import static com.fooddeliveryapp.util.AppConstants.*;

public final class InputUtil {

    private InputUtil() {}

    // ==========================
    // Common Null / Blank Checks
    // ==========================

    public static String requireNonBlank(String value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " cannot be null");

        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " cannot be empty");
        }

        return value.trim();
    }

    public static void requirePositive(double value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    public static void requireNonNegative(double value, String fieldName) {
        if (value < 0) {
            throw new IllegalArgumentException(fieldName + " cannot be negative");
        }
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than zero");
        }
    }

    // ==========================
    // Email Validation
    // ==========================

    public static String validateEmail(String email) {
        email = requireNonBlank(email, "Email");

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }

        return email;
    }

    // ==========================
    // Phone Validation (India simple)
    // ==========================

    public static String validatePhone(String phone) {
        phone = requireNonBlank(phone, "Phone");

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Invalid phone number");
        }

        return phone;
    }

    // ==========================
    // Password Validation
    // ==========================

    public static String validatePassword(String password) {
        password = requireNonBlank(password, "Password");

        if (password.length() < PASSWORD_MIN_LENGTH ) {
            throw new IllegalArgumentException("Password must be at least " + PASSWORD_MIN_LENGTH + " characters");
        }

        return password;
    }

    // ==========================
    // Rating Validation
    // ==========================

    public static void validateRating(double rating) {
        if (rating < 1.0 || rating > 5.0) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }
    }

}