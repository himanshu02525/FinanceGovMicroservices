package com.finance.client.fallback;

import org.springframework.stereotype.Service;

import com.finance.client.ProgramSubsidyFeignClient;
import com.finance.dto.FinancialProgramResponse;
import com.finance.dto.SubsidyResponse;
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

	private final ProgramSubsidyFeignClient programSubsidyFeignClient;
	private final MessageUtil messageUtil;

	@CircuitBreaker(name = "programSubsidyService", fallbackMethod = "getProgramFallback")
	public FinancialProgramResponse getProgramById(Long id) {
		try {
			return programSubsidyFeignClient.getProgramById(id);
		} catch (feign.FeignException.NotFound ex) {
			throw new ProgramNotFoundException(messageUtil.getMessage("not.found.message", "Program", id));
		}
	}

	@SuppressWarnings("unused")
	private FinancialProgramResponse getProgramFallback(Long id, Throwable ex) {
		log.error("Program service error for id={}: {}", id, ex.getMessage());
		if (ex instanceof ProgramNotFoundException programNotFoundException) {
			throw programNotFoundException;
		}
		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Program"));
	}

	@CircuitBreaker(name = "programSubsidyService", fallbackMethod = "getSubsidyFallback")
	public SubsidyResponse getSubsidyById(Long id) {
		try {
			return programSubsidyFeignClient.getSubsidyById(id);
		} catch (feign.FeignException.NotFound ex) {
			throw new SubsidyNotFoundException(messageUtil.getMessage("not.found.message", "Subsidy", id));
		}
	}

	@SuppressWarnings("unused")
	private SubsidyResponse getSubsidyFallback(Long id, Throwable ex) {
		log.error("Subsidy service error for id={}: {}", id, ex.getMessage());
		if (ex instanceof SubsidyNotFoundException subsidyNotFoundException) {
			throw subsidyNotFoundException;
		}
		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Subsidy"));
	}
}