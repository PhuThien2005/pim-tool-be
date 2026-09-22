package vn.elca.training.util;

import com.p6spy.engine.spy.appender.MessageFormattingStrategy;
import org.hibernate.engine.jdbc.internal.BasicFormatterImpl;
import org.springframework.stereotype.Component;

@Component
public class P6SpySqlFormatter implements MessageFormattingStrategy {

    private final BasicFormatterImpl formatter = new BasicFormatterImpl();

    @Override
    public String formatMessage(int connectionId, String now, long elapsed, String category, String prepared, String sql, String url) {
        if (sql == null || sql.trim().isEmpty()) {
            return "";
        }
        String cleanSql = sql.trim();
        String upper = cleanSql.toUpperCase();
        if (upper.startsWith("SELECT")) {
            String formattedSql = formatter.format(cleanSql);
            return String.format(
                    "==> (Took %dms):\n" +
                    "EXPLAIN ANALYZE%s;\n" +
                    "--------------------------------------------------------------------------------", elapsed, formattedSql);
        }
        return String.format("\n[SQL EXECUTE] (took %dms): %s;", elapsed, cleanSql);
    }
}
