package org.example.expert.domain.user.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.example.expert.domain.user.validation.annotation.ValidPassword;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value.length() < 8) return false;

        boolean hasDigit = value.matches(".*\\d.*");
        boolean hasUpper = value.matches(".*[A-Z].*");

        return hasDigit && hasUpper;
    }
}
