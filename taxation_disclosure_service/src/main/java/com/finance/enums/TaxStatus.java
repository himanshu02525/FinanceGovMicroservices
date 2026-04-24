package com.finance.enums;

public enum TaxStatus {
    PENDING,  // Tax has been filed but payment/verification is not yet complete
    PAID,     // Tax obligation has been successfully fulfilled
    OVERDUE   // Payment deadline has passed without fulfillment
}