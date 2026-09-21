package vn.elca.training.service.impl;

import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.exception.ApplicationUnexpectedException;
import vn.elca.training.repository.UserRepository;
import vn.elca.training.service.UserService;
import vn.elca.training.util.ApplicationMapper;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author gtn
 *
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;
    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ApplicationMapper applicationMapper;

    @Override
    public Employee findOne(Long id) {
        Employee user = userRepository.findById(id).orElse(null);
        if (user != null) {
            Hibernate.initialize(user.getTasks());
        }
        // Should throw exception if not found

        return user;
    }

    @Override
    public Employee findOne(String usename) {
        return userRepository.findUserByUsername(usename);
    }

    @Override
    public UserDto addTasksToUser(List<Long> taskIds, String username) {
        List<Task> tasks = taskRepository.findAllById(taskIds);
        if (tasks.isEmpty()) {
            throw new ApplicationUnexpectedException("Tasks don't exists!");
        }
        Employee user = findOne(username);
        if (user != null) {
            for (Task task : tasks) {
                task.setUser(user);
            }
            taskRepository.saveAll(tasks);
            tasks.addAll(user.getTasks());
            user.setTasks(tasks.stream().collect(Collectors.toSet()));
        }

        return applicationMapper.userToUserDto(user);
    }

    @Override
    public Employee update(Employee user) {
        return userRepository.save(user);
    }
}
