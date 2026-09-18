package vn.elca.training.web;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.model.dto.UserDto;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.service.UserService;

import java.util.List;

/**
 * @author gtn
 *
 */
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users")
public class UserController extends AbstractApplicationController {

    @Autowired
    UserService userService;

    @GetMapping("/id/{id}")
    public Employee findOne(@PathVariable Long id) {
        return userService.findOne(id);
    }

    @GetMapping("/{username}")
    public UserDto findOne(@PathVariable String username) {
        Employee user = userService.findOne(username);
        return mapper.userToUserDto(user);
    }

    @PostMapping("/{username}/addTasks")
    public UserDto addTasks(@RequestBody List<Long> taskIds, @PathVariable String username) {
        if (CollectionUtils.isEmpty(taskIds)) {
            throw new IllegalArgumentException("Invalid request! List taskIds is empty");
        } else if (StringUtils.isBlank(username)) {
            throw new IllegalArgumentException("Invalid request! Username is blank");
        }

        UserDto user = userService.addTasksToUser(taskIds, username);
        if (user == null) {
            throw new IllegalArgumentException("User with username '" + username + "' does not exist!");
        }
        return user;
    }

    @PutMapping({"/update"})
    public Employee update(@RequestBody Employee user) {
        if (user == null) {
            throw new IllegalArgumentException("Invalid request! User not found");
        }

        return userService.update(user);
    }
}
