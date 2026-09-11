package vn.elca.training.service;

import java.util.List;

import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;

/**
 * @author vlp
 *
 */
public interface ProjectService {
    List<Project> findAll();
    List<ProjectDto> searchByKeyword(String keyword);
    long count();
    ProjectDto findProjectById(Long id);
    ProjectDto updateProject(Long id, ProjectDto projectDto);
    Project createMaintenanceProject(Long oldProjectId) throws Exception;
    Project createMaintenanceProjectWithException(Long oldProjectId, boolean simulateError) throws Exception;
}
