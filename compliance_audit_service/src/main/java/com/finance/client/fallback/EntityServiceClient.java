package com.finance.client.fallback;

import org.springframework.stereotype.Service;

import com.finance.client.EntityFeignClient;
import com.finance.exceptions.EntityNotFoundException;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.util.MessageUtil;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class EntityServiceClient {

	private final EntityFeignClient entityFeignClient;
	private final MessageUtil messageUtil;

	@CircuitBreaker(name = "entityService", fallbackMethod = "validateEntityFallback")
	public Boolean validateEntity(Long entityId) {
		try {
			return entityFeignClient.validateEntity(entityId);
		} catch (feign.FeignException.NotFound ex) {
			throw new EntityNotFoundException(messageUtil.getMessage("not.found.message", "Entity", entityId));
		}
	}

	@SuppressWarnings("unused")
	private Boolean validateEntityFallback(Long entityId, Throwable ex) {
		log.error("Entity service error for entityId={}. Reason: {}", entityId, ex.getMessage());
		if (ex instanceof EntityNotFoundException entityNotFoundException) {
			throw entityNotFoundException;
		}
		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Entity"));
	}
}