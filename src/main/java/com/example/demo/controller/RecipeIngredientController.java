package com.example.demo.controller;

import com.example.demo.entity.RecipeIngredient;
import com.example.demo.service.RecipeIngredientService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/recipe-ingredients")
public class RecipeIngredientController {
    
    private final RecipeIngredientService recipeIngredientService;
    
    public RecipeIngredientController(RecipeIngredientService recipeIngredientService) {
        this.recipeIngredientService = recipeIngredientService;
    }
    
    @PostMapping
    public ResponseEntity<RecipeIngredient> addIngredientToMenuItem(@RequestBody RecipeIngredient recipeIngredient) {
        RecipeIngredient created = recipeIngredientService.addIngredientToMenuItem(recipeIngredient);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<RecipeIngredient> updateRecipeIngredient(@PathVariable Long id, @RequestParam Double quantity) {
        RecipeIngredient updated = recipeIngredientService.updateRecipeIngredient(id, quantity);
        return ResponseEntity.ok(updated);
    }
    
    @GetMapping("/menu-item/{menuItemId}")
    public ResponseEntity<List<RecipeIngredient>> getIngredientsByMenuItem(@PathVariable Long menuItemId) {
        List<RecipeIngredient> ingredients = recipeIngredientService.getIngredientsByMenuItem(menuItemId);
        return ResponseEntity.ok(ingredients);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeIngredientFromRecipe(@PathVariable Long id) {
        recipeIngredientService.removeIngredientFromRecipe(id);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/ingredient/{ingredientId}/total-quantity")
    public ResponseEntity<Double> getTotalQuantityOfIngredient(@PathVariable Long ingredientId) {
        Double totalQuantity = recipeIngredientService.getTotalQuantityOfIngredient(ingredientId);
        return ResponseEntity.ok(totalQuantity);
    }
}