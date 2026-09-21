package vn.elca.training.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

/**
 * Validator triển khai kiểm tra định dạng VISA: Đúng 3 chữ cái in hoa.
 */
public class VisaValidator implements ConstraintValidator<ValidVisa, String> {

    private static final Pattern VISA_PATTERN = Pattern.compile("^[A-Z]{3}$");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // Nếu null hoặc rỗng thì để @NotNull hoặc @NotBlank kiểm tra
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        return VISA_PATTERN.matcher(value.trim()).matches();
    }
}
