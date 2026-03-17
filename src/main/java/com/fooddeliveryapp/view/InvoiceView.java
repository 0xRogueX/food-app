package com.fooddeliveryapp.view;

import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.model.OrderItem;
import com.fooddeliveryapp.model.Payment;
import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.util.FormatUtil;

import java.util.List;

public class InvoiceView {

    public static void printInvoice(Order order, List<Payment> payments, User customer) {
        System.out.println("\n====================================================");
        System.out.println("                 TAX INVOICE                        ");
        System.out.println("====================================================");
        System.out.println("Order No    : " + order.getOrderNumber());
        System.out.println("Date        : " + FormatUtil.formatDate(order.getCreatedAt()));
        System.out.println("Customer    : " + customer.getName() + " (" + customer.getPhone() + ")");
        System.out.println("Status      : " + order.getStatus());
        System.out.println("----------------------------------------------------");

        System.out.printf("%-30s %-10s %-10s\n", "Item", "Qty", "Price");
        System.out.println("----------------------------------------------------");

        for (OrderItem item : order.getItems()) {
            System.out.printf("%-30s %-10d %-10s\n",
                    item.getFoodItemName(),
                    item.getQuantity(),
                    FormatUtil.formatCurrency(item.getLineTotal()));
        }

        System.out.println("----------------------------------------------------");
        System.out.printf("%-41s %-10s\n", "Subtotal:", FormatUtil.formatCurrency(order.getSubTotal()));
        System.out.printf("%-41s %-10s\n", "Discount:", "-" + FormatUtil.formatCurrency(order.getDiscountAmount()));
        System.out.printf("%-41s %-10s\n", "Tax:", FormatUtil.formatCurrency(order.getTaxAmount()));
        System.out.printf("%-41s %-10s\n", "Delivery Fee:", FormatUtil.formatCurrency(order.getDeliveryFee()));
        System.out.println("----------------------------------------------------");
        System.out.printf("%-41s %-10s\n", "GRAND TOTAL:", FormatUtil.formatCurrency(order.getFinalAmount()));
        System.out.println("====================================================");

        // Print Payment History for this order
        if (payments != null && !payments.isEmpty()) {
            System.out.println("Payment Details:");
            for (Payment p : payments) {
                System.out.printf("- [%s] %s : %s\n",
                        p.getStatus(), p.getMode(), FormatUtil.formatCurrency(p.getAmount()));
            }
            System.out.println("====================================================");
        }
    }
}