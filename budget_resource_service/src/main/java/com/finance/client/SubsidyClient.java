package com.finance.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "financial-program-subsidy-service")
public interface SubsidyClient {

    @GetMapping("/subsidies/approved_sum/{programId}")
    BigDecimal getApprovedAmount(
            @PathVariable Long programId);
}