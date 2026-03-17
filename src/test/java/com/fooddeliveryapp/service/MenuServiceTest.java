package com.fooddeliveryapp.service;

import com.fooddeliveryapp.exception.FoodDeliveryException;
import com.fooddeliveryapp.model.Category;
import com.fooddeliveryapp.model.MenuItem;
import com.fooddeliveryapp.repository.CategoryRepository;
import com.fooddeliveryapp.repository.MenuItemRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryCategoryRepository;
import com.fooddeliveryapp.repository.inmemory.InMemoryMenuItemRepository;
import com.fooddeliveryapp.service.impl.MenuServiceImpl;
import com.fooddeliveryapp.type.ErrorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class MenuServiceTest {

    private MenuService menuService;

    @BeforeEach
    void setUp() {
        CategoryRepository categoryRepo = new InMemoryCategoryRepository();
        MenuItemRepository menuItemRepo = new InMemoryMenuItemRepository(categoryRepo);
        menuService = new MenuServiceImpl(categoryRepo, menuItemRepo);
    }

    // ── addCategory ───────────────────────────────────────────────────────────

    @Test
    void addCategory_success_returnsActiveCategory() {
        Category c = menuService.addCategory("Pizza");

        assertNotNull(c.getId());
        assertEquals("Pizza", c.getName());
        assertTrue(c.isActive());
    }

    @Test
    void addCategory_duplicateName_throwsResourceAlreadyExists() {
        menuService.addCategory("Pizza");

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                menuService.addCategory("Pizza"));

        assertEquals(ErrorType.RESOURCE_ALREADY_EXISTS, ex.getErrorType());
    }

    // ── renameCategory ────────────────────────────────────────────────────────

    @Test
    void renameCategory_success_updatesName() {
        Category c = menuService.addCategory("Pizza");
        menuService.renameCategory(c.getId(), "Italian");

        assertEquals("Italian", menuService.getAllCategories().get(0).getName());
    }

    @Test
    void renameCategory_notFound_throwsException() {
        assertThrows(FoodDeliveryException.class, () ->
                menuService.renameCategory(999L, "NewName"));
    }

    // ── toggleCategoryStatus ──────────────────────────────────────────────────

    @Test
    void toggleCategoryStatus_deactivate_setsInactive() {
        Category c = menuService.addCategory("Pizza");
        menuService.toggleCategoryStatus(c.getId(), false);

        assertFalse(menuService.getAllCategories().get(0).isActive());
    }

    @Test
    void toggleCategoryStatus_activate_setsActive() {
        Category c = menuService.addCategory("Pizza");
        menuService.toggleCategoryStatus(c.getId(), false);
        menuService.toggleCategoryStatus(c.getId(), true);

        assertTrue(menuService.getAllCategories().get(0).isActive());
    }

    // ── getActiveCategories ───────────────────────────────────────────────────

    @Test
    void getActiveCategories_returnsOnlyActiveCategories() {
        Category c1 = menuService.addCategory("Pizza");
        Category c2 = menuService.addCategory("Burgers");
        menuService.toggleCategoryStatus(c2.getId(), false);

        List<Category> active = menuService.getActiveCategories();

        assertEquals(1, active.size());
        assertEquals("Pizza", active.get(0).getName());
    }

    // ── addMenuItem ───────────────────────────────────────────────────────────

    @Test
    void addMenuItem_success_returnsAvailableItem() {
        Category c = menuService.addCategory("Pizza");
        MenuItem item = menuService.addMenuItem("Margherita", 200.0, c.getId());

        assertNotNull(item.getId());
        assertEquals("Margherita", item.getName());
        assertEquals(200.0, item.getPrice());
        assertTrue(item.isAvailable());
    }

    @Test
    void addMenuItem_categoryNotFound_throwsResourceNotFound() {
        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                menuService.addMenuItem("Margherita", 200.0, 999L));

        assertEquals(ErrorType.RESOURCE_NOT_FOUND, ex.getErrorType());
    }

    @Test
    void addMenuItem_inactiveCategory_throwsValidationError() {
        Category c = menuService.addCategory("Pizza");
        menuService.toggleCategoryStatus(c.getId(), false);

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                menuService.addMenuItem("Margherita", 200.0, c.getId()));

        assertEquals(ErrorType.VALIDATION_ERROR, ex.getErrorType());
    }

    @Test
    void addMenuItem_duplicateName_throwsResourceAlreadyExists() {
        Category c = menuService.addCategory("Pizza");
        menuService.addMenuItem("Margherita", 200.0, c.getId());

        FoodDeliveryException ex = assertThrows(FoodDeliveryException.class, () ->
                menuService.addMenuItem("Margherita", 250.0, c.getId()));

        assertEquals(ErrorType.RESOURCE_ALREADY_EXISTS, ex.getErrorType());
    }

    // ── updateMenuItem ────────────────────────────────────────────────────────

    @Test
    void updateMenuItem_success_updatesFields() {
        Category c1 = menuService.addCategory("Pizza");
        Category c2 = menuService.addCategory("Pasta");
        MenuItem item = menuService.addMenuItem("Margherita", 200.0, c1.getId());

        menuService.updateMenuItem(item.getId(), "Napoletana", 220.0, c2.getId());
        Optional<MenuItem> updated = menuService.getMenuItemById(item.getId());

        assertTrue(updated.isPresent());
        assertEquals("Napoletana", updated.get().getName());
        assertEquals(220.0, updated.get().getPrice());
        assertEquals(c2.getId(), updated.get().getCategoryId());
    }

    // ── toggleMenuItemAvailability ────────────────────────────────────────────

    @Test
    void toggleMenuItemAvailability_makeUnavailable() {
        Category c = menuService.addCategory("Pizza");
        MenuItem item = menuService.addMenuItem("Margherita", 200.0, c.getId());

        menuService.toggleMenuItemAvailability(item.getId(), false);

        assertFalse(menuService.getMenuItemById(item.getId()).get().isAvailable());
    }

    @Test
    void toggleMenuItemAvailability_makeAvailable() {
        Category c = menuService.addCategory("Pizza");
        MenuItem item = menuService.addMenuItem("Margherita", 200.0, c.getId());
        menuService.toggleMenuItemAvailability(item.getId(), false);
        menuService.toggleMenuItemAvailability(item.getId(), true);

        assertTrue(menuService.getMenuItemById(item.getId()).get().isAvailable());
    }

    // ── removeMenuItem ────────────────────────────────────────────────────────

    @Test
    void removeMenuItem_softDeletesMakingItemUnavailable() {
        Category c = menuService.addCategory("Pizza");
        MenuItem item = menuService.addMenuItem("Margherita", 200.0, c.getId());

        menuService.removeMenuItem(item.getId());

        // soft-delete marks item unavailable
        assertFalse(menuService.getMenuItemById(item.getId()).get().isAvailable());
    }

    // ── getAllMenuItems ────────────────────────────────────────────────────────

    @Test
    void getAllMenuItems_returnsAllItems() {
        Category c = menuService.addCategory("Pizza");
        menuService.addMenuItem("Margherita", 200.0, c.getId());
        menuService.addMenuItem("Pepperoni", 250.0, c.getId());

        assertEquals(2, menuService.getAllMenuItems().size());
    }

    // ── getAvailableMenuItemsByCategory ───────────────────────────────────────

    @Test
    void getAvailableMenuItemsByCategory_returnsItemsInCategory() {
        Category c = menuService.addCategory("Pizza");
        menuService.addMenuItem("Margherita", 200.0, c.getId());
        menuService.addMenuItem("Pepperoni", 250.0, c.getId());

        List<MenuItem> items = menuService.getAvailableMenuItemsByCategory(c.getId());

        assertEquals(2, items.size());
    }

    // ── searchMenuItemsByName ─────────────────────────────────────────────────

    @Test
    void searchMenuItemsByName_findsMatchingItems() {
        Category c = menuService.addCategory("Food");
        menuService.addMenuItem("Margherita Pizza", 200.0, c.getId());
        menuService.addMenuItem("Chicken Burger", 150.0, c.getId());

        List<MenuItem> results = menuService.searchMenuItemsByName("pizza");

        assertEquals(1, results.size());
        assertEquals("Margherita Pizza", results.get(0).getName());
    }

    @Test
    void searchMenuItemsByName_caseInsensitive() {
        Category c = menuService.addCategory("Food");
        menuService.addMenuItem("Margherita Pizza", 200.0, c.getId());

        assertEquals(1, menuService.searchMenuItemsByName("PIZZA").size());
    }

    @Test
    void searchMenuItemsByName_noMatch_returnsEmptyList() {
        Category c = menuService.addCategory("Pizza");
        menuService.addMenuItem("Margherita", 200.0, c.getId());

        assertTrue(menuService.searchMenuItemsByName("Sushi").isEmpty());
    }
}
