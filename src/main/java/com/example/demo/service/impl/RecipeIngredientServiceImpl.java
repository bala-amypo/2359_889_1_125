package com.example.demo.service.impl;

import com.example.demo.entity.Ingredient;
import com.example.demo.entity.MenuItem;
import com.example.demo.entity.RecipeIngredient;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.RecipeIngredientRepository;
import com.example.demo.service.RecipeIngredientService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecipeIngredientServiceImpl implements RecipeIngredientService {
    
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final MenuItemRepository menuItemRepository;
    
    public RecipeIngredientServiceImpl(RecipeIngredientRepository recipeIngredientRepository,
                                     IngredientRepository ingredientRepository,
                                     MenuItemRepository menuItemRepository) {
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.menuItemRepository = menuItemRepository;
    }
    
    @Override
    public RecipeIngredient addIngredientToMenuItem(RecipeIngredient recipeIngredient) {
        if (recipeIngredient.getQuantity() <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        
        Ingredient ingredient = ingredientRepository.findById(recipeIngredient.getIngredient().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        
        MenuItem menuItem = menuItemRepository.findById(recipeIngredient.getMenuItem().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        recipeIngredient.setIngredient(ingredient);
        recipeIngredient.setMenuItem(menuItem);
        
        return recipeIngredientRepository.save(recipeIngredient);
    }
    
    @Override
    public RecipeIngredient updateRecipeIngredient(Long id, Double quantity) {
        RecipeIngredient existing = recipeIngredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe ingredient not found"));
        
        if (quantity <= 0) {
            throw new BadRequestException("Quantity must be greater than 0");
        }
        
        existing.setQuantity(quantity);
        return recipeIngredientRepository.save(existing);
    }
    
    @Override
    public List<RecipeIngredient> getIngredientsByMenuItem(Long menuItemId) {
        return recipeIngredientRepository.findByMenuItemId(menuItemId);
    }
    
    @Override
    public void removeIngredientFromRecipe(Long id) {
        RecipeIngredient recipeIngredient = recipeIngredientRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Recipe ingredient not found"));
        recipeIngredientRepository.delete(recipeIngredient);
    }
    
    @Override
    public Double getTotalQuantityOfIngredient(Long ingredientId) {
        return recipeIngredientRepository.getTotalQuantityByIngredientId(ingredientId);
    }
}