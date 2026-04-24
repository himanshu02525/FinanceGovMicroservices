package com.finance.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finance.dto.FinancialProgramRequest;
import com.finance.dto.FinancialProgramResponse;
import com.finance.enums.ProgramStatus;
import com.finance.exceptions.ProgramNotFoundException;
import com.finance.model.FinancialProgram;
import com.finance.repository.FinancialProgramRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FinancialProgramServiceImpl implements FinancialProgramService {

    private final FinancialProgramRepository repository;

    @Override
    @Transactional
    public FinancialProgramResponse saveProgram(FinancialProgramRequest request) {
        log.info("Saving new financial program: {}", request);

        FinancialProgram program = new FinancialProgram();
        program.setTitle(request.getTitle());
        program.setDescription(request.getDescription());

        // Default start date to today if not provided
        if (request.getStartDate() == null) {
            program.setStartDate(LocalDate.now());
        } else {
            program.setStartDate(request.getStartDate());
        }

        program.setEndDate(request.getEndDate());
        program.setBudget(request.getBudget());

        // Default status to ACTIVE if not provided
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            program.setStatus(ProgramStatus.ACTIVE);
        } else {
            program.setStatus(ProgramStatus.valueOf(request.getStatus().toUpperCase()));
        }

        // Auto-close rule at creation
        if ((program.getBudget() != null && program.getBudget() <= 0)
                || (program.getEndDate() != null && program.getEndDate().isBefore(LocalDate.now()))) {
            program.setStatus(ProgramStatus.CLOSED);
            log.warn("Program auto-closed at creation due to budget exhausted or end date passed");
        }

        FinancialProgram savedProgram = repository.save(program);
        log.info("Financial program saved successfully with ID: {}", savedProgram.getProgramId());

        return toResponse(savedProgram);
    }

    @Override
    @Transactional
    public FinancialProgramResponse updateProgram(Long id, FinancialProgramRequest request) {
        log.info("Updating financial program ID: {} with data: {}", id, request);

        FinancialProgram existingProgram = repository.findById(id).orElseThrow(() -> {
            log.error("Financial program not found with ID: {}", id);
            return new ProgramNotFoundException(id);
        });

        existingProgram.setTitle(request.getTitle());
        existingProgram.setDescription(request.getDescription());

        // Default start date to today if not provided
        if (request.getStartDate() == null) {
            existingProgram.setStartDate(LocalDate.now());
        } else {
            existingProgram.setStartDate(request.getStartDate());
        }

        existingProgram.setEndDate(request.getEndDate());
        existingProgram.setBudget(request.getBudget());

        // Default status to ACTIVE if not provided
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            existingProgram.setStatus(ProgramStatus.ACTIVE);
        } else {
            existingProgram.setStatus(ProgramStatus.valueOf(request.getStatus().toUpperCase()));
        }

        // Auto-close rule at update
        if ((existingProgram.getBudget() != null && existingProgram.getBudget() <= 0)
                || (existingProgram.getEndDate() != null && existingProgram.getEndDate().isBefore(LocalDate.now()))) {
            existingProgram.setStatus(ProgramStatus.CLOSED);
            log.warn("Program ID {} auto-closed due to budget exhausted or end date passed", id);
        }

        FinancialProgram updatedProgram = repository.save(existingProgram);
        log.info("Financial program updated successfully with ID: {}", id);

        return toResponse(updatedProgram);
    }


    @Override
    public String deleteProgram(Long id) {
        log.info("Deleting financial program with ID: {}", id);
        if (!repository.existsById(id)) {
            log.error("Program with ID {} not found for deletion", id);
            throw new ProgramNotFoundException("Program with ID " + id + " not found");
        }
        repository.deleteById(id);
        log.info("Program deleted successfully with ID: {}", id);
        return "Program deleted successfully with ID " + id;
    }

    @Override
    public FinancialProgramResponse getProgramById(Long programId) {
        log.info("Fetching program by ID: {}", programId);
        FinancialProgram program = repository.findById(programId).orElseThrow(() -> {
            log.error("Program not found with ID: {}", programId);
            return new ProgramNotFoundException(programId);
        });
        return toResponse(program);
    }

    @Override
    public List<FinancialProgramResponse> getAllPrograms() {
        log.info("Fetching all financial programs");
        return repository.findAll()
                         .stream()
                         .map(this::toResponse)
                         .collect(Collectors.toList());
    }

    @Override
    public List<FinancialProgramResponse> getProgramsByStatus(ProgramStatus status) {
        log.info("Fetching programs by status: {}", status);
        return repository.getProgramsByStatus(status)
                         .stream()
                         .map(this::toResponse)
                         .collect(Collectors.toList());
    }

    

    private FinancialProgramResponse toResponse(FinancialProgram program) {
        return new FinancialProgramResponse(
                program.getProgramId(),
                program.getTitle(),
                program.getDescription(),
                program.getBudget(),
                program.getStatus().name()
        );
    }
    
    
    public long getTotalPrograms() {
        return repository.count();
    }

    public long getActivePrograms() {
        return repository.countByStatus(ProgramStatus.ACTIVE);
    }
    
    @Override
    public Map<String, Object> getProgramSummary() {
        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPrograms", repository.count());
        summary.put("activePrograms", repository.countByStatus(ProgramStatus.ACTIVE));
        summary.put("budgetUsed", repository.sumTotalBudgetAcrossAllPrograms()); // ✅ now uses program budgets
        return summary;
    }




}
