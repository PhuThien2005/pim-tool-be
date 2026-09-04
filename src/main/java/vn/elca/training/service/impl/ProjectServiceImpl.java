package vn.elca.training.service.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.ApplicationMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author vlp
 *
 */
@Service
@Profile("!dummy | dev")
public class ProjectServiceImpl implements ProjectService {

    private ProjectRepository projectRepository;
    private ApplicationMapper mapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ApplicationMapper mapper) {
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public List<ProjectDto> searchByKeyword(String keyword) {
        return projectRepository.findAll().stream()
                .filter(p-> p.getName() != null && p.getName().contains(keyword))
                .map(mapper::projectToProjectDto)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return projectRepository.count();
    }

    @Override
    public ProjectDto findProjectById(Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        return project != null ? mapper.projectToProjectDto(project) : null;
    }

    @Override
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            if (projectDto.getName() != null) {
                project.setName(projectDto.getName());
            }
            if (projectDto.getCustomer() != null) {
                project.setCustomer(projectDto.getCustomer());
            }
            if (projectDto.getFinishingDate() != null) {
                project.setFinishingDate(projectDto.getFinishingDate());
            }
            project = projectRepository.save(project);
            return mapper.projectToProjectDto(project);
        }
        return null;
    }
}
