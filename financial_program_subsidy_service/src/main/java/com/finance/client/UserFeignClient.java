package com.finance.client;

import java.util.List;
 
import com.finance.dto.UserDto;
 
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
 
/**
* Feign client for Identity/User microservice.
*/
@FeignClient(
    name = "identity-service"
)
public interface UserFeignClient {
 
    /**
     * Fetch all users from Identity Service
     */
    @GetMapping("/getAllUsers")
    List<UserDto> getAllUsers();
}