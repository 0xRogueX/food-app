package com.fooddeliveryapp.service.impl;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.repository.CategoryRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.service.MenuService;
import com.fooddeliveryapp.type.ErrorType;
import com.fooddeliveryapp.util.InputUtil;

import java.util.List;
import java.util.Optional;

public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuServiceImpl(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    // --- Category Management ---
    @Override
    public Category addCategory(String name) {
        if (categoryRepository.existsByName(name)) {
            throw new FoodDeliveryException(ErrorType.RESOURCE_ALREADY_EXISTS, "Category name already exists");
        }
        Category category = new Category(null, name);
        return categoryRepository.save(category);
    }

    @Override
    public void renameCategory(Long categoryId, String newName) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Category not found"));
        category.rename(newName);
        categoryRepository.save(category);
    }

    @Override
    public void toggleCategoryStatus(Long categoryId, boolean activate) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Category not found"));
        if (activate) category.activate();
        else category.deactivate();
        categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public List<Category> getActiveCategories() {
        return categoryRepository.findAllActive();
    }

    // --- MenuItem Management ---
    @Override
    public MenuItem addMenuItem(String name, double price, Long categoryId) {
        InputUtil.requireNonBlank(name, "Name");
        InputUtil.requirePositive(price, "Price");

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND,
                        "Category not found with ID: " + categoryId));
        if (!category.isActive()) {
            throw new FoodDeliveryException(ErrorType.VALIDATION_ERROR,
                    "Category '" + category.getName() + "' is inactive. Activate it first.");
        }
        if (menuItemRepository.existsByName(name)) {
            throw new FoodDeliveryException(ErrorType.RESOURCE_ALREADY_EXISTS,
                    "Menu item with name '" + name + "' already exists.");
        }

        MenuItem item = new MenuItem(null, name, price, categoryId);
        return menuItemRepository.save(item);
    }

    @Override
    public void updateMenuItem(Long itemId, String name, double price, Long categoryId) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Menu item not found"));
        if (!categoryRepository.existsById(categoryId)) {
            throw new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Category does not exist");
        }
        item.rename(name);
        item.updatePrice(price);
        item.changeCategory(categoryId);
        menuItemRepository.save(item);
    }

    @Override
    public void toggleMenuItemAvailability(Long itemId, boolean available) {
        MenuItem item = menuItemRepository.findById(itemId)
                .orElseThrow(() -> new FoodDeliveryException(ErrorType.RESOURCE_NOT_FOUND, "Menu item not found"));
        item.changeAvailability(available);
        menuItemRepository.save(item);
    }

    @Override
    public void removeMenuItem(Long itemId) {
        menuItemRepository.deleteById(itemId);
    }

    @Override
    public Optional<MenuItem> getMenuItemById(Long itemId) {
        return menuItemRepository.findById(itemId);
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public List<MenuItem> getAvailableMenuItemsByCategory(Long categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<MenuItem> searchMenuItemsByName(String keyword) {
        return menuItemRepository.findAllActive().stream()
                .filter(item -> item.getName().toLowerCase().contains(keyword.toLowerCase()))
                .toList();
    }
}