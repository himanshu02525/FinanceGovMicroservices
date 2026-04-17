package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(
    name = "financial-program-subsidy-service",
    url = "http://localhost:8083"
)
public interface SubsidyClient {

    @GetMapping("/internal/program/summary")
    Map<String, Object> getProgramSummary();

    @GetMapping("/internal/subsidy/summary")
    Map<String, Object> getSubsidySummary();
}
