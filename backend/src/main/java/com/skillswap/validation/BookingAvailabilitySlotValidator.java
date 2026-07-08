package com.skillswap.validation;

import com.skillswap.dto.request.booking.AvailabilityRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Bean Validation validator for booking availability slots.
 */
public class BookingAvailabilitySlotValidator implements ConstraintValidator<ValidBookingAvailabilitySlot, AvailabilityRequest> {

    @Override
    public boolean isValid(AvailabilityRequest request, ConstraintValidatorContext context) {
        if (request == null || request.startTime() == null || request.endTime() == null) {
            return true;
        }
        return request.endTime().isAfter(request.startTime());
    }
}
