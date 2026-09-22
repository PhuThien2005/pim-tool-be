package vn.elca.training.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.dto.response.GroupListResponse;
import vn.elca.training.model.entity.Group;
import vn.elca.training.validator.annotation.StartBeforeEndDate;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
}
