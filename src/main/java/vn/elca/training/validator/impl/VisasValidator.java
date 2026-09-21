package vn.elca.training.validator.impl;

import vn.elca.training.validator.annotation.ValidVisas;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Collection;
import java.util.regex.Pattern;

public class VisasValidator implements ConstraintValidator<ValidVisas, Collection<String>> {

    private static final Pattern VISA_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(Collection<String> visas, ConstraintValidatorContext context) {
        if (visas == null || visas.isEmpty()) {
            return true;
        }

        for (String visa : visas) {
            if (visa == null || !VISA_PATTERN.matcher(visa.trim()).matches()) {
                return false;
            }
        }
        return true;
    }
}
