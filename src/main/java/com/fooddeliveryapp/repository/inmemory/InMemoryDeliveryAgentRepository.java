package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.repository.DelegatingDeliveryAgentRepository;
import com.fooddeliveryapp.repository.UserRepository;

public class InMemoryDeliveryAgentRepository extends DelegatingDeliveryAgentRepository {

    public InMemoryDeliveryAgentRepository(UserRepository userRepository) {
        super(userRepository);
    }
}