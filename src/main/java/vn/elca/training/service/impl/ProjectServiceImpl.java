package vn.elca.training.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.exception.InvalidProjectStatusException;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
@AllArgsConstructor
@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<ProjectListResponse> searchProjects(SearchProjectCriteria criteria, Pageable pageable) {
        return projectRepository.searchProjects(criteria, pageable)
                .map(p -> modelMapper.map(p, ProjectListResponse.class));
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
}
