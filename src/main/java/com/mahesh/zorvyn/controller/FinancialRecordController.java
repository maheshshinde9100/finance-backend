package com.mahesh.zorvyn.controller;

import com.mahesh.zorvyn.dto.DashboardSummary;
import com.mahesh.zorvyn.dto.FinancialRecordRequest;
import com.mahesh.zorvyn.model.FinancialRecord;
import com.mahesh.zorvyn.model.RecordType;
import com.mahesh.zorvyn.service.FinancialRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class FinancialRecordController {
    
    private final FinancialRecordService financialRecordService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialRecord> createRecord(@Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecord record = financialRecordService.createRecord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(record);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<FinancialRecord>> getAllRecords() {
        List<FinancialRecord> records = financialRecordService.getAllRecords();
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialRecord> getRecordById(@PathVariable String id) {
        FinancialRecord record = financialRecordService.getRecordById(id);
        return ResponseEntity.ok(record);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FinancialRecord> updateRecord(
            @PathVariable String id,
            @Valid @RequestBody FinancialRecordRequest request) {
        FinancialRecord record = financialRecordService.updateRecord(id, request);
        return ResponseEntity.ok(record);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteRecord(@PathVariable String id) {
        financialRecordService.deleteRecord(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<FinancialRecord>> filterRecords(
            @RequestParam(required = false) RecordType type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<FinancialRecord> records = financialRecordService.filterRecords(type, category, startDate, endDate);
        return ResponseEntity.ok(records);
    }
    
    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<DashboardSummary> getDashboardSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        DashboardSummary summary = financialRecordService.getDashboardSummary(startDate, endDate);
        return ResponseEntity.ok(summary);
    }
}
