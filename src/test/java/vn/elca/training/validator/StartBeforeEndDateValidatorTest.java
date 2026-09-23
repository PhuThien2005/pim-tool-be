package vn.elca.training.validator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import vn.elca.training.validator.annotation.StartBeforeEndDate;
import vn.elca.training.validator.impl.StartBeforeEndDateValidator;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class StartBeforeEndDateValidatorTest {

    @Getter
    @Setter
    @AllArgsConstructor
    public static class TestDateRange {
        private LocalDate startDate;
        private LocalDate endDate;
    }

    private StartBeforeEndDateValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @Before
    public void setUp() {
        validator = new StartBeforeEndDateValidator();
        context = Mockito.mock(ConstraintValidatorContext.class);
        violationBuilder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeBuilder = Mockito.mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        Mockito.when(context.buildConstraintViolationWithTemplate(Mockito.anyString())).thenReturn(violationBuilder);
        Mockito.when(violationBuilder.addPropertyNode(Mockito.anyString())).thenReturn(nodeBuilder);
    }

    private void initValidator(boolean allowEqual) {
        StartBeforeEndDate annotation = Mockito.mock(StartBeforeEndDate.class);
        Mockito.when(annotation.startDateField()).thenReturn("startDate");
        Mockito.when(annotation.endDateField()).thenReturn("endDate");
        Mockito.when(annotation.message()).thenReturn("End date must be after start date");
        Mockito.when(annotation.allowEqual()).thenReturn(allowEqual);
        validator.initialize(annotation);
    }

    @Test
    public void testValid_StartDateBeforeEndDate() {
        initValidator(false);
        TestDateRange target = new TestDateRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 10));
        Assert.assertTrue(validator.isValid(target, context));
    }

    @Test
    public void testInvalid_StartDateAfterEndDate() {
        initValidator(false);
        TestDateRange target = new TestDateRange(LocalDate.of(2025, 2, 1), LocalDate.of(2025, 1, 1));
        Assert.assertFalse(validator.isValid(target, context));
    }

    @Test
    public void testEqualDates_WhenAllowEqualTrue_IsValid() {
        initValidator(true);
        LocalDate date = LocalDate.of(2025, 5, 5);
        TestDateRange target = new TestDateRange(date, date);
        Assert.assertTrue(validator.isValid(target, context));
    }

    @Test
    public void testEqualDates_WhenAllowEqualFalse_IsInvalid() {
        initValidator(false);
        LocalDate date = LocalDate.of(2025, 5, 5);
        TestDateRange target = new TestDateRange(date, date);
        Assert.assertFalse(validator.isValid(target, context));
    }

    @Test
    public void testValid_NullDates() {
        initValidator(false);
        Assert.assertTrue(validator.isValid(null, context));
        Assert.assertTrue(validator.isValid(new TestDateRange(null, LocalDate.of(2025, 1, 1)), context));
        Assert.assertTrue(validator.isValid(new TestDateRange(LocalDate.of(2025, 1, 1), null), context));
    }
}
