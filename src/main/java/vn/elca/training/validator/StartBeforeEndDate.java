package vn.elca.training.validator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation kiểm tra logic: End Date phải sau Start Date (nếu End Date được nhập).
 * Theo yêu cầu của ELCA PIM Tool:
 * "END_DATE: Date. If this value is not null, it must be later than the project start date."
 *
 * Annotation này được đặt ở cấp CLASS (ElementType.TYPE) trên các Request DTO.
 */
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
