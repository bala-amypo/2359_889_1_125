package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.RecipeIngredientRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Service
public class MenuItemServiceImpl implements MenuItemService {

    private final MenuItemRepository menuItemRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final CategoryRepository categoryRepository;

    public MenuItemServiceImpl(MenuItemRepository menuItemRepository, 
                              RecipeIngredientRepository recipeIngredientRepository,
                              CategoryRepository categoryRepository) {
        this.menuItemRepository = menuItemRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.categoryRepository = categoryRepository;
    }

    @Override
    public MenuItem createMenuItem(MenuItem menuItem) {
        if (menuItem.getSellingPrice() == null || menuItem.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Selling price must be greater than 0");
        }
        if (menuItemRepository.findByNameIgnoreCase(menuItem.getName()).isPresent()) {
            throw new BadRequestException("Menu item name already exists");
        }
        
        validateCategories(menuItem.getCategories());
        return menuItemRepository.save(menuItem);
    }

    @Override
    public MenuItem updateMenuItem(Long id, MenuItem menuItem) {
        MenuItem existing = menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        if (menuItem.getActive() != null && menuItem.getActive() && 
            !recipeIngredientRepository.existsByMenuItemId(existing.getId())) {
            throw new BadRequestException("Cannot activate menu item without recipe ingredients");
        }
        
        existing.setName(menuItem.getName());
        existing.setDescription(menuItem.getDescription());
        existing.setSellingPrice(menuItem.getSellingPrice());
        existing.setActive(menuItem.getActive());
        
        if (menuItem.getCategories() != null) {
            validateCategories(menuItem.getCategories());
            existing.setCategories(menuItem.getCategories());
        }
        
        return menuItemRepository.save(existing);
    }

    @Override
    public MenuItem getMenuItemById(Long id) {
        if (id == null) {
            throw new BadRequestException("Menu item ID cannot be null");
        }
        return menuItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
    }

    @Override
    public List<MenuItem> getAllMenuItems() {
        return menuItemRepository.findAll();
    }

    @Override
    public void deactivateMenuItem(Long id) {
        MenuItem menuItem = getMenuItemById(id);
        menuItem.setActive(false);
        menuItemRepository.save(menuItem);
    }

    private void validateCategories(Set<Category> categories) {
        if (categories != null) {
            for (Category category : categories) {
                Long categoryId = category.getId();
                if (categoryId != null) {
                    Category existing = categoryRepository.findById(categoryId)
                            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                    if (!existing.getActive()) {
                        throw new BadRequestException("Cannot assign inactive category to menu item");
                    }
                }
            }
        }
    }
}