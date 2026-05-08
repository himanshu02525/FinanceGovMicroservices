package com.finance.client;
 
import org.springframework.cloud.openfeign.FeignClient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
 
@FeignClient(name = "citizen-business-service",fallback = CitizenClientFallback.class)
public interface CitizenClient {
 

    @GetMapping("/entities/validate/{entityId}")
    Boolean validateCitizen(@PathVariable("entityId") Long entityId);
}