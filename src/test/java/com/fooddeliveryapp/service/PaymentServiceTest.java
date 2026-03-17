package com.fooddeliveryapp.service;

import com.fooddeliveryapp.JdbcTestBase;
import com.fooddeliveryapp.config.SystemConfig;
import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.model.OrderItem;
import com.fooddeliveryapp.model.Payment;
import com.fooddeliveryapp.repository.PaymentRepository;
import com.fooddeliveryapp.repository.jdbc.JdbcPaymentRepository;
import com.fooddeliveryapp.service.impl.PaymentServiceImpl;
import com.fooddeliveryapp.strategy.CashPayment;
import com.fooddeliveryapp.strategy.UPIPayment;
import com.fooddeliveryapp.type.PaymentMode;
import com.fooddeliveryapp.type.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PaymentServiceTest extends JdbcTestBase {

    private PaymentService paymentService;
    private Order sampleOrder;

    @BeforeEach
    void setUp() {
        // Ensure predictable SystemConfig state (used by Order constructor)
        SystemConfig config = SystemConfig.getInstance();
        config.setTaxRate(5.0);
        config.setDeliveryFee(40.0);

        PaymentRepository paymentRepository = new JdbcPaymentRepository(connectionManager);
        paymentService = new PaymentServiceImpl(paymentRepository);

        // subTotal=200, discount=0, deliveryFee=40 → tax=10, finalAmount=250
        List<OrderItem> items = List.of(new OrderItem(1L, "Margherita", 200.0, 1));
        sampleOrder = new Order(1L, 1L, items, "123 Main St", 200.0, 0, 40.0);
    }

    // ── processPayment ────────────────────────────────────────────────────────

    @Test
    void processPayment_cashPayment_isCompleted() {
        Payment payment = paymentService.processPayment(sampleOrder, new CashPayment());

        assertNotNull(payment.getPaymentId());
        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(PaymentMode.CASH, payment.getMode());
        assertNotNull(payment.getPaidAt());
    }

    @Test
    void processPayment_upiPayment_isCompleted() {
        Payment payment = paymentService.processPayment(sampleOrder, new UPIPayment("user@upi"));

        assertEquals(PaymentStatus.COMPLETED, payment.getStatus());
        assertEquals(PaymentMode.UPI, payment.getMode());
    }

    @Test
    void processPayment_amountMatchesOrderFinalAmount() {
        Payment payment = paymentService.processPayment(sampleOrder, new CashPayment());

        assertEquals(sampleOrder.getFinalAmount(), payment.getAmount(), 0.001);
    }

    @Test
    void processPayment_isLinkedToCorrectOrder() {
        Payment payment = paymentService.processPayment(sampleOrder, new CashPayment());

        assertEquals(sampleOrder.getId(), payment.getOrderId());
    }

    // ── getPaymentById ────────────────────────────────────────────────────────

    @Test
    void getPaymentById_found_returnsPayment() {
        Payment payment = paymentService.processPayment(sampleOrder, new CashPayment());

        Optional<Payment> found = paymentService.getPaymentById(payment.getPaymentId());

        assertTrue(found.isPresent());
        assertEquals(payment.getPaymentId(), found.get().getPaymentId());
    }

    @Test
    void getPaymentById_notFound_returnsEmpty() {
        assertTrue(paymentService.getPaymentById(999L).isEmpty());
    }

    // ── getPaymentsByOrderId ──────────────────────────────────────────────────

    @Test
    void getPaymentsByOrderId_returnsPaymentsForOrder() {
        Payment payment = paymentService.processPayment(sampleOrder, new CashPayment());

        List<Payment> payments = paymentService.getPaymentsByOrderId(sampleOrder.getId());

        assertEquals(1, payments.size());
        assertEquals(payment.getPaymentId(), payments.get(0).getPaymentId());
    }

    @Test
    void getPaymentsByOrderId_noPayments_returnsEmptyList() {
        assertTrue(paymentService.getPaymentsByOrderId(999L).isEmpty());
    }

    // ── getAllPayments ────────────────────────────────────────────────────────

    @Test
    void getAllPayments_returnsAllProcessedPayments() {
        paymentService.processPayment(sampleOrder, new CashPayment());

        List<OrderItem> items2 = List.of(new OrderItem(2L, "Burger", 150.0, 2));
        Order order2 = new Order(2L, 2L, items2, "456 Side St", 300.0, 0, 40.0);
        paymentService.processPayment(order2, new CashPayment());

        assertEquals(2, paymentService.getAllPayments().size());
    }

    // ── calculateTotalRevenue ─────────────────────────────────────────────────

    @Test
    void calculateTotalRevenue_sumsOnlyCompletedPayments() {
        paymentService.processPayment(sampleOrder, new CashPayment());

        List<OrderItem> items2 = List.of(new OrderItem(2L, "Burger", 150.0, 1));
        Order order2 = new Order(2L, 2L, items2, "456 Side St", 150.0, 0, 40.0);
        paymentService.processPayment(order2, new CashPayment());

        double revenue = paymentService.calculateTotalRevenue();

        // Both payments are COMPLETED, revenue = finalAmount1 + finalAmount2
        assertEquals(sampleOrder.getFinalAmount() + order2.getFinalAmount(), revenue, 0.001);
    }

    @Test
    void calculateTotalRevenue_noPayments_returnsZero() {
        assertEquals(0.0, paymentService.calculateTotalRevenue());
    }
}
