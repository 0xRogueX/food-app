package com.fooddeliveryapp.service;

import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.MenuItem;

import java.util.List;
import java.util.Optional;

public interface MenuService {

    // --- Category Management ---
    Category addCategory(String name);
    void renameCategory(Long categoryId, String newName);
    void toggleCategoryStatus(Long categoryId, boolean activate);
    List<Category> getAllCategories();
    List<Category> getActiveCategories();

    // --- MenuItem Management ---
    MenuItem addMenuItem(String name, double price, Long categoryId);
    void updateMenuItem(Long itemId, String name, double price, Long categoryId);
    void toggleMenuItemAvailability(Long itemId, boolean available);
    void removeMenuItem(Long itemId); // Soft delete

    Optional<MenuItem> getMenuItemById(Long itemId);
    List<MenuItem> getAllMenuItems();
    List<MenuItem> getAvailableMenuItemsByCategory(Long categoryId);
    List<MenuItem> searchMenuItemsByName(String keyword);
}