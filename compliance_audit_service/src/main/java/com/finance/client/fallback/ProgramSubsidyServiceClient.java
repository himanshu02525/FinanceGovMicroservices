package com.finance.client.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.ProgramSubsidyFeignClient;
import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;
import com.finance.dto.SubsidyUpdateRequest;
import com.finance.exceptions.ProgramNotFoundException;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.exceptions.SubsidyNotFoundException;
import com.finance.util.MessageUtil;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProgramSubsidyServiceClient {
	private static final String EXTERNAL_SERVICE_UNAVAILABLE = "external.service.unavailable";
	private static final String NOT_FOUND_MESSAGE = "not.found.message";
	private static final String PROGRAM = "Program";
	private final ProgramSubsidyFeignClient programSubsidyFeignClient;
	private final MessageUtil messageUtil;

	@CircuitBreaker(name = "programSubsidyService", fallbackMethod = "getProgramFallback")
	public ResponseEntity<FinancialProgramResponse> getProgramById(Long id) {
		try {
			return programSubsidyFeignClient.getProgramById(id);
		} catch (feign.FeignException.NotFound ex) {
			throw new ProgramNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, PROGRAM, id));
		}
	}

	@SuppressWarnings("unused")
	private ResponseEntity<FinancialProgramResponse> getProgramFallback(Long id, Throwable ex) {

		log.error("Program service error for id={}", id, ex);

		if (ex instanceof ProgramNotFoundException pnfe) {
			throw pnfe;
		}

		throw new ServiceUnavailableException(messageUtil.getMessage(EXTERNAL_SERVICE_UNAVAILABLE, PROGRAM));
	}

	@CircuitBreaker(name = "programSubsidyService", fallbackMethod = "getSubsidyFallback")
	public ResponseEntity<SubsidyResponse> getSubsidyById(Long id) {
		try {
			return programSubsidyFeignClient.getSubsidyById(id);
		} catch (feign.FeignException.NotFound ex) {
			throw new SubsidyNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, "Subsidy", id));
		}
	}

	@SuppressWarnings("unused")
	private ResponseEntity<SubsidyResponse> getSubsidyFallback(Long id, Throwable ex) {

		log.error("Subsidy service error for id={}", id, ex);

		if (ex instanceof SubsidyNotFoundException snfe) {
			throw snfe;
		}

		throw new ServiceUnavailableException(messageUtil.getMessage(EXTERNAL_SERVICE_UNAVAILABLE, "Subsidy"));
	}

	@CircuitBreaker(name = "programSubsidyService", fallbackMethod = "updateProgramStatusFallback")
	public ResponseEntity<SubsidyResponse> updateSubsidy(SubsidyUpdateRequest requestBody, Long subsidyId) {
		try {
			return programSubsidyFeignClient.updateSubsidy(requestBody, subsidyId);
		} catch (feign.FeignException.NotFound ex) {
			throw new ProgramNotFoundException(messageUtil.getMessage(NOT_FOUND_MESSAGE, PROGRAM, subsidyId));
		}
	}

	@SuppressWarnings("unused")
	private ResponseEntity<FinancialProgramResponse> updateProgramStatusFallback(Long id, Throwable ex) {

		log.error("Program update service error for id={}", id, ex);

		if (ex instanceof ProgramNotFoundException pnfe) {
			throw pnfe;
		}

		throw new ServiceUnavailableException(messageUtil.getMessage(EXTERNAL_SERVICE_UNAVAILABLE, PROGRAM));
	}
}