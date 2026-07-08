package com.skillswap.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that an availability slot ends after it starts.
 */
@Documented
@Constraint(validatedBy = AvailabilitySlotValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidAvailabilitySlot {

    String message() default "End time must be after start time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
