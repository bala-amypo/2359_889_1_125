package com.example.demo.service;

import com.example.demo.entity.ProfitCalculationRecord;
import java.util.List;

public interface ProfitCalculationService {
    ProfitCalculationRecord calculateProfit(Long menuItemId);
    ProfitCalculationRecord getCalculationById(Long id);
    List<ProfitCalculationRecord> getCalculationsForMenuItem(Long menuItemId);
    List<ProfitCalculationRecord> getAllCalculations();
    List<ProfitCalculationRecord> findRecordsWithMarginBetween(Double min, Double max);
}