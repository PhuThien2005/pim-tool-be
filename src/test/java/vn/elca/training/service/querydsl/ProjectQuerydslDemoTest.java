package vn.elca.training.service.querydsl;

import org.junit.Assert;
import org.junit.Before;
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
import vn.elca.training.repository.UserRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Unit Test thực hành các tính năng QueryDSL được viết trong ProjectQuerydslDemoService.
 * Bạn có thể nhấp chuột phải và Run file test này để quan sát câu lệnh SQL Hibernate sinh ra.
 */
@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@Transactional
public class ProjectQuerydslDemoTest {

    @Autowired
    private ProjectQuerydslDemoService demoService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private Project prjKsta;
    private Project prjSecutix;
    private Project prjCustom;
    private Employee devJohn;

    @Before
    public void setUp() {
        // Chuẩn bị dữ liệu mẫu cho Database H2: xóa task trước để không vi phạm Foreign Key constraint
        taskRepository.deleteAll();
        userRepository.deleteAll();
        projectRepository.deleteAll();

        // 1. Tạo User
        devJohn = new Employee();
        devJohn.setUsername("john.doe");
        devJohn = userRepository.save(devJohn);

        // 2. Tạo Projects
        prjKsta = new Project("KSTA", LocalDate.of(2025, 6, 30));
        prjKsta.setCustomer("ELCA Internal");
        prjKsta = projectRepository.save(prjKsta);

        prjSecutix = new Project("SECUTIX", LocalDate.of(2025, 12, 31));
        prjSecutix.setCustomer("Ticketing Co");
        prjSecutix = projectRepository.save(prjSecutix);

        prjCustom = new Project("ONBOARDING_APP", LocalDate.of(2026, 3, 15));
        prjCustom.setCustomer("ELCA Training");
        prjCustom = projectRepository.save(prjCustom);

        // 3. Tạo Tasks
        Task task1 = new Task(prjKsta, "Setup CI/CD");
        task1.setUser(devJohn);
        taskRepository.save(task1);

        Task task2 = new Task(prjKsta, "Refactor JPA Query");
        taskRepository.save(task2);

        Task task3 = new Task(prjSecutix, "Security Audit");
        task3.setUser(devJohn);
        taskRepository.save(task3);
    }

    /**
     * Test 1: Tìm kiếm đơn giản bằng QuerydslPredicateExecutor
     */
    @Test
    public void testFindProjectsByNameBasic() {
        List<Project> results = demoService.findProjectsByNameBasic("KSTA");
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("KSTA", results.get(0).getName());
    }

    /**
     * Test 2: Tìm kiếm động theo Keyword (khớp cả Project Name hoặc Customer)
     */
    @Test
    public void testSearchProjectsDynamic_ByKeyword() {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setKeyword("ELCA"); // Khớp "ELCA Internal" (prjKsta) và "ELCA Training" (prjCustom)

        List<Project> results = demoService.searchProjectsDynamic(criteria);
        Assert.assertEquals(2, results.size());
    }

    /**
     * Test 3: Tìm kiếm động kết hợp nhiều tiêu chí (Keyword + Khoảng ngày)
     */
    @Test
    public void testSearchProjectsDynamic_CombinedFilters() {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        criteria.setKeyword("ELCA");
        // Chỉ lấy các dự án kết thúc trước năm 2026
        criteria.setFinishingDateTo(LocalDate.of(2025, 12, 31));

        List<Project> results = demoService.searchProjectsDynamic(criteria);
        // Chỉ có KSTA (hạn 2025-06-30), ONBOARDING_APP (2026-03-15) bị loại trừ
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("KSTA", results.get(0).getName());
    }

    /**
     * Test 4: Khi người dùng không chọn tiêu chí nào (criteria rỗng) -> trả về toàn bộ
     */
    @Test
    public void testSearchProjectsDynamic_EmptyCriteria() {
        ProjectSearchCriteria criteria = new ProjectSearchCriteria();
        List<Project> results = demoService.searchProjectsDynamic(criteria);
        Assert.assertEquals(3, results.size());
    }

    /**
     * Test 5: JOIN sang bảng Task và User
     */
    @Test
    public void testSearchProjectsWithTasks() {
        // Tìm dự án có task tên chứa "CI/CD"
        ProjectSearchCriteria criteriaTask = new ProjectSearchCriteria();
        criteriaTask.setTaskName("CI/CD");

        List<Project> results = demoService.searchProjectsWithTasks(criteriaTask);
        Assert.assertEquals(1, results.size());
        Assert.assertEquals("KSTA", results.get(0).getName());

        // Tìm dự án có task được phân công cho user "john.doe"
        ProjectSearchCriteria criteriaUser = new ProjectSearchCriteria();
        criteriaUser.setAssigneeUsername("john.doe");

        List<Project> resultsByUser = demoService.searchProjectsWithTasks(criteriaUser);
        // john.doe có task ở cả KSTA và SECUTIX
        Assert.assertEquals(2, resultsByUser.size());
    }

    /**
     * Test 6: Projection trực tiếp ra ProjectDto (không load Entity vào RAM)
     */
    @Test
    public void testFindProjectDtosProjected() {
        List<ProjectDto> dtos = demoService.findProjectDtosProjected("SECUTIX");
        Assert.assertEquals(1, dtos.size());
        ProjectDto dto = dtos.get(0);
        Assert.assertEquals("SECUTIX", dto.getName());
        Assert.assertEquals("Ticketing Co", dto.getCustomer());
    }

    /**
     * Test 7: Thống kê số lượng Task theo Project (Aggregation & Group By)
     */
    @Test
    public void testCountTasksPerProject() {
        Map<String, Long> countMap = demoService.countTasksPerProject();
        Assert.assertEquals(Long.valueOf(2), countMap.get("KSTA"));
        Assert.assertEquals(Long.valueOf(1), countMap.get("SECUTIX"));
    }
}
