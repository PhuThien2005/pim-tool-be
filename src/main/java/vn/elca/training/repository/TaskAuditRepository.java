package vn.elca.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * @author vlp
 *
 */
@Repository
public interface TaskAuditRepository extends JpaRepository<TaskAudit, Long> {}
