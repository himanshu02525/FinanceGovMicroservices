package com.finance.enums;

public enum DisclosureStatus {
    SUBMITTED, // Initial status when a citizen/business files a disclosure
    VALIDATED, // Set by a Financial Officer after verifying the details
    REJECTED   // Set if the disclosure information is incorrect or fraudulent
}