package vn.elca.training.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Employee;

/**
 * @author gtn
 *
 */
@Repository
public interface UserRepository extends JpaRepository<Employee, Long>, QuerydslPredicateExecutor<Employee> {
    @EntityGraph(attributePaths = {"tasks", "tasks.project"})
    Employee findUserByUsername(String username);
}
