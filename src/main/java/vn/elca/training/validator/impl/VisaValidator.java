package vn.elca.training.validator.impl;

import vn.elca.training.validator.annotation.ValidVisa;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class VisaValidator implements ConstraintValidator<ValidVisa, String> {

    private static final Pattern VISA_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return VISA_PATTERN.matcher(value.trim()).matches();
    }
}
