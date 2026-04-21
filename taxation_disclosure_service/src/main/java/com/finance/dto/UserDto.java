package com.finance.dto; 

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
 
    private Long userId;
    private String name;
    private String email;
} 