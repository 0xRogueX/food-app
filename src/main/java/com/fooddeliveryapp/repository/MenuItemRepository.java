package com.fooddeliveryapp.repository;

import com.fooddeliveryapp.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuItemRepository {
    MenuItem save(MenuItem item);
    Optional<MenuItem> findById(Long id);
    Optional<MenuItem> findByName(String name);
    List<MenuItem> findAll();
    List<MenuItem> findAllActive();
    List<MenuItem> findAllInactive();
    List<MenuItem> findByCategoryId(Long categoryId);
    void deleteById(Long id);
    boolean existsById(Long id);
    boolean existsByName(String name);
}
