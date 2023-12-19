package edu.harvard.iq.dataverse.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 *
 * @author Asbjoedt
 */
public class BooleanValidator implements ConstraintValidator<ValidateBoolean, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return isBooleanValid(value);
    }

    /**
     * Validate a boolean string value either "true" or "false".
     * (Null is invalid since the state always exists and should always be explicitly defined)
     * @param value The string value to validate
     * @return true when valid (valid string must be "true" or "false"), false when invalid (null = invalid!)
     */
    public static boolean isBooleanValid(String value) {
        return value != null && value.equals("true") ||
                value != null && value.equals("false");
    }
}
