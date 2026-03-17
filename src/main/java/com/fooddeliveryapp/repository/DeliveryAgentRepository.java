package com.fooddeliveryapp.repository;

import com.fooddeliveryapp.model.DeliveryAgent;

import java.util.List;
import java.util.Optional;

public interface DeliveryAgentRepository {

    Optional<DeliveryAgent> findById(Long id);

    Optional<DeliveryAgent> findByEmail(String email);

    List<DeliveryAgent> findAll();

    List<DeliveryAgent> findAvailableAgents();
}