package vn.elca.training.util;

import org.hibernate.resource.jdbc.spi.StatementInspector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SqlCaptureInspector implements StatementInspector {

    private static final List<String> QUERIES = Collections.synchronizedList(new ArrayList<>());

    @Override
    public String inspect(String sql) {
        if (sql != null && !sql.trim().toUpperCase().startsWith("EXPLAIN")) {
            QUERIES.add(sql);
        }
        return sql;
    }

    public static void clear() {
        QUERIES.clear();
    }

    public static List<String> getQueries() {
        return new ArrayList<>(QUERIES);
    }

    public static String getLastQuery() {
        if (QUERIES.isEmpty()) {
            return null;
        }
        return QUERIES.get(QUERIES.size() - 1);
    }
}
