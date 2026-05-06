package com.finance.client.fallback;

import org.springframework.stereotype.Service;

import com.finance.client.TaxFeignClient;
import com.finance.dto.TaxResponseDTO;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.util.MessageUtil;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TaxServiceClient {

	private final TaxFeignClient taxFeignClient;
	private final MessageUtil messageUtil;

	@CircuitBreaker(name = "taxService", fallbackMethod = "getTaxFallback")
	public TaxResponseDTO getTaxById(Long taxId) {
		try {
			return taxFeignClient.getTaxById(taxId);
		} catch (feign.FeignException.NotFound ex) {
			throw new TaxRecordNotFoundException(messageUtil.getMessage("not.found.message", "Tax", taxId));
		}
	}

	@SuppressWarnings("unused")
	private TaxResponseDTO getTaxFallback(Long taxId, Throwable ex) {
		log.error("Tax service failure for taxId={}. Reason: {}", taxId, ex.getMessage());
		if (ex instanceof TaxRecordNotFoundException taxRecordNotFoundException) {
			throw new TaxRecordNotFoundException(messageUtil.getMessage("not.found.message", "Tax", taxId));
		}
		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Taxation"));
	}
}