package vn.elca.training.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vn.elca.training.validator.impl.VisaValidator;

public class VisaValidatorTest {

    private VisaValidator validator;

    @Before
    public void setUp() {
        validator = new VisaValidator();
    }

    @Test
    public void testValidVisa_ExactlyThreeUppercaseLetters() {
        Assert.assertTrue(validator.isValid("DTH", null));
        Assert.assertTrue(validator.isValid("BHU", null));
        Assert.assertTrue(validator.isValid("XYZ", null));
    }

    @Test
    public void testValidVisa_NullOrEmpty() {
        Assert.assertTrue(validator.isValid(null, null));
        Assert.assertTrue(validator.isValid("", null));
        Assert.assertTrue(validator.isValid("   ", null));
    }

    @Test
    public void testInvalidVisa_Lowercase() {
        Assert.assertFalse(validator.isValid("dth", null));
        Assert.assertFalse(validator.isValid("DTh", null));
    }

    @Test
    public void testInvalidVisa_LengthNotThree() {
        Assert.assertFalse(validator.isValid("D", null));
        Assert.assertFalse(validator.isValid("DT", null));
        Assert.assertFalse(validator.isValid("DTHX", null));
    }

    @Test
    public void testInvalidVisa_ContainsNumbersOrSpecialChars() {
        Assert.assertFalse(validator.isValid("DT1", null));
        Assert.assertFalse(validator.isValid("123", null));
        Assert.assertFalse(validator.isValid("D-T", null));
    }
}
