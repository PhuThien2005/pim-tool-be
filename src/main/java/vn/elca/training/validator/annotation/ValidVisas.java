package vn.elca.training.validator.annotation;

import vn.elca.training.validator.impl.VisasValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = VisasValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidVisas {

    String message() default "One or more member VISAs have invalid format. Each VISA must consist of exactly 3 uppercase letters.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
