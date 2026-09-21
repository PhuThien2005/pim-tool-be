package vn.elca.training.repository.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.entity.Project;

public interface ProjectRepositoryCustom {
    Page<Project> searchProjects(SearchProjectCriteria criteria, Pageable pageable);
}
