package com.example.demo.repository;

import com.example.demo.entity.RecipeIngredient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecipeIngredientRepository extends JpaRepository<RecipeIngredient, Long> {
    List<RecipeIngredient> findByMenuItemId(Long menuItemId);
    boolean existsByMenuItemId(Long menuItemId);
    
    @Query("SELECT COALESCE(SUM(ri.quantityRequired), 0.0) FROM RecipeIngredient ri WHERE ri.ingredient.id = :ingredientId")
    Double getTotalQuantityByIngredientId(@Param("ingredientId") Long ingredientId);
}