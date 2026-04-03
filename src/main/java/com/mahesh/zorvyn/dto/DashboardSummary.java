package com.mahesh.zorvyn.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
    
    private Double totalIncome;
    private Double totalExpense;
    private Double balance;
    private Integer totalRecords;
    private Map<String, Double> incomeByCategory;
    private Map<String, Double> expenseByCategory;
    private Map<String, Double> monthlyTrend;
}
