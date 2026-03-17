package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.model.Payment;
import com.fooddeliveryapp.repository.PaymentRepository;

import java.util.*;

public class InMemoryPaymentRepository implements PaymentRepository {

    private final Map<Long, Payment> paymentStore = new HashMap<>();  // paymentId, payment
    private final Map<Long, List<Long>> orderIndex = new HashMap<>(); // orderId -> list of paymentIds
    private long sequence = 1L;

    @Override
    public Payment save(Payment payment) {
        if (payment == null) {
            throw new IllegalArgumentException("Payment cannot be null");
        }

        if (payment.getPaymentId() == null) {
            payment.setId(sequence++);
        }

        paymentStore.put(payment.getPaymentId(), payment);

        // Update orderIndex
        if (payment.getOrderId() != null) {
            orderIndex.computeIfAbsent(payment.getOrderId(), key -> new ArrayList<>()) // here key -> orderId
                    .add(payment.getPaymentId());
        }

        return payment;
    }

    @Override
    public Optional<Payment> findById(Long paymentId) {
        if (paymentId == null) return Optional.empty();
        return Optional.ofNullable(paymentStore.get(paymentId));
    }

    @Override
    public List<Payment> findAll() {
        return new ArrayList<>(paymentStore.values());
    }

    @Override
    public List<Payment> findByOrderId(Long orderId) {
        if (orderId == null) return List.of();
        List<Long> paymentIds = orderIndex.get(orderId);
        if (paymentIds == null || paymentIds.isEmpty()) return List.of();

        return paymentIds.stream()
                .map(paymentStore::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void update(Payment payment) {
        if (payment == null || payment.getPaymentId() == null) {
            throw new IllegalArgumentException("Invalid payment for update");
        }

        if (!paymentStore.containsKey(payment.getPaymentId())) {
            throw new IllegalArgumentException("Payment does not exist. Cannot update.");
        }

        // Remove old orderIndex entry if orderId changed
        Payment old = paymentStore.get(payment.getPaymentId());
        Long oldOrderId = old.getOrderId();
        if (oldOrderId != null && !oldOrderId.equals(payment.getOrderId())) {
            List<Long> payments = orderIndex.get(oldOrderId);
            if (payments != null) {
                payments.remove(payment.getPaymentId());
                if (payments.isEmpty()) orderIndex.remove(oldOrderId);
            }
        }

        // Save updated payment
        paymentStore.put(payment.getPaymentId(), payment);

        // Add to new orderIndex if necessary (only if orderId changed to avoid duplicates)
        if (payment.getOrderId() != null && !payment.getOrderId().equals(oldOrderId)) {
            orderIndex.computeIfAbsent(payment.getOrderId(), k -> new ArrayList<>())
                    .add(payment.getPaymentId());
        }
    }

    @Override
    public boolean existsById(Long paymentId) {
        if (paymentId == null) return false;
        return paymentStore.containsKey(paymentId);
    }
}