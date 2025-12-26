package com.example.demo.service.impl;

import com.example.demo.entity.Category;
import com.example.demo.entity.MenuItem;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.RecipeIngredientRepository;
import com.example.demo.service.MenuItemService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.HashSet;
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
        if (menuItemRepository.findByNameIgnoreCase(menuItem.getName()).isPresent()) {
            throw new BadRequestException("Menu item with this name already exists");
        }
        if (menuItem.getSellingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Selling price must be greater than 0");
        }
        
        // Validate categories
        if (menuItem.getCategories() != null && !menuItem.getCategories().isEmpty()) {
            Set<Category> validCategories = new HashSet<>();
            for (Category category : menuItem.getCategories()) {
                Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                if (!existingCategory.getActive()) {
                    throw new BadRequestException("Cannot assign inactive category to menu item");
                }
                validCategories.add(existingCategory);
            }
            menuItem.setCategories(validCategories);
        }
        
        menuItem.setActive(true);
        return menuItemRepository.save(menuItem);
    }
    
    @Override
    public MenuItem updateMenuItem(Long id, MenuItem updated) {
        MenuItem existing = menuItemRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        if (updated.getActive() && !recipeIngredientRepository.existsByMenuItemId(id)) {
            throw new BadRequestException("Cannot activate menu item without recipe ingredients");
        }
        
        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setSellingPrice(updated.getSellingPrice());
        existing.setActive(updated.getActive());
        
        // Update categories
        if (updated.getCategories() != null) {
            Set<Category> validCategories = new HashSet<>();
            for (Category category : updated.getCategories()) {
                Category existingCategory = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                if (!existingCategory.getActive()) {
                    throw new BadRequestException("Cannot assign inactive category to menu item");
                }
                validCategories.add(existingCategory);
            }
            existing.setCategories(validCategories);
        }
        
        return menuItemRepository.save(existing);
    }
    
    @Override
    public MenuItem getMenuItemById(Long id) {
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
}