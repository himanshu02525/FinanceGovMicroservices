package com.finance.dto;
 
import com.finance.enums.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
 
/**
* DTO representing User from Identity service.
* Follows FinanceGov User table.
*/
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
 
    private Long userId;
    private String name;
    private RoleType role;
    private String email;
    private String phone;
    private String status;
} 