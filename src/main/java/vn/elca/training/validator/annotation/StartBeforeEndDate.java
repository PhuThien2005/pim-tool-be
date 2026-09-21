package vn.elca.training.validator.annotation;

import vn.elca.training.validator.impl.StartBeforeEndDateValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = StartBeforeEndDateValidator.class)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(StartBeforeEndDate.List.class)
public @interface StartBeforeEndDate {

    String message() default "End date must be later than start date.";

    String startDateField() default "startDate";

    String endDateField() default "endDate";

    boolean allowEqual() default false;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    @Documented
    @interface List {
        StartBeforeEndDate[] value();
    }
}
