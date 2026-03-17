package com.fooddeliveryapp.repository.inmemory;

import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.repository.CategoryRepository;

import java.util.*;

public class InMemoryCategoryRepository implements CategoryRepository {

    private final Map<Long, Category> store = new HashMap<>();    // categoryId, category
    private final Map<String, Long> nameIndex = new HashMap<>();  // categoryName, categoryId
    private long sequence = 1L;

    @Override
    public Category save(Category category) {
        if(category == null)
            throw new IllegalArgumentException("category cannot be null");

        if (category.getId() == null) {
            category.setId(sequence++);
        }

        store.put(category.getId(), category);

        if(category.getName() != null) {
            nameIndex.put(category.getName().toLowerCase(), category.getId());
        }
        return category;
    }

    @Override
    public Optional<Category> findById(Long id) {
        if(id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Optional<Category> findByName(String name) {
        if(name == null || name.isEmpty()) {
            return Optional.empty();
        }
        Long id  = nameIndex.get(name.toLowerCase());
        if(id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Category> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Category> findAllActive() {
        return store.values().stream()
                .filter(Category::isActive)
                .toList();
    }

    @Override
    public List<Category> findAllInactive() {
        return store.values().stream()
                .filter(c -> !c.isActive())
                .toList();
    }

    @Override
    public void deleteById(Long id) {
        Category category = store.get(id);
        if(category != null) {
            category.deactivate();
            nameIndex.remove(category.getName().toLowerCase());
        }
    }

    @Override
    public boolean existsByName(String name) {
        if(name == null || name.isEmpty()) {
            return false;
        }
        return nameIndex.containsKey(name.toLowerCase());
    }

    @Override
    public boolean existsById(Long categoryId) {
        return store.get(categoryId) != null;
    }
}
