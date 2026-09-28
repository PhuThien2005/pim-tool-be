package vn.elca.training.util;

import org.junit.Assert;
import org.junit.Test;

public class P6SpySqlFormatterTest {

    private final P6SpySqlFormatter formatter = new P6SpySqlFormatter();

    @Test
    public void testFormatMessage_CrossJoin_FormattedCorrectly() {
        String rawSql = "select p.ID from PROJECT p cross join \"GROUP\" g cross join EMPLOYEE e where p.GROUP_ID = g.ID";
        String formatted = formatter.formatMessage(1, "now", 5L, "statement", "", rawSql, "");

        Assert.assertNotNull(formatted);
        Assert.assertTrue(formatted.contains("cross join"));
        Assert.assertFalse("Should not have 'cross' separated from 'join'", formatted.matches("(?s).*\\bcross\\s*\\r?\\n\\s*join\\b.*"));
        Assert.assertTrue(formatted.contains("\n    cross join"));
    }

    @Test
    public void testFormatMessage_NonSelect_ReturnsSimpleLog() {
        String rawSql = "update PROJECT set NAME = 'Test' where ID = 1";
        String formatted = formatter.formatMessage(1, "now", 2L, "statement", "", rawSql, "");

        Assert.assertEquals("\n[SQL EXECUTE] (took 2ms): update PROJECT set NAME = 'Test' where ID = 1;", formatted);
    }
}
