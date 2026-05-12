package com.finance.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.finance.dto.SubsidyApplicationResponse;
import com.finance.dto.SubsidyApplicationRequest;
import com.finance.service.SubsidyApplicationService;

@RestController
@RequestMapping("/applications")
@CrossOrigin(origins = "http://localhost:5173")
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
        return ResponseEntity.ok(service.getApplicationsByEntity(entityId));
    }

    // Financial officer approves application
    @PutMapping("/approve/{id}")
    public ResponseEntity<SubsidyApplicationResponse> approveApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.approveApplication(id));
    }

    // Financial officer rejects application
    @PutMapping("/reject/{id}")
    public ResponseEntity<SubsidyApplicationResponse> rejectApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.rejectApplication(id));
    }

    // Reporting: applications received for a program
    @GetMapping("/applicationsReceived/{programId}")
    public ResponseEntity<Long> getApplicationsReceived(@PathVariable Long programId) {
        return ResponseEntity.ok(service.getApplicationsReceived(programId));
    }

    // Fetch applications by program
    @GetMapping("/fetchByProgram/{programId}")
    public ResponseEntity<List<SubsidyApplicationResponse>> fetchByProgram(@PathVariable Long programId) {
        return ResponseEntity.ok(service.fetchByProgram(programId));
    }

    // ✅ NEW: Fetch all applications (for Financial Officer)
    @GetMapping("/fetchAll")
    public ResponseEntity<List<SubsidyApplicationResponse>> fetchAllApplications() {
        return ResponseEntity.ok(service.getAllApplications());
    }
}
