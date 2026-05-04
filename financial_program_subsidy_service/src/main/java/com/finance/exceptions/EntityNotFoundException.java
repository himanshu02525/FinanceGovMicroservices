package com.finance.exceptions;

@SuppressWarnings("serial")
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(Long entityId) {
        super("No subsidies found for entity ID " + entityId);
    }
}
