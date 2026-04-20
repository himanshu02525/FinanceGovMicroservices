package com.finance.client;
 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
 
@FeignClient(name = "citizen-service")
@Profile("!dev")
 
public interface CitizenClient {
 
//    @GetMapping("/fetch/{id}")
//    CitizenBusinessResponse getCitizenById(@PathVariable("id") Long id);
    @GetMapping("/validate/{entityId}")
    Boolean validateCitizen(@PathVariable("entityId") Long entityId);
}