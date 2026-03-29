package com.omra.platform.exception;

import lombok.Getter;

@Getter
public class ForbiddenException extends RuntimeException {

    /** Code stable pour le client (ex. SUBSCRIPTION_INACTIVE). */
    private final String code;

    public ForbiddenException(String message) {
        this(message, null);
    }

    public ForbiddenException(String message, String code) {
        super(message);
        this.code = code;
    }
}
