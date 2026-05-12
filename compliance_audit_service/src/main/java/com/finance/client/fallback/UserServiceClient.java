package com.finance.client.fallback;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.finance.client.UserFeignClient;
import com.finance.dto.UserResponseDto;
import com.finance.exceptions.ServiceUnavailableException;
import com.finance.exceptions.UserNotFoundException;
import com.finance.util.MessageUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceClient {

	private final UserFeignClient userFeignClient;
	private final MessageUtil messageUtil;

	public ResponseEntity<UserResponseDto> getOfficerById(Long officerId) {
		try {
			return userFeignClient.getOfficerById(officerId);
		} catch (feign.FeignException.NotFound ex) {
			throw new UserNotFoundException(messageUtil.getMessage("not.found.message", "Officer", officerId));
		}
	}

	@SuppressWarnings("unused")
	private ResponseEntity<UserResponseDto> getOfficerById(Long officerId, Throwable ex) {
		log.error("Entity service error for entityId={}. Reason: {}", officerId, ex.getMessage());
		if (ex instanceof UserNotFoundException userNotFoundException) {
			throw userNotFoundException;
		}
		throw new ServiceUnavailableException(messageUtil.getMessage("external.service.unavailable", "Entity"));
	}

}
