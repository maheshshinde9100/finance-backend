package com.mahesh.zorvyn.service;

import com.mahesh.zorvyn.dto.DashboardSummary;
import com.mahesh.zorvyn.dto.FinancialRecordRequest;
import com.mahesh.zorvyn.model.FinancialRecord;
import com.mahesh.zorvyn.model.RecordType;
import com.mahesh.zorvyn.repository.FinancialRecordRepository;
import com.mahesh.zorvyn.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {
    
    private final FinancialRecordRepository financialRecordRepository;
    
    private String getCurrentUserId() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return userDetails.getId();
    }
    
    public FinancialRecord createRecord(FinancialRecordRequest request) {
        FinancialRecord record = new FinancialRecord();
        record.setUserId(getCurrentUserId());
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setAmount(request.getAmount());
        record.setDescription(request.getDescription());
        record.setDate(request.getDate());
        record.setCreatedAt(LocalDateTime.now());
        record.setUpdatedAt(LocalDateTime.now());
        
        return financialRecordRepository.save(record);
    }
    
    public List<FinancialRecord> getAllRecords() {
        return financialRecordRepository.findByUserId(getCurrentUserId());
    }
    
    public FinancialRecord getRecordById(String id) {
        return financialRecordRepository.findById(id)
                .filter(record -> record.getUserId().equals(getCurrentUserId()))
                .orElseThrow(() -> new RuntimeException("Record not found"));
    }
    
    public FinancialRecord updateRecord(String id, FinancialRecordRequest request) {
        FinancialRecord record = getRecordById(id);
        record.setType(request.getType());
        record.setCategory(request.getCategory());
        record.setAmount(request.getAmount());
        record.setDescription(request.getDescription());
        record.setDate(request.getDate());
        record.setUpdatedAt(LocalDateTime.now());
        
        return financialRecordRepository.save(record);
    }
    
    public void deleteRecord(String id) {
        FinancialRecord record = getRecordById(id);
        financialRecordRepository.delete(record);
    }
    
    public List<FinancialRecord> filterRecords(RecordType type, String category, LocalDate startDate, LocalDate endDate) {
        String userId = getCurrentUserId();

        boolean hasType = type != null;
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasDateRange = startDate != null && endDate != null;

        if (hasType && hasCategory && hasDateRange) {
            return financialRecordRepository.findByUserIdAndTypeAndDateBetween(userId, type, startDate, endDate)
                    .stream()
                    .filter(record -> record.getCategory() != null && record.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        } else if (hasType && hasDateRange) {
            return financialRecordRepository.findByUserIdAndTypeAndDateBetween(userId, type, startDate, endDate);
        } else if (hasCategory && hasDateRange) {
            return financialRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate)
                    .stream()
                    .filter(record -> record.getCategory() != null && record.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        } else if (hasDateRange) {
            return financialRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else if (hasType && hasCategory) {
            return financialRecordRepository.findByUserIdAndType(userId, type)
                    .stream()
                    .filter(record -> record.getCategory() != null && record.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        } else if (hasType) {
            return financialRecordRepository.findByUserIdAndType(userId, type);
        } else if (hasCategory) {
            return financialRecordRepository.findByUserId(userId)
                    .stream()
                    .filter(record -> record.getCategory() != null && record.getCategory().equalsIgnoreCase(category.trim()))
                    .collect(Collectors.toList());
        } else {
            return financialRecordRepository.findByUserId(userId);
        }
    }
    
    public DashboardSummary getDashboardSummary(LocalDate startDate, LocalDate endDate) {
        String userId = getCurrentUserId();
        List<FinancialRecord> records;
        
        if (startDate != null && endDate != null) {
            records = financialRecordRepository.findByUserIdAndDateBetween(userId, startDate, endDate);
        } else {
            records = financialRecordRepository.findByUserId(userId);
        }
        
        double totalIncome = records.stream()
                .filter(r -> r.getType() == RecordType.INCOME)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();
        
        double totalExpense = records.stream()
                .filter(r -> r.getType() == RecordType.EXPENSE)
                .mapToDouble(FinancialRecord::getAmount)
                .sum();
        
        Map<String, Double> incomeByCategory = records.stream()
                .filter(r -> r.getType() == RecordType.INCOME)
                .collect(Collectors.groupingBy(
                        FinancialRecord::getCategory,
                        Collectors.summingDouble(FinancialRecord::getAmount)
                ));
        
        Map<String, Double> expenseByCategory = records.stream()
                .filter(r -> r.getType() == RecordType.EXPENSE)
                .collect(Collectors.groupingBy(
                        FinancialRecord::getCategory,
                        Collectors.summingDouble(FinancialRecord::getAmount)
                ));
        
        Map<String, Double> monthlyTrend = records.stream()
                .collect(Collectors.groupingBy(
                        record -> record.getDate().format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.summingDouble(record -> 
                                record.getType() == RecordType.INCOME ? record.getAmount() : -record.getAmount())
                ));
        
        DashboardSummary summary = new DashboardSummary();
        summary.setTotalIncome(totalIncome);
        summary.setTotalExpense(totalExpense);
        summary.setBalance(totalIncome - totalExpense);
        summary.setTotalRecords(records.size());
        summary.setIncomeByCategory(incomeByCategory);
        summary.setExpenseByCategory(expenseByCategory);
        summary.setMonthlyTrend(monthlyTrend);
        
        return summary;
    }
}
