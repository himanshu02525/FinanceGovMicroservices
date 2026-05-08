package com.finance.client;
 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
 
import com.finance.dto.ComplianceCreateRequest;
import com.finance.dto.ComplianceResponse;
 
@FeignClient(name = "compliance-audit-service")
public interface ComplianceFeignClient {
 
    @PostMapping("/compliance")
    ComplianceResponse create(@RequestBody ComplianceCreateRequest request);
}