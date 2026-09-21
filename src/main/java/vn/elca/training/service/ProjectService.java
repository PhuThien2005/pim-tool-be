package vn.elca.training.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.response.ProjectListResponse;

public interface ProjectService {
    public Page<ProjectListResponse> searchProjects(SearchProjectCriteria criteria, Pageable pageable);
    public void deleteProject(Long projectId);
    public void deleteProjects(List<Long> projectIds);
}
