package vn.elca.training.validator.impl;

import org.springframework.beans.BeanWrapperImpl;
import vn.elca.training.validator.annotation.StartBeforeEndDate;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

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

            if (startObj == null || endObj == null) {
                return true;
            }

            if (startObj instanceof LocalDate && endObj instanceof LocalDate) {
                LocalDate startDate = (LocalDate) startObj;
                LocalDate endDate = (LocalDate) endObj;

                boolean isValid = allowEqual ? !endDate.isBefore(startDate) : endDate.isAfter(startDate);
                if (!isValid) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate(message)
                            .addPropertyNode(endDateField)
                            .addConstraintViolation();
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            return true;
        }
    }
}
