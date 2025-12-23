package com.example.demo.service;

import com.example.demo.entity.RecipeIngredient;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.RecipeIngredientRepository;
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
        if (recipeIngredient.getQuantityRequired() == null || recipeIngredient.getQuantityRequired() <= 0) {
            throw new BadRequestException("Quantity required must be greater than 0");
        }
        
        if (recipeIngredient.getIngredient() != null && recipeIngredient.getIngredient().getId() != null) {
            Long ingredientId = recipeIngredient.getIngredient().getId();
            ingredientRepository.findById(ingredientId)
                    .orElseThrow(() -> new ResourceNotFoundException("Ingredient not found"));
        }
        
        if (recipeIngredient.getMenuItem() != null && recipeIngredient.getMenuItem().getId() != null) {
            Long menuItemId = recipeIngredient.getMenuItem().getId();
            menuItemRepository.findById(menuItemId)
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        }
        
        return recipeIngredientRepository.save(recipeIngredient);
    }

    @Override
    public RecipeIngredient updateRecipeIngredient(Long id, Double quantity) {
        RecipeIngredient existing = recipeIngredientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe ingredient not found"));
        
        if (quantity <= 0) {
            throw new BadRequestException("Quantity required must be greater than 0");
        }
        
        existing.setQuantityRequired(quantity);
        return recipeIngredientRepository.save(existing);
    }

    @Override
    public List<RecipeIngredient> getIngredientsByMenuItem(Long menuItemId) {
        return recipeIngredientRepository.findByMenuItemId(menuItemId);
    }

    @Override
    public void removeIngredientFromRecipe(Long id) {
        if (!recipeIngredientRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipe ingredient not found");
        }
        recipeIngredientRepository.deleteById(id);
    }

    @Override
    public Double getTotalQuantityOfIngredient(Long ingredientId) {
        return recipeIngredientRepository.getTotalQuantityByIngredientId(ingredientId);
    }
}