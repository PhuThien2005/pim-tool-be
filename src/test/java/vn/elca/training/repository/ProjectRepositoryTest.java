package vn.elca.training.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private org.modelmapper.ModelMapper modelMapper;

    @Test
    public void testSearchProjects_EmptyCriteria_ReturnsAll() {
        SearchProjectCriteria criteria = new SearchProjectCriteria();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertNotNull(page);
        Assert.assertEquals(25, page.getTotalElements());
        Assert.assertEquals(Integer.valueOf(1001), page.getContent().get(0).getProjectNumber());
    }

    @Test
    public void testSearchProjects_ByKeyword_Number() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("1003")
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(1, page.getTotalElements());
        Assert.assertEquals("CRYSTAL BALL", page.getContent().get(0).getName());
    }

    @Test
    public void testSearchProjects_ByKeyword_Name() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("EFV")
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(1, page.getTotalElements());
        Assert.assertEquals(Integer.valueOf(1001), page.getContent().get(0).getProjectNumber());
    }

    @Test
    public void testSearchProjects_ByKeyword_Customer() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("Swisscom")
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(3, page.getTotalElements());
    }

    @Test
    public void testSearchProjects_ByStatus() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .status(ProjectStatus.NEW)
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(6, page.getTotalElements());
        Assert.assertTrue(page.getContent().stream().allMatch(p -> p.getStatus() == ProjectStatus.NEW));
    }

    @Test
    public void testSearchProjects_ByLeaderVisa() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .leaderVisa("DTH")
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(6, page.getTotalElements());
        Assert.assertTrue(page.getContent().stream().allMatch(p -> "DTH".equalsIgnoreCase(p.getGroup().getGroupLeader().getVisa())));
    }

    @Test
    public void testSearchProjects_ByMemberVisa() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .memberVisas(java.util.Collections.singleton("HTV"))
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(4, page.getTotalElements());
    }

    @Test
    public void testSearchProjects_ByStartDateRange() {
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .startDateFrom(LocalDate.of(2025, 1, 1))
                .startDateTo(LocalDate.of(2025, 12, 31))
                .build();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10));

        Assert.assertEquals(12, page.getTotalElements());
    }

    @Test
    public void testSearchProjects_Pagination() {
        SearchProjectCriteria criteria = new SearchProjectCriteria();
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 5));

        Assert.assertEquals(25, page.getTotalElements());
        Assert.assertEquals(5, page.getContent().size());
        Assert.assertEquals(5, page.getTotalPages());
    }

    @Test
    public void testFindDetailById() {
        java.util.Optional<Project> projectOptional = projectRepository.findDetailById(1L);
        Assert.assertTrue(projectOptional.isPresent());
        Project project = projectOptional.get();
        Assert.assertNotNull(project.getGroup());
        Assert.assertNotNull(project.getGroup().getGroupLeader());
        Assert.assertNotNull(project.getEmployees());
        Assert.assertFalse(project.getEmployees().isEmpty());

        vn.elca.training.model.dto.response.ProjectDetailResponse response =
                modelMapper.map(project, vn.elca.training.model.dto.response.ProjectDetailResponse.class);
        Assert.assertNotNull(response);
        Assert.assertNotNull(response.getGroup());
        Assert.assertNotNull(response.getGroup().getGroupLeader());
        Assert.assertEquals(2, response.getEmployees().size());
    }

    @Test
    public void testSearchProjects_DynamicSorting_MultiColumn() {
        SearchProjectCriteria criteria = new SearchProjectCriteria();
        org.springframework.data.domain.Sort sort = org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Order.desc("status"),
                org.springframework.data.domain.Sort.Order.asc("projectNumber")
        );
        Page<Project> page = projectRepository.searchProjects(criteria, PageRequest.of(0, 10, sort));

        Assert.assertNotNull(page);
        Assert.assertEquals(25, page.getTotalElements());
        Assert.assertFalse(page.getContent().isEmpty());
    }
}
