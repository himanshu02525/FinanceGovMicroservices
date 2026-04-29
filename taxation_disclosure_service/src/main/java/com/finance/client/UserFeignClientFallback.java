package com.finance.client;

import org.springframework.stereotype.Component;

import com.finance.dto.UserDto;

@Component
public class UserFeignClientFallback implements UserFeignClient {

    @Override
    public UserDto getUserById(Long id) {
        // Return null → notification will be skipped safely
        return null;
    }
}