package vn.elca.training.service;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.repository.ProjectRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.List;

/**
 * Lớp kiểm thử chứng minh tính toàn vẹn Transactional của tính năng tạo dự án bảo trì (Maintenance Project) trong ProjectService.
 *
 * Các chứng minh bao gồm:
 * 1. Chứng minh thực nghiệm (Empirical Proof):
 *    - Thành công: Dự án cũ chuyển sang activated = false, dự án mới được tạo có tên '<tên cũ> Maint. <năm>' và activated = true.
 *    - Thất bại (có Exception): Toàn bộ transaction được ROLLBACK sạch sẽ -> Dự án cũ giữ nguyên activated = true, dự án mới KHÔNG được tạo.
 * 2. Chứng minh kiến trúc Spring AOP Proxy (Architectural Proof):
 *    - Kiểm tra bean ProjectService được Spring tiêm vào là một Proxy (JDK Dynamic Proxy hoặc CGLIB Proxy).
 *    - Xác thực class name mang tiền tố proxy (ví dụ: com.sun.proxy.$Proxy*).
 * 3. Chứng minh cơ chế bẫy tự gọi (Self-invocation pitfall):
 *    - Giải thích nguyên nhân tại sao gọi trực tiếp 'this.method()' trong cùng class sẽ bỏ qua Proxy của Spring.
 */
@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
public class ProjectServiceTransactionTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectRepository projectRepository;

    /**
     * Kịch bản 1: Tạo dự án bảo trì thành công trong 1 transaction.
     * - Dự án cũ phải được cập nhật activated = false.
     * - Dự án mới phải được lưu với tên '<tên cũ> Maint. <năm>' và activated = true.
     */
    @Test
    public void testCreateMaintenanceProjectSuccess() throws Exception {
        // 1. Chuẩn bị dự án phát triển ban đầu đang hoạt động (activated = true)
        Project oldProject = new Project("EFV_PROD", LocalDate.of(2026, 1, 1), "ELCA", ProjectStatus.INP);
        oldProject.setActivated(true);
        oldProject = projectRepository.saveAndFlush(oldProject);
        Long oldProjectId = oldProject.getId();

        // 2. Thực thi tạo bảo trì
        Project maintenanceProject = projectService.createMaintenanceProject(oldProjectId);
        em.clear();

        // 3. Kiểm tra kết quả
        int currentYear = LocalDate.now().getYear();
        String expectedMaintName = "EFV_PROD Maint. " + currentYear;

        // Dự án bảo trì mới được tạo
        Assert.assertNotNull(maintenanceProject);
        Assert.assertNotNull(maintenanceProject.getId());
        Assert.assertEquals(expectedMaintName, maintenanceProject.getName());
        Assert.assertTrue(maintenanceProject.isActivated());
        Assert.assertEquals("ELCA", maintenanceProject.getCustomer());

        // Dự án cũ đã chuyển sang không hoạt động (activated = false)
        Project reloadedOldProject = projectRepository.findById(oldProjectId).orElse(null);
        Assert.assertNotNull(reloadedOldProject);
        Assert.assertFalse("Dự án cũ phải chuyển sang inactive (activated = false)", reloadedOldProject.isActivated());
    }

    /**
     * Kịch bản 2: Chứng minh tính nguyên tử (Atomicity) và khả năng Rollback khi có Exception.
     * Khi bạn thêm code ném ngoại lệ (throw Exception) vào giữa method createMaintenanceProject,
     * hãy bỏ @Ignore ở đây để chạy test xác thực transaction rollback hoàn toàn.
     */
    @Test
//    @Ignore("Tạm bỏ qua: Bỏ comment @Ignore khi bạn tự thêm lệnh throw exception vào createMaintenanceProject để kiểm tra rollback")
    public void testCreateMaintenanceProjectRollbackOnException() {
        // 1. Chuẩn bị dự án phát triển ban đầu
        Project oldProject = new Project("TRANSACTION_TEST_PROJECT", LocalDate.of(2026, 1, 1), "CLIENT_XYZ", ProjectStatus.INP);
        oldProject.setActivated(true);
        oldProject = projectRepository.saveAndFlush(oldProject);
        Long oldProjectId = oldProject.getId();

        long initialProjectCount = projectRepository.count();

        // 2. Gọi tạo dự án bảo trì
        boolean exceptionThrown = false;
        try {
            projectService.createMaintenanceProject(oldProjectId);
        } catch (Exception e) {
            exceptionThrown = true;
        }

        Assert.assertTrue("Phải có ngoại lệ được ném ra trong quá trình xử lý", exceptionThrown);

        // Xóa sạch EntityManager session cache để chắc chắn đọc dữ liệu mới nhất từ database
        em.clear();

        // 3. XÁC MINH ROLLBACK HOÀN TOÀN:
        // A. Dự án cũ KHÔNG BỊ CẬP NHẬT: activated vẫn phải là true!
        Project reloadedOldProject = projectRepository.findById(oldProjectId).orElse(null);
        Assert.assertNotNull(reloadedOldProject);
        Assert.assertTrue("Dự án cũ KHÔNG được cập nhật (activated vẫn là true) do transaction đã rollback",
                reloadedOldProject.isActivated());

        // B. Dự án mới KHÔNG ĐƯỢC TẠO: số lượng dự án trong DB giữ nguyên
        long projectCountAfterError = projectRepository.count();
        Assert.assertEquals("Số lượng dự án trong DB không được tăng lên", initialProjectCount, projectCountAfterError);

        // C. Tìm theo tên dự án mới không có kết quả
        int currentYear = LocalDate.now().getYear();
        String maintName = "TRANSACTION_TEST_PROJECT Maint. " + currentYear;
        List<Project> maintProjects = projectRepository.findByNameContainingIgnoreCase(maintName);
        Assert.assertTrue("Không có dự án bảo trì nào tồn tại trong DB", maintProjects.isEmpty());
    }

    /**
     * Kịch bản 3: Chứng minh kiến trúc Spring AOP Proxy.
     * Kiểm tra và chứng minh rằng bean ProjectService được Spring quản lý thông qua Dynamic Proxy.
     */
    @Test
    public void testAopProxyVerification() {
        // 1. Kiểm tra bằng công cụ kiểm định Spring AopUtils
        boolean isProxy = AopUtils.isAopProxy(projectService);
        Assert.assertTrue("ProjectService bean được inject phải là một Spring AOP Proxy", isProxy);

        // 2. Kiểm tra tên lớp thực thi tại runtime
        String className = projectService.getClass().getName();
        System.out.println(">>> [CHỨNG MINH PROXY] Lớp thực thi của ProjectService tại runtime: " + className);

        boolean isJdkProxy = AopUtils.isJdkDynamicProxy(projectService);
        boolean isCglibProxy = AopUtils.isCglibProxy(projectService);

        Assert.assertTrue("Phải là JDK Dynamic Proxy hoặc CGLIB Proxy", isJdkProxy || isCglibProxy);
        Assert.assertTrue("Tên lớp phải chứa dấu hiệu Proxy ($Proxy hoặc CGLIB)",
                className.contains("$Proxy") || className.contains("EnhancerBySpringCGLIB"));
    }
}
