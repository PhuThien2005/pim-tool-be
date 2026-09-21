package vn.elca.training.validator;

import org.springframework.beans.BeanWrapperImpl;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

/**
 * Validator kiểm tra quan hệ giữa startDate và endDate bằng Reflection/BeanWrapper.
 * Nếu endDate không null, nó bắt buộc phải sau startDate (endDate.isAfter(startDate)).
 */
public class StartBeforeEndDateValidator implements ConstraintValidator<StartBeforeEndDate, Object> {

    private String startDateField;
    private String endDateField;
    private String message;
    private boolean allowEqual;

    @Override
    public void initialize(StartBeforeEndDate constraintAnnotation) {
        this.startDateField = constraintAnnotation.startDateField();
        this.endDateField = constraintAnnotation.endDateField();
        this.message = constraintAnnotation.message();
        this.allowEqual = constraintAnnotation.allowEqual();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        try {
            BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
            Object startObj = wrapper.getPropertyValue(startDateField);
            Object endObj = wrapper.getPropertyValue(endDateField);

            // Nếu 1 trong 2 ngày là null, bỏ qua (để @NotNull lo nếu trường đó bắt buộc)
            if (startObj == null || endObj == null) {
                return true;
            }

            if (startObj instanceof LocalDate && endObj instanceof LocalDate) {
                LocalDate startDate = (LocalDate) startObj;
                LocalDate endDate = (LocalDate) endObj;

                boolean isValid = allowEqual ? !endDate.isBefore(startDate) : endDate.isAfter(startDate);
                if (!isValid) {
                    // Gắn lỗi trực tiếp vào trường endDate để frontend highlight đỏ đúng trường
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message)
                            .addPropertyNode(endDateField)
                            .addConstraintViolation();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            // Nếu không đọc được property thì bỏ qua để không làm gián đoạn hệ thống
            return true;
        }
    }
}
