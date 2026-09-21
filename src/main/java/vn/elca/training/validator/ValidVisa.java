package vn.elca.training.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation kiểm tra định dạng VISA của Employee.
 * Theo yêu cầu của ELCA PIM Tool: VISA gồm đúng 3 chữ cái in hoa (ví dụ: DTH, BHU, JHV).
 */
@Documented
@Constraint(validatedBy = VisaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVisa {

    String message() default "Invalid VISA format. VISA must consist of exactly 3 uppercase letters (e.g. DTH, BHU).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
