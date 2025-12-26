package com.example.demo.service.impl;

import com.example.demo.entity.MenuItem;
import com.example.demo.entity.ProfitCalculationRecord;
import com.example.demo.entity.RecipeIngredient;
import com.example.demo.exception.BadRequestException;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.repository.IngredientRepository;
import com.example.demo.repository.MenuItemRepository;
import com.example.demo.repository.ProfitCalculationRecordRepository;
import com.example.demo.repository.RecipeIngredientRepository;
import com.example.demo.service.ProfitCalculationService;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.util.List;

@Service
public class ProfitCalculationServiceImpl implements ProfitCalculationService {
    
    private final MenuItemRepository menuItemRepository;
    private final RecipeIngredientRepository recipeIngredientRepository;
    private final IngredientRepository ingredientRepository;
    private final ProfitCalculationRecordRepository profitCalculationRecordRepository;
    
    public ProfitCalculationServiceImpl(MenuItemRepository menuItemRepository,
                                      RecipeIngredientRepository recipeIngredientRepository,
                                      IngredientRepository ingredientRepository,
                                      ProfitCalculationRecordRepository profitCalculationRecordRepository) {
        this.menuItemRepository = menuItemRepository;
        this.recipeIngredientRepository = recipeIngredientRepository;
        this.ingredientRepository = ingredientRepository;
        this.profitCalculationRecordRepository = profitCalculationRecordRepository;
    }
    
    @Override
    public ProfitCalculationRecord calculateProfit(Long menuItemId) {
        MenuItem menuItem = menuItemRepository.findById(menuItemId)
            .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));
        
        List<RecipeIngredient> recipeIngredients = recipeIngredientRepository.findByMenuItemId(menuItemId);
        if (recipeIngredients.isEmpty()) {
            throw new BadRequestException("Cannot calculate profit for menu item without ingredients");
        }
        
        BigDecimal totalCost = BigDecimal.ZERO;
        for (RecipeIngredient ri : recipeIngredients) {
            BigDecimal ingredientCost = ri.getIngredient().getCostPerUnit()
                .multiply(BigDecimal.valueOf(ri.getQuantity()));
            totalCost = totalCost.add(ingredientCost);
        }
        
        BigDecimal profit = menuItem.getSellingPrice().subtract(totalCost);
        Double profitMargin = profit.divide(menuItem.getSellingPrice(), 4, BigDecimal.ROUND_HALF_UP)
            .multiply(BigDecimal.valueOf(100)).doubleValue();
        
        ProfitCalculationRecord record = new ProfitCalculationRecord();
        record.setMenuItem(menuItem);
        record.setTotalCost(totalCost);
        record.setProfitMargin(profitMargin);
        
        return profitCalculationRecordRepository.save(record);
    }
    
    @Override
    public ProfitCalculationRecord getCalculationById(Long id) {
        return profitCalculationRecordRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Profit calculation record not found"));
    }
    
    @Override
    public List<ProfitCalculationRecord> getCalculationsForMenuItem(Long menuItemId) {
        return profitCalculationRecordRepository.findByMenuItemId(menuItemId);
    }
    
    @Override
    public List<ProfitCalculationRecord> getAllCalculations() {
        return profitCalculationRecordRepository.findAll();
    }
    
    @Override
    public List<ProfitCalculationRecord> findRecordsWithMarginBetween(Double min, Double max) {
        // This method is used in tests with spy - implementation can be simple
        return profitCalculationRecordRepository.findAll().stream()
            .filter(record -> record.getProfitMargin() >= min && record.getProfitMargin() <= max)
            .toList();
    }
}