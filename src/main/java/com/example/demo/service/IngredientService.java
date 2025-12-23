package com.example.demo.service;

import com.example.demo.entity.Ingredient;
import java.util.List;

public interface IngredientService {
    Ingredient createIngredient(Ingredient ingredient);
    Ingredient updateIngredient(Long id, Ingredient ingredient);
    Ingredient getIngredientById(Long id);
    List<Ingredient> getAllIngredients();
    void deactivateIngredient(Long id);
}