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
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.exception.GroupNotFoundException;
import vn.elca.training.model.exception.InvalidProjectStatusException;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.model.exception.ProjectNumberAlreadyExistsException;
import vn.elca.training.model.exception.VisaNotFoundException;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.impl.ProjectServiceImpl;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @Test
    public void testDeleteProjects_DuplicateIds_Success() {
        List<Long> duplicateIds = java.util.Arrays.asList(1L, 1L);
        Mockito.when(projectRepository.findAllById(Collections.singletonList(1L))).thenReturn(Collections.singletonList(project));

        projectService.deleteProjects(duplicateIds);

        Mockito.verify(projectRepository, Mockito.times(1)).deleteAll(Collections.singletonList(project));
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

    @Test
    public void testGetProject_Success() {
        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(1L)
                .projectNumber(1001)
                .name("EFV")
                .build();

        Mockito.when(projectRepository.findDetailById(1L)).thenReturn(java.util.Optional.of(project));
        Mockito.when(modelMapper.map(project, ProjectDetailResponse.class)).thenReturn(detail);

        ProjectDetailResponse result = projectService.getProject(1L);

        Assert.assertNotNull(result);
        Assert.assertEquals(1L, result.getId().longValue());
        Mockito.verify(projectRepository, Mockito.times(1)).findDetailById(1L);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testGetProject_NotFound_ThrowsException() {
        Mockito.when(projectRepository.findDetailById(999L)).thenReturn(java.util.Optional.empty());

        projectService.getProject(999L);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testCreateProject_GroupNotFound_ThrowsException() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(2001)
                .name("Project")
                .customer("Customer")
                .groupId(999L)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(2001)).thenReturn(false);
        Mockito.when(groupRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        projectService.createProject(request);
    }

    @Test(expected = VisaNotFoundException.class)
    public void testCreateProject_VisaNotFound_ThrowsException() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(2001)
                .name("Project")
                .customer("Customer")
                .groupId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .visas(new HashSet<>(Arrays.asList("UNKNOWN_VISA")))
                .build();

        Group group = new Group();
        group.setId(1L);

        Mockito.when(projectRepository.existsByProjectNumber(2001)).thenReturn(false);
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(employeeRepository.findByVisaIn(Mockito.anySet())).thenReturn(Collections.emptyList());

        projectService.createProject(request);
    }

    @Test
    public void testCreateProject_WithVisas_Success() {
        Employee emp = new Employee();
        emp.setId(10L);
        emp.setVisa("DTH");

        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(2002)
                .name("Project With Visa")
                .customer("Customer")
                .groupId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .visas(new HashSet<>(Arrays.asList("DTH", " ")))
                .build();

        Group group = new Group();
        group.setId(1L);

        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(20L)
                .projectNumber(2002)
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(2002)).thenReturn(false);
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(employeeRepository.findByVisaIn(Mockito.anySet())).thenReturn(Collections.singletonList(emp));
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detail);

        ProjectDetailResponse result = projectService.createProject(request);

        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(2002), result.getProjectNumber());
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testUpdateProject_NotFound_ThrowsException() {
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(1L)
                .build();

        Mockito.when(projectRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        projectService.updateProject(999L, request);
    }

    @Test(expected = GroupNotFoundException.class)
    public void testUpdateProject_GroupNotFound_ThrowsException() {
        project.setVersion(1L);
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(1L)
                .groupId(999L)
                .build();

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        Mockito.when(groupRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        projectService.updateProject(1L, request);
    }

    @Test(expected = ProjectNotFoundException.class)
    public void testDeleteProject_NotFound_ThrowsException() {
        Mockito.when(projectRepository.findById(999L)).thenReturn(java.util.Optional.empty());

        projectService.deleteProject(999L);
    }

    @Test(expected = InvalidProjectStatusException.class)
    public void testDeleteProject_InvalidStatus_ThrowsException() {
        project.setStatus(ProjectStatus.INP);
        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));

        projectService.deleteProject(1L);
    }


    @Test
    public void testDeleteProjects_NullOrEmpty_ReturnsEarly() {
        projectService.deleteProjects(null);
        projectService.deleteProjects(Collections.emptyList());

        Mockito.verify(projectRepository, Mockito.never()).findAllById(Mockito.anyList());
        Mockito.verify(projectRepository, Mockito.never()).deleteAll(Mockito.anyList());
    }

    @Test
    public void testDeleteProjects_Success_DeletesAll() {
        Project p1 = Project.builder().projectNumber(1001).status(ProjectStatus.NEW).build();
        p1.setId(1L);
        Project p2 = Project.builder().projectNumber(1002).status(ProjectStatus.NEW).build();
        p2.setId(2L);

        List<Long> ids = Arrays.asList(1L, 2L, 1L); // contains duplicate
        Mockito.when(projectRepository.findAllById(Arrays.asList(1L, 2L))).thenReturn(Arrays.asList(p1, p2));

        projectService.deleteProjects(ids);

        Mockito.verify(projectRepository, Mockito.times(1)).deleteAll(Arrays.asList(p1, p2));
    }

    @Test
    public void testCreateProject_NullOptionalFields_DefaultsApplied() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(3001)
                .name(null)
                .customer(null)
                .status(null)
                .groupId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .visas(null)
                .build();

        Group group = new Group();
        group.setId(1L);

        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(50L)
                .projectNumber(3001)
                .status(ProjectStatus.NEW)
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(3001)).thenReturn(false);
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detail);

        ProjectDetailResponse result = projectService.createProject(request);

        Assert.assertNotNull(result);
        Assert.assertEquals(Integer.valueOf(3001), result.getProjectNumber());
    }

    @Test
    public void testCreateProject_VisasAllBlank_ResolvesToEmptySet() {
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(3002)
                .groupId(1L)
                .startDate(LocalDate.of(2025, 1, 1))
                .visas(new HashSet<>(Arrays.asList("   ", null, "")))
                .build();

        Group group = new Group();
        group.setId(1L);

        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(51L)
                .projectNumber(3002)
                .build();

        Mockito.when(projectRepository.existsByProjectNumber(3002)).thenReturn(false);
        Mockito.when(groupRepository.findById(1L)).thenReturn(java.util.Optional.of(group));
        Mockito.when(projectRepository.save(Mockito.any(Project.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detail);

        ProjectDetailResponse result = projectService.createProject(request);

        Assert.assertNotNull(result);
        Mockito.verify(employeeRepository, Mockito.never()).findByVisaIn(Mockito.anySet());
    }

    @Test
    public void testUpdateProject_Success_NullFieldsHandled() {
        project.setVersion(1L);
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(1L)
                .name(null)
                .customer(null)
                .status(ProjectStatus.INP)
                .groupId(2L)
                .startDate(LocalDate.of(2025, 1, 1))
                .visas(Collections.emptySet())
                .build();

        Group newGroup = new Group();
        newGroup.setId(2L);

        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(1L)
                .status(ProjectStatus.INP)
                .build();

        Mockito.when(projectRepository.findById(1L)).thenReturn(java.util.Optional.of(project));
        Mockito.when(groupRepository.findById(2L)).thenReturn(java.util.Optional.of(newGroup));
        Mockito.when(projectRepository.saveAndFlush(Mockito.any(Project.class))).thenAnswer(i -> i.getArgument(0));
        Mockito.when(modelMapper.map(Mockito.any(Project.class), Mockito.eq(ProjectDetailResponse.class))).thenReturn(detail);

        ProjectDetailResponse result = projectService.updateProject(1L, request);

        Assert.assertNotNull(result);
        Assert.assertEquals(ProjectStatus.INP, result.getStatus());
    }
}
