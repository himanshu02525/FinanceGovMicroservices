package com.finance.controller;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.finance.dto.SubsidyRequest;
import com.finance.dto.SubsidyResponse;
import com.finance.service.SubsidyService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/subsidies")
@RequiredArgsConstructor
public class SubsidyController {

    
    private final SubsidyService service;

    @PostMapping("/save")
    public ResponseEntity<SubsidyResponse> createSubsidy(@RequestBody SubsidyRequest request) {
        SubsidyResponse response = service.saveSubsidy(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/fetchAll")
    public ResponseEntity<List<SubsidyResponse>> getAllSubsidies() {
        List<SubsidyResponse> responses = service.getAllSubsidies();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fetchByProgram/{programId}")
    public ResponseEntity<List<SubsidyResponse>> getSubsidiesByProgram(@PathVariable Long programId) {
        List<SubsidyResponse> responses = service.getSubsidiesByProgram(programId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/fetchByEntity/{entityId}")
    public ResponseEntity<List<SubsidyResponse>> getSubsidiesByEntity(@PathVariable Long entityId) {
        List<SubsidyResponse> responses = service.getSubsidiesByEntity(entityId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/fetch/{id}")
    public ResponseEntity<SubsidyResponse> getSubsidyById(@PathVariable Long id) {
        SubsidyResponse response = service.getSubsidyById(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/approved_sum/{programId}")
    public ResponseEntity<BigDecimal> getApprovedAmount(@PathVariable Long programId) {
        return ResponseEntity.ok(service.getApprovedAmountByProgram(programId));
    }
}
