package com.finance.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RoleRedirectUtil {

	public static String getEndPoint(String roleName) {
		// Using a null check to prevent NullPointerExceptions
		log.info("Redirecting user with role: {}", roleName);

		switch (roleName) {
		case "ROLE_ADMIN":
			return "/api/admin/dashboard";
		case "ROLE_FINANCIAL_OFFICER":
			return "/api/officer/dashboard";
		case "ROLE_PROGRAM_MANAGER":
			return "/api/manager/dashboard";
		case "ROLE_COMPLIANCE_OFFICER":
			return "/api/compliance/dashboard";
		case "ROLE_GOVERNMENT_AUDITOR":
			return "/api/auditor/dashboard";
		case "ROLE_CITIZEN":
			return "/api/citizen/home";
		default:
			log.warn("Unknown role encountered: {}", roleName);
			return "/api/auth/login?error=unauthorized";
		}
	}
}