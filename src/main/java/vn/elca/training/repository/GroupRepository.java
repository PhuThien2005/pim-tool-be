package vn.elca.training.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    @EntityGraph(attributePaths = {"groupLeader"})
    Slice<Group> findAllBy(Pageable pageable);
}
