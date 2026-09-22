package vn.elca.training.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
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
import vn.elca.training.service.ProjectService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@AllArgsConstructor
@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private GroupRepository groupRepository;
    private EmployeeRepository employeeRepository;
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public ProjectDetailResponse getProject(Long projectId) {
        Project project = projectRepository.findDetailById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        return modelMapper.map(project, ProjectDetailResponse.class);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<ProjectListResponse> searchProjects(SearchProjectCriteria criteria, Pageable pageable) {
        return projectRepository.searchProjects(criteria, pageable)
                .map(p -> modelMapper.map(p, ProjectListResponse.class));
    }

    @Override
    public ProjectDetailResponse createProject(CreateProjectRequest request) {
        if (projectRepository.existsByProjectNumber(request.getProjectNumber())) {
            throw new ProjectNumberAlreadyExistsException(request.getProjectNumber());
        }

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(request.getGroupId()));

        Set<Employee> employees = resolveEmployeesByVisas(request.getVisas());

        Project project = Project.builder()
                .projectNumber(request.getProjectNumber())
                .name(request.getName() != null ? request.getName().trim() : null)
                .customer(request.getCustomer() != null ? request.getCustomer().trim() : null)
                .status(request.getStatus() != null ? request.getStatus() : ProjectStatus.NEW)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        project.setGroup(group);
        project.setEmployees(employees);

        Project saved = projectRepository.save(project);
        return modelMapper.map(saved, ProjectDetailResponse.class);
    }

    @Override
    public ProjectDetailResponse updateProject(Long projectId, UpdateProjectRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (!project.getVersion().equals(request.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Project.class, projectId);
        }

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new GroupNotFoundException(request.getGroupId()));

        Set<Employee> employees = resolveEmployeesByVisas(request.getVisas());

        project.setName(request.getName() != null ? request.getName().trim() : null);
        project.setCustomer(request.getCustomer() != null ? request.getCustomer().trim() : null);
        project.setStatus(request.getStatus());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setGroup(group);
        project.setEmployees(employees);

        Project updated = projectRepository.saveAndFlush(project);
        return modelMapper.map(updated, ProjectDetailResponse.class);
    }

    @Override
    public void deleteProject(Long projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));

        if (project.getStatus() != ProjectStatus.NEW) {
            throw new InvalidProjectStatusException("Only projects with status NEW can be deleted");
        }

        projectRepository.delete(project);
    }

    @Override
    public void deleteProjects(List<Long> projectIds) {
        if (CollectionUtils.isEmpty(projectIds)) {
            return;
        }

        List<Project> projects = projectRepository.findAllById(projectIds);
        if (projects.size() != projectIds.size()) {
            Set<Long> foundIds = projects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toSet());
            List<Long> notFoundIds = projectIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new ProjectNotFoundException(notFoundIds);
        }

        List<Long> invalidProjectIds = projects.stream()
                .filter(p -> p.getStatus() != ProjectStatus.NEW)
                .map(Project::getId)
                .collect(Collectors.toList());

        if (!invalidProjectIds.isEmpty()) {
            throw new InvalidProjectStatusException("Only projects with status NEW can be deleted", invalidProjectIds);
        }

        projectRepository.deleteAll(projects);
    }

    private Set<Employee> resolveEmployeesByVisas(Set<String> visas) {
        if (visas == null || visas.isEmpty()) {
            return new HashSet<>();
        }

        Set<String> trimmedVisas = visas.stream()
                .filter(v -> v != null && !v.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toSet());

        if (trimmedVisas.isEmpty()) {
            return new HashSet<>();
        }

        List<Employee> foundEmployees = employeeRepository.findByVisaIn(trimmedVisas);
        Set<String> foundVisas = foundEmployees.stream()
                .map(Employee::getVisa)
                .collect(Collectors.toSet());

        List<String> notFoundVisas = trimmedVisas.stream()
                .filter(v -> !foundVisas.contains(v))
                .sorted()
                .collect(Collectors.toList());

        if (!notFoundVisas.isEmpty()) {
            throw new VisaNotFoundException(notFoundVisas);
        }

        return new HashSet<>(foundEmployees);
    }
}
