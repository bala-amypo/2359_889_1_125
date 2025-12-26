package com.example.demo.repository;

import com.example.demo.entity.ProfitCalculationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfitCalculationRecordRepository extends JpaRepository<ProfitCalculationRecord, Long> {
    List<ProfitCalculationRecord> findByMenuItemId(Long menuItemId);
    List<ProfitCalculationRecord> findByProfitMarginGreaterThanEqual(Double margin);
}