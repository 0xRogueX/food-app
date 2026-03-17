package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.model.User;
import com.fooddeliveryapp.repository.UserRepository;

import java.util.*;

public class InMemoryUserRepository implements UserRepository {

    private final Map<Long, User> usersById = new HashMap<>();    // userId, user
    private final Map<String, User> usersByEmail = new HashMap<>(); // userEmail, user
    private long sequence = 1L;

    @Override
    public User save(User user) {
        Objects.requireNonNull(user, "User can't be null");

        if (user.getId() == null) {
            user.setId(sequence++);
        }

        usersById.put(user.getId(), user);
        usersByEmail.put(user.getEmail(), user);

        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        if(id==null) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if(email==null || email.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable(usersByEmail.get(email));
    }

    @Override
    public List<User> findAll() {
        return new ArrayList<>(usersById.values());
    }

    @Override
    public void deleteById(Long id) {
        User removedUser = usersById.remove(id);
        if(removedUser!=null) {
            usersByEmail.remove(removedUser.getEmail());
        }
    }

    @Override
    public boolean existsById(Long id) {
        return usersById.containsKey(id);
    }
}