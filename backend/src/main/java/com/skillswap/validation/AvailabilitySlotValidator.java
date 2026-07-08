package com.skillswap.validation;

import com.skillswap.dto.request.profile.AvailabilityRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Bean Validation validator for weekly availability slots.
 */
public class AvailabilitySlotValidator implements ConstraintValidator<ValidAvailabilitySlot, AvailabilityRequest> {

    @Override
    public boolean isValid(AvailabilityRequest request, ConstraintValidatorContext context) {
        if (request == null || request.startTime() == null || request.endTime() == null) {
            return true;
        }
        return request.endTime().isAfter(request.startTime());
    }
}
