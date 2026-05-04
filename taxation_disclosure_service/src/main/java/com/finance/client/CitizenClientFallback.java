package com.finance.client;

import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CitizenClientFallback implements CitizenClient {

    @Override
    public Boolean validateCitizen(Long entityId) {
        log.error(
            "Fallback executed: Citizen Business Service is unavailable for entityId {}",
            entityId
        );

        // Safe default: assume entity does NOT exist
        return false;
    }
}