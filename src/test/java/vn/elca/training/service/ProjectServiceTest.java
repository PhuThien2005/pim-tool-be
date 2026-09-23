package vn.elca.training.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import vn.elca.training.model.dto.request.CreateProjectRequest;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.request.UpdateProjectRequest;
import vn.elca.training.model.dto.response.ProjectDetailResponse;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.exception.InvalidProjectStatusException;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.model.exception.ProjectNumberAlreadyExistsException;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.impl.ProjectServiceImpl;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
public class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private SearchProjectCriteria criteria;
    private Pageable pageable;
    private Project project;
    private ProjectListResponse response;

    @Before
    public void setUp() {
        criteria = new SearchProjectCriteria();
        pageable = PageRequest.of(0, 10);

        project = Project.builder()
                .projectNumber(1001)
                .name("EFV")
                .customer("Customer A")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
        project.setId(1L);

        response = ProjectListResponse.builder()
                .id(1L)
                .projectNumber(1001)
                .name("EFV")
                .customer("Customer A")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();
    }

    @Test
    public void testSearchProjects_SuccessfulMapping() {
        Page<Project> projectPage = new PageImpl<>(Collections.singletonList(project), pageable, 1);
        Mockito.when(projectRepository.searchProjects(criteria, pageable)).thenReturn(projectPage);
        Mockito.when(modelMapper.map(project, ProjectListResponse.class)).thenReturn(response);

        Page<ProjectListResponse> result = projectService.searchProjects(criteria, pageable);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getTotalElements());
        Assert.assertEquals(Integer.valueOf(1001), result.getContent().get(0).getProjectNumber());
        Assert.assertEquals("EFV", result.getContent().get(0).getName());

        Mockito.verify(projectRepository, Mockito.times(1)).searchProjects(criteria, pageable);
        Mockito.verify(modelMapper, Mockito.times(1)).map(project, ProjectListResponse.class);
    }

    @Test
    public void testSearchProjects_EmptyResult() {
        Page<Project> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        Mockito.when(projectRepository.searchProjects(criteria, pageable)).thenReturn(emptyPage);

        Page<ProjectListResponse> result = projectService.searchProjects(criteria, pageable);

        Assert.assertNotNull(result);
        Assert.assertEquals(0, result.getTotalElements());
        Assert.assertTrue(result.getContent().isEmpty());

        Mockito.verify(projectRepository, Mockito.times(1)).searchProjects(criteria, pageable);
        Mockito.verifyNoInteractions(modelMapper);
    }

    @Test
    public void testDeleteProjects_Success() {
        Project p2 = Project.builder()
                .projectNumber(1002)
                .name("CXTRANET")
                .customer("Customer B")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 2, 1))
                .build();
        p2.setId(2L);

        List<Long> ids = java.util.Arrays.asList(1L, 2L);
        List<Project> projects = java.util.Arrays.asList(project, p2);

        Mockito.when(projectRepository.findAllById(ids)).thenReturn(projects);

        projectService.deleteProjects(ids);

        Mockito.verify(projectRepository, Mockito.times(1)).deleteAll(projects);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testDeleteProjects_NotFound_ThrowsException() {
        List<Long> ids = java.util.Arrays.asList(1L, 999L);
        Mockito.when(projectRepository.findAllById(ids)).thenReturn(Collections.singletonList(project));

        projectService.deleteProjects(ids);
    }

    @Test(expected = InvalidProjectStatusException.class)
    public void testDeleteProjects_InvalidStatus_ThrowsException() {
        Project inProgressProject = Project.builder()
                .projectNumber(1003)
                .name("CRYSTAL")
                .customer("Customer C")
                .status(ProjectStatus.INP)
                .startDate(LocalDate.of(2025, 3, 1))
                .build();
        inProgressProject.setId(3L);

        List<Long> ids = java.util.Arrays.asList(1L, 3L);
        Mockito.when(projectRepository.findAllById(ids)).thenReturn(java.util.Arrays.asList(project, inProgressProject));

        projectService.deleteProjects(ids);
    }

    @Test
    public void testDeleteProjects_EmptyList_DoesNothing() {
        projectService.deleteProjects(Collections.emptyList());
        projectService.deleteProjects(null);

        Mockito.verifyNoInteractions(projectRepository);
    }

    @Test
    public void testDeleteProject_Success() {
        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        projectService.deleteProject(1L);

        Mockito.verify(projectRepository, Mockito.times(1)).delete(project);
    }

    @Test
    public void testCreateProject_Success() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(1002)
                .name("Project B")
                .customer("Customer B")
                .groupId(1L)
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Group group = new Group();
        group.setId(1L);

        ProjectDetailResponse detailResponse = ProjectDetailResponse.builder()
                .id(2L)
                .projectNumber(1002)
                .name("Project B")
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(1002)).thenReturn(false);
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenAnswer(i -> {
            Project p = i.getArgument(0);
            p.setId(2L);
            return p;
        });
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detailResponse);

        ProjectDetailResponse result = projectService.createProject(request);

        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(1002), result.getProjectNumber());
        Mockito.verify(projectRepository, Mockito.times(1)).save(Mockito.any(Project.class));
    }

    @Test(expected = ProjectNumberAlreadyExistsException.class)
    public void testCreateProject_DuplicateNumber_ThrowsException() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(1001)
                .name("Project Duplicate")
                .customer("Customer A")
                .groupId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(1001)).thenReturn(true);

        projectService.createProject(request);
    }

    @Test
    public void testUpdateProject_Success() {
        project.setVersion(1L);

        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(1L)
                .name("Updated Name")
                .customer("Updated Customer")
                .groupId(1L)
                .status(ProjectStatus.INP)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Group group = new Group();
        group.setId(1L);

        ProjectDetailResponse detailResponse = ProjectDetailResponse.builder()
                .id(1L)
                .name("Updated Name")
                .version(2L)
                .build();

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(projectRepository.saveAndFlush(Mockito.any(Project.class))).thenReturn(project);
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detailResponse);

        ProjectDetailResponse result = projectService.updateProject(1L, request);

        Assert.assertNotNull(result);
        Assert.assertEquals("Updated Name", result.getName());
        Mockito.verify(projectRepository, Mockito.times(1)).saveAndFlush(project);
    }

    @Test(expected = ObjectOptimisticLockingFailureException.class)
    public void testUpdateProject_OptimisticLockingFailure_ThrowsException() {
        project.setVersion(2L);

        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(1L)
                .name("Stale Update")
                .customer("Customer")
                .groupId(1L)
                .status(ProjectStatus.INP)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        projectService.updateProject(1L, request);
    }
}
