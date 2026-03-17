package com.fooddeliveryapp.exception;

import com.fooddeliveryapp.type.ErrorType;

public class FoodDeliveryException extends RuntimeException {

    private final ErrorType errorType;

    public FoodDeliveryException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public FoodDeliveryException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }

    public ErrorType getErrorType() {
        return errorType;
    }
}