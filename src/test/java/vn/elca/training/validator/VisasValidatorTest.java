package vn.elca.training.validator;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import vn.elca.training.validator.impl.VisasValidator;

import java.util.Arrays;
import java.util.Collections;

public class VisasValidatorTest {

    private VisasValidator validator;

    @Before
    public void setUp() {
        validator = new VisasValidator();
    }

    @Test
    public void testValidVisas_AllUppercaseThreeLetters() {
        Assert.assertTrue(validator.isValid(Arrays.asList("DTH", "BHU", "JHV"), null));
    }

    @Test
    public void testValidVisas_NullOrEmpty() {
        Assert.assertTrue(validator.isValid(null, null));
        Assert.assertTrue(validator.isValid(Collections.emptyList(), null));
    }

    @Test
    public void testInvalidVisas_ContainsLowercase() {
        Assert.assertFalse(validator.isValid(Arrays.asList("DTH", "bhu"), null));
    }

    @Test
    public void testInvalidVisas_ContainsInvalidLength() {
        Assert.assertFalse(validator.isValid(Arrays.asList("DTH", "LONGVISA"), null));
        Assert.assertFalse(validator.isValid(Arrays.asList("DTH", "NO"), null));
    }

    @Test
    public void testInvalidVisas_ContainsNull() {
        Assert.assertFalse(validator.isValid(Arrays.asList("DTH", null), null));
    }
}
