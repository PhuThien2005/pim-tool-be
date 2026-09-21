package vn.elca.training.repository.custom;

import vn.elca.training.model.entity.Project;

import java.util.Set;

public interface ProjectRepositoryCustom {
    Set<Project> searchWithCriteria();
}
