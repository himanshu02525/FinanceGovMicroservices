package com.finance.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.FinancialProgramRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.enums.ProgramStatus;
import com.finance.service.FinancialProgramService;
import com.finance.service.SubsidyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/programs")
@Slf4j
@RequiredArgsConstructor
public class FinancialProgramController {

    
    private final FinancialProgramService service;
    private final SubsidyService subsidyService; // needed for budgetUsed

    @PostMapping("/save")
    public ResponseEntity<FinancialProgramResponse> createProgram(@RequestBody FinancialProgramRequest request) {
        log.info("Received request to create program: {}", request);
        FinancialProgramResponse response = service.saveProgram(request);
        log.info("Program created successfully with ID: {}", response.getProgramId());
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<FinancialProgramResponse> updateProgram(@PathVariable Long id,
                                                                  @RequestBody FinancialProgramRequest request) {
        log.info("Received request to update program ID: {}", id);
        FinancialProgramResponse response = service.updateProgram(id, request);
        log.info("Program updated successfully with ID: {}", id);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public String deleteProgram(@PathVariable Long id) {
        log.info("Received request to delete program ID: {}", id);
        return service.deleteProgram(id);
    }
    
    @GetMapping("/fetch/{id}")
    public ResponseEntity<FinancialProgramResponse> getProgramById(@PathVariable Long id) {
        log.info("Received request to fetch program ID: {}", id);
        FinancialProgramResponse response = service.getProgramById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchAll")
    public ResponseEntity<List<FinancialProgramResponse>> getAllPrograms() {
        log.info("Received request to fetch all programs");
        List<FinancialProgramResponse> responses = service.getAllPrograms();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fetchByStatus/{status}")
    public ResponseEntity<List<FinancialProgramResponse>> getProgramsByStatus(@PathVariable ProgramStatus status) {
        log.info("Received request to fetch programs by status: {}", status);
        List<FinancialProgramResponse> responses = service.getProgramsByStatus(status);
        return ResponseEntity.ok(responses);
    }
    
 // -------- PROGRAM METRICS --------
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getProgramSummary() {
        return ResponseEntity.ok(service.getProgramSummary());
    }

}
