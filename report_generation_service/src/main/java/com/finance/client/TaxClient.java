package com.finance.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@FeignClient(
    name = "taxation-disclosure-service",
    url = "http://localhost:8084"
)
public interface TaxClient {

    @GetMapping("/internal/tax/summary")
    Map<String, Object> getTaxStatistics();
}
