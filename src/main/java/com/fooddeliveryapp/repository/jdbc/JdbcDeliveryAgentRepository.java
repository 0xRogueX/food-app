package com.fooddeliveryapp.repository.jdbc;

import com.fooddeliveryapp.repository.DelegatingDeliveryAgentRepository;
import com.fooddeliveryapp.repository.UserRepository;

public class JdbcDeliveryAgentRepository extends DelegatingDeliveryAgentRepository {

    public JdbcDeliveryAgentRepository(UserRepository userRepository) {
        super(userRepository);
    }
}

