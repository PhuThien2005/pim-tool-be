package vn.elca.training.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation kiểm tra danh sách VISA của Project Members (Collection hoặc Set của String).
 * Mỗi VISA trong danh sách phải đúng 3 chữ cái in hoa.
 */
@Documented
@Constraint(validatedBy = VisasValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVisas {

    String message() default "One or more member VISAs have invalid format. Each VISA must consist of exactly 3 uppercase letters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
