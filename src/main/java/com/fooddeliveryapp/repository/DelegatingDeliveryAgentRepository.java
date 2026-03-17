package com.fooddeliveryapp.repository;

import com.fooddeliveryapp.model.DeliveryAgent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DelegatingDeliveryAgentRepository implements DeliveryAgentRepository {

    private final UserRepository userRepository;

    public DelegatingDeliveryAgentRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Optional<DeliveryAgent> findById(Long id) {
        return userRepository.findById(id)
                .filter(user -> user instanceof DeliveryAgent)
                .map(user -> (DeliveryAgent) user);
    }

    @Override
    public Optional<DeliveryAgent> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .filter(user -> user instanceof DeliveryAgent)
                .map(user -> (DeliveryAgent) user);
    }

    @Override
    public List<DeliveryAgent> findAll() {
        return userRepository.findAll().stream()
                .filter(user -> user instanceof DeliveryAgent)
                .map(user -> (DeliveryAgent) user)
                .toList();
    }

    @Override
    public List<DeliveryAgent> findAvailableAgents() {
        return findAll().stream()
                .filter(DeliveryAgent::isAvailable)
                .sorted(Comparator
                        .comparing(DeliveryAgent::getLastAssignedTime,
                                Comparator.nullsFirst(Comparator.naturalOrder()))
                        .thenComparing(DeliveryAgent::getId))
                .toList();
    }
}
