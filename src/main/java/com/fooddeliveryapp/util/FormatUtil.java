package com.fooddeliveryapp.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class FormatUtil {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    private FormatUtil() {}

    public static String formatCurrency(double amount) {
        return String.format("₹%.2f", amount);
    }

    public static String formatDate(LocalDateTime dateTime) {
        return (dateTime == null) ? "N/A" : dateTime.format(DATE_FORMATTER);
    }
}