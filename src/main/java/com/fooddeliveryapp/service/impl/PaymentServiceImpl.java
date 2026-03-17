package com.fooddeliveryapp.service.impl;

import com.fooddeliveryapp.model.Order;
import com.fooddeliveryapp.model.Payment;
import com.fooddeliveryapp.repository.PaymentRepository;
import com.fooddeliveryapp.service.PaymentService;
import com.fooddeliveryapp.strategy.PaymentStrategy;
import com.fooddeliveryapp.type.PaymentStatus;

import java.util.List;
import java.util.Optional;

public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentServiceImpl(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    @Override
    public Payment processPayment(Order order, PaymentStrategy strategy) {
        boolean success = strategy.pay(order.getFinalAmount());

        Payment payment = new Payment(
                null, // ID will be assigned by persistence layer (in-memory sequence or DB)
                order.getId(),
                order.getFinalAmount(),
                strategy.getPaymentType()
        );

        if (success) {
            payment.markSuccess();
        } else {
            payment.markFailed();
        }

        return paymentRepository.save(payment);
    }

    @Override
    public Optional<Payment> getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId);
    }

    @Override
    public List<Payment> getPaymentsByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Override
    public double calculateTotalRevenue() {
        return paymentRepository.findAll().stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .mapToDouble(Payment::getAmount)
                .sum();
    }
}