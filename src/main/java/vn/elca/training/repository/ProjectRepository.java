package vn.elca.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import vn.elca.training.model.entity.Project;
import vn.elca.training.repository.custom.ProjectRepositoryCustom;

import java.util.List;

/**
 * @author vlp
 *
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, QuerydslPredicateExecutor<Project>, ProjectRepositoryCustom {
    public List<Project> findByNameContainingIgnoreCase(String name);
}
