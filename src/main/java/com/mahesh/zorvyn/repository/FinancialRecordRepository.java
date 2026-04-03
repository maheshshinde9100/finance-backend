package com.mahesh.zorvyn.repository;

import com.mahesh.zorvyn.model.FinancialRecord;
import com.mahesh.zorvyn.model.RecordType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface FinancialRecordRepository extends MongoRepository<FinancialRecord, String> {
    List<FinancialRecord> findByUserId(String userId);
    List<FinancialRecord> findByUserIdAndType(String userId, RecordType type);
    List<FinancialRecord> findByUserIdAndCategory(String userId, String category);
    
    List<FinancialRecord> findByUserIdAndDateBetween(String userId, LocalDate startDate, LocalDate endDate);
    List<FinancialRecord> findByUserIdAndTypeAndDateBetween(String userId, RecordType type, LocalDate startDate, LocalDate endDate);
    List<FinancialRecord> findByUserIdAndCategoryAndDateBetween(String userId, String category, LocalDate startDate, LocalDate endDate);
}
