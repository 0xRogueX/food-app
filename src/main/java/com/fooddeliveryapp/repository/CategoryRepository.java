package com.fooddeliveryapp.repository;

import com.fooddeliveryapp.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);
    Optional<Category> findByName(String name);
    List<Category> findAll();
    List<Category> findAllActive();
    List<Category> findAllInactive();

    void deleteById(Long id);

    boolean existsByName(String name);
    boolean existsById(Long categoryId);
}
