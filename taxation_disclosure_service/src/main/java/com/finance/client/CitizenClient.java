package com.finance.client;
 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
 
@FeignClient(name = "citizen-business-service")
public interface CitizenClient {
 
//    @GetMapping("/fetch/{id}")
    @GetMapping("/entities/validate/{entityId}")
    Boolean validateCitizen(@PathVariable("entityId") Long entityId);
}