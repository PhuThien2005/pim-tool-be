package vn.elca.training.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.model.entity.Project;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.repository.TaskRepository;
import vn.elca.training.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@Transactional
public class UserServiceTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Test
    public void testAddTasksToUser_SyncBidirectionalRelationship() {
        // 1. Prepare User and Project with Tasks
        Employee user = userRepository.save(new Employee("dev_user", "Developer"));
        Project project = projectRepository.save(new Project("Project Alpha", LocalDate.now().plusYears(1)));

        Task task1 = taskRepository.save(new Task(project, "Dev Task 1"));
        Task task2 = taskRepository.save(new Task(project, "Dev Task 2"));

        List<Long> taskIds = Arrays.asList(task1.getId(), task2.getId());

        // 2. Execute addTasksToUser
        UserDto updatedUser = userService.addTasksToUser(taskIds, "dev_user");
        Assert.assertNotNull(updatedUser);
        Assert.assertEquals(2, updatedUser.getTasks().size());

        // 3. Flush and clear persistence context to verify real DB state
        em.flush();
        em.clear();

        // 4. Verify tasks in DB now have foreign key user_id set to dev_user
        Task reloadedTask1 = taskRepository.findById(task1.getId()).orElse(null);
        Assert.assertNotNull(reloadedTask1);
        Assert.assertNotNull(reloadedTask1.getUser());
        Assert.assertEquals("dev_user", reloadedTask1.getUser().getUsername());

        Task reloadedTask2 = taskRepository.findById(task2.getId()).orElse(null);
        Assert.assertNotNull(reloadedTask2);
        Assert.assertNotNull(reloadedTask2.getUser());
        Assert.assertEquals("dev_user", reloadedTask2.getUser().getUsername());
    }

    @Test
    public void testUserJacksonSerialization_NoInfiniteRecursion() throws Exception {
        Employee user = userRepository.save(new Employee("json_user", "Tester"));
        Project project = projectRepository.save(new Project("Project Beta", LocalDate.now().plusYears(1)));
        Task task = taskRepository.save(new Task(project, "Beta Task"));

        userService.addTasksToUser(Arrays.asList(task.getId()), "json_user");

        Employee loadedUser = userService.findOne(user.getId());
        Assert.assertNotNull(loadedUser);

        // Verify that Jackson serializes User without StackOverflowError / infinite recursion
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(loadedUser);

        Assert.assertNotNull(json);
        Assert.assertTrue(json.contains("json_user"));
        Assert.assertTrue(json.contains("Beta Task"));
    }
}
