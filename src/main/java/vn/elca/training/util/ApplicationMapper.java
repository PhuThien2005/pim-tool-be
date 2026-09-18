package vn.elca.training.util;

import org.springframework.stereotype.Component;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.dto.TaskDto;
import vn.elca.training.model.dto.UserDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Task;

import java.util.stream.Collectors;

/**
 * @author gtn
 */
@Component
public class ApplicationMapper {
    public ApplicationMapper() {
        // Mapper utility class
    }

    public ProjectDto projectToProjectDto(Project entity) {
        if (entity == null) {
            return null;
        }
        ProjectDto dto = new ProjectDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setFinishingDate(entity.getFinishingDate());
        dto.setCustomer(entity.getCustomer());

        return dto;
    }

    public TaskDto taskToTaskDto(Task task) {
        TaskDto dto = new TaskDto();
        dto.setId(task.getId());
        dto.setTaskName(task.getName());
        dto.setDeadline(task.getDeadline());
        dto.setProjectName(task.getProject() != null
                ? task.getProject().getName()
                : null);

        return dto;
    }

    public UserDto userToUserDto(Employee user) {
        if (user == null) {
            return null;
        }
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        if (user.getTasks() != null) {
            dto.setTasks(user.getTasks().stream().map(this::taskToTaskDto).collect(Collectors.toList()));
        }

        return dto;
    }
}
