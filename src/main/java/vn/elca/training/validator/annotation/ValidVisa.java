package vn.elca.training.validator.annotation;

import vn.elca.training.validator.impl.VisaValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = VisaValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD, ElementType.TYPE_USE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVisa {

    String message() default "Invalid VISA format. VISA must consist of exactly 3 uppercase letters (e.g. DTH, BHU).";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
