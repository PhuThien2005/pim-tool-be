package vn.elca.training.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.util.SqlCaptureInspector;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ProjectExecutionPlanTest {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ProjectRepository projectRepository;

    private void executeAndPrintExplain(String title, String sql) {
        System.out.println("================================================================================");
        System.out.println("EXPLAIN ANALYZE: " + title);
        System.out.println("SQL: " + sql);
        System.out.println("--------------------------------------------------------------------------------");
        List<?> resultList = entityManager.createNativeQuery("EXPLAIN ANALYZE " + sql).getResultList();
        for (Object row : resultList) {
            System.out.println(row);
        }
        System.out.println("================================================================================\n");
        Assert.assertFalse(resultList.isEmpty());
    }

    @Test
    public void testExplainAnalyze_FindProjectDetailById() {
        String sql = "SELECT p.ID, p.NAME, p.CUSTOMER, p.STATUS, g.ID, gl.ID, gl.VISA, pe.PROJECT_ID, e.ID, e.VISA " +
                "FROM PROJECT p " +
                "LEFT JOIN \"GROUP\" g ON p.GROUP_ID = g.ID " +
                "LEFT JOIN EMPLOYEE gl ON g.GROUP_LEADER_ID = gl.ID " +
                "LEFT JOIN PROJECT_EMPLOYEE pe ON p.ID = pe.PROJECT_ID " +
                "LEFT JOIN EMPLOYEE e ON pe.EMPLOYEE_ID = e.ID " +
                "WHERE p.ID = 1";
        executeAndPrintExplain("Find Project Detail by ID (EntityGraph JOIN)", sql);
    }

    @Test
    public void testExplainAnalyze_SearchProjectsByStatusAndCustomer() {
        String sql = "SELECT p.ID, p.PROJECT_NUMBER, p.NAME, p.STATUS, p.CUSTOMER " +
                "FROM PROJECT p " +
                "WHERE p.STATUS = 'NEW' AND p.CUSTOMER = 'Canton de Vaud'";
        executeAndPrintExplain("Search Projects with Composite Index (idx_project_status_customer)", sql);
    }

    @Test
    public void testExplainAnalyze_SearchProjectsByMemberVisa() {
        String sql = "SELECT DISTINCT p.ID, p.PROJECT_NUMBER, p.NAME " +
                "FROM PROJECT p " +
                "INNER JOIN PROJECT_EMPLOYEE pe ON p.ID = pe.PROJECT_ID " +
                "INNER JOIN EMPLOYEE e ON pe.EMPLOYEE_ID = e.ID " +
                "WHERE e.VISA = 'HTV'";
        executeAndPrintExplain("Search Projects by Member VISA", sql);
    }

    @Test
    public void testExplainAnalyze_FindEmployeeByVisa() {
        String sql = "SELECT e.ID, e.VISA, e.FIRST_NAME, e.LAST_NAME " +
                "FROM EMPLOYEE e " +
                "WHERE e.VISA = 'DTH'";
        executeAndPrintExplain("Find Employee by Unique VISA (idx_employee_visa)", sql);
    }

    @Test
    public void testExplainAnalyze_FullTableScan_UnindexedColumn() {
        String sql = "SELECT p.ID, p.NAME, p.START_DATE " +
                "FROM PROJECT p " +
                "WHERE p.START_DATE >= '2025-01-01'";
        executeAndPrintExplain("Full Table Scan on Unindexed Column (START_DATE)", sql);
    }

    @Test
    public void testExplainAnalyze_FullTableScan_LeadingWildcard() {
        String sql = "SELECT p.ID, p.NAME " +
                "FROM PROJECT p " +
                "WHERE LOWER(p.NAME) LIKE '%ball%'";
        executeAndPrintExplain("Full Table Scan with Leading Wildcard (LOWER(NAME) LIKE '%ball%')", sql);
    }

    @Test
    public void testExplainAnalyze_ActualHibernateQuery_FindDetailById() {
        SqlCaptureInspector.clear();
        projectRepository.findDetailById(1L);

        List<String> queries = SqlCaptureInspector.getQueries();
        Assert.assertEquals(1, queries.size());

        String generatedSql = queries.get(0);
        String executableSql = generatedSql.replace("?", "1");
        executeAndPrintExplain("Actual Hibernate Generated SQL for findDetailById(1L)", executableSql);
    }

    @Test
    public void testExplainAnalyze_ActualHibernateQuery_FullTableScan_FindByNameContaining() {
        SqlCaptureInspector.clear();
        projectRepository.findByNameContainingIgnoreCase("ball");

        List<String> queries = SqlCaptureInspector.getQueries();
        Assert.assertFalse(queries.isEmpty());

        String generatedSql = queries.get(0);
        String executableSql = generatedSql.replaceFirst("\\?", "'%BALL%'").replaceFirst("\\?", "'\\\\'");
        executeAndPrintExplain("Actual Hibernate Generated SQL for findByNameContainingIgnoreCase('ball')", executableSql);
    }
}
