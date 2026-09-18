package vn.elca.training.service;

import vn.elca.training.model.dto.UserDto;
import vn.elca.training.model.entity.Employee;

import java.util.List;

/**
 * @author gtn
 *
 */
public interface UserService {
    Employee findOne(Long id);

    Employee findOne(String username);

    UserDto addTasksToUser(List<Long> taskIds, String username);

    Employee update(Employee user);
}
