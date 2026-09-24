package vn.elca.training.model.dto.request;

import com.querydsl.core.types.Predicate;
import org.junit.Assert;
import org.junit.Test;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;

public class SearchProjectCriteriaTest {

    @Test
    public void testToPredicate_EmptyCriteria() {
        SearchProjectCriteria criteria = new SearchProjectCriteria();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
    }

    @Test
    public void testToPredicate_TextKeyword() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("EFV")
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().contains("project.name"));
        Assert.assertTrue(predicate.toString().contains("project.customer"));
    }

    @Test
    public void testToPredicate_NumericKeyword() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("1001")
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().contains("project.projectNumber = 1001"));
    }

    @Test
    public void testToPredicate_StatusFilter() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .status(ProjectStatus.INP)
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().contains("project.status = INP"));
    }

    @Test
    public void testToPredicate_LeaderVisaFilter() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .leaderVisa("DTH")
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().toLowerCase().contains("group.groupleader.visa"));
    }

    @Test
    public void testToPredicate_MemberVisaFilter() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .memberVisas(java.util.Collections.singleton("BHU"))
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().toLowerCase().contains("any(project.employees).visa"));
    }

    @Test
    public void testToPredicate_DateRangeFilters() {
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 12, 31);
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .startDateFrom(from)
                .startDateTo(to)
                .endDateFrom(from)
                .endDateTo(to)
                .build();
        Predicate predicate = criteria.toPredicate();
        Assert.assertNotNull(predicate);
        Assert.assertTrue(predicate.toString().contains("project.startDate >= 2025-01-01"));
        Assert.assertTrue(predicate.toString().contains("project.startDate <= 2025-12-31"));
    }
}
