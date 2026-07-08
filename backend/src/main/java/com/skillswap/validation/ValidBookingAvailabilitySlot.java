package com.skillswap.validation;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/**
 * Validates that a booking availability slot ends after it starts.
 */
@Documented
@Constraint(validatedBy = BookingAvailabilitySlotValidator.class)
@Target(TYPE)
@Retention(RUNTIME)
public @interface ValidBookingAvailabilitySlot {

    String message() default "End time must be after start time";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
