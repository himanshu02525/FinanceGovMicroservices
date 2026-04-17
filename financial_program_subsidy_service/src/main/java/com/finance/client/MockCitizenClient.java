package com.finance.client;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class MockCitizenClient implements CitizenClient {

    @Override
    public Boolean validateCitizen(Long entityId) {
        // Mock logic: accept only positive IDs
        return entityId != null && entityId > 0;
    }
}
