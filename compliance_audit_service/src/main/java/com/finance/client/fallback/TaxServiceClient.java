package com.finance.client.fallback;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.TaxFeignClient;
import com.finance.dto.TaxResponseDTO;
import com.finance.dto.TaxUpdateDTO;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.exceptions.TaxRecordNotFoundException;
import com.finance.util.MessageUtil;

import feign.FeignException;
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
	public ResponseEntity<TaxResponseDTO> getTaxById(Long taxId) {
		return taxFeignClient.getTaxById(taxId);
	}

	public ResponseEntity<TaxResponseDTO> getTaxFallback(Long taxId, Throwable ex) {

		if (ex instanceof FeignException.NotFound) {
			throw new TaxRecordNotFoundException("Tax record not found for ID: " + taxId);
		}

		// Other failures (service down, timeout, etc.)
		return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
	}

	@CircuitBreaker(name = "taxService", fallbackMethod = "getTaxStatusUpdateFallback")
	public ResponseEntity<TaxResponseDTO> verifySingleTax(Long taxId, TaxUpdateDTO taxUpdateDTO) {
		try {
			return taxFeignClient.verifySingleTax(taxId, taxUpdateDTO);
		} catch (feign.FeignException.NotFound ex) {
			throw new TaxRecordNotFoundException(messageUtil.getMessage("not.found.message", "Tax", taxId));
		}
	}

	@SuppressWarnings("unused")
	private ResponseEntity<TaxResponseDTO> getTaxStatusUpdateFallback(Long taxId, Throwable ex) {

		log.error("Tax update failure for taxId={}", taxId, ex);

		if (ex instanceof TaxRecordNotFoundException tnfe) {
			throw tnfe;
		}

		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Taxation"));
	}

}