package com.finance.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.dto.SubsidyApplicationRequest;
import com.finance.dto.SubsidyApplicationResponse;
import com.finance.service.SubsidyApplicationService;

@RestController
@RequestMapping("/applications")
public class SubsidyApplicationController {

    private final SubsidyApplicationService service;

    public SubsidyApplicationController(SubsidyApplicationService service) {
        this.service = service;
    }

    // Citizen submits application
    @PostMapping("/save")
    public ResponseEntity<SubsidyApplicationResponse> createApplication(
            @RequestBody SubsidyApplicationRequest request) {

        SubsidyApplicationResponse response = service.saveApplication(request);
        return ResponseEntity.ok(response);
    }

    // Fetch applications by entity
    @GetMapping("/fetchByEntity/{entityId}")
    public ResponseEntity<List<SubsidyApplicationResponse>> getApplicationsByEntity(@PathVariable Long entityId) {
        List<SubsidyApplicationResponse> responses = service.getApplicationsByEntity(entityId);
        return ResponseEntity.ok(responses);
    }

    // Financial officer approves application
    @PutMapping("/approve/{id}")
    public ResponseEntity<SubsidyApplicationResponse> approveApplication(@PathVariable Long id) {
        SubsidyApplicationResponse response = service.approveApplication(id);
        return ResponseEntity.ok(response);
    }

    // Financial officer rejects application
    @PutMapping("/reject/{id}")
    public ResponseEntity<SubsidyApplicationResponse> rejectApplication(@PathVariable Long id) {
        SubsidyApplicationResponse response = service.rejectApplication(id);
        return ResponseEntity.ok(response);
    }

    // Reporting: applications received for a program
    @GetMapping("/applicationsReceived/{programId}")
    public ResponseEntity<Long> getApplicationsReceived(@PathVariable Long programId) {
        return ResponseEntity.ok(service.getApplicationsReceived(programId));
    }
}
