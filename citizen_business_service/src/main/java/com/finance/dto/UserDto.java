package com.finance.dto;

import com.finance.enums.RoleType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

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