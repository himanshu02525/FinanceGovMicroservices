package com.finance.client.mock;
import java.math.BigDecimal;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.finance.client.SubsidyClient;

@Component
@Profile("dev")
public class MockSubsidyClient implements SubsidyClient {

    @Override
    public BigDecimal getApprovedAmount(Long programId) {
        return BigDecimal.valueOf(100000); // mock used budget
    }
}