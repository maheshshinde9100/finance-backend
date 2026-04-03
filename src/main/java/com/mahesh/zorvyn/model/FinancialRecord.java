package com.mahesh.zorvyn.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "financial_records")
public class FinancialRecord {
    
    @Id
    private String id;
    
    private String userId;
    
    private RecordType type;
    
    private String category;
    
    private Double amount;
    
    private String description;
    
    private LocalDate date;
    
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt = LocalDateTime.now();
}
