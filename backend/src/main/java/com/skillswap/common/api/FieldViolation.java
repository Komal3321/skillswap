package com.skillswap.common.api;

public record FieldViolation(
        String field,
        String message) {
}
