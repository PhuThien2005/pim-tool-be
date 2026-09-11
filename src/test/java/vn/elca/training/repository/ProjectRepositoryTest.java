package vn.elca.training.repository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.dao.GroupRepository;
import vn.elca.training.model.entity.*;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(value = SpringRunner.class)
@Transactional
public class ProjectRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    private User createOrGetUser(String username, String role) {
        User user = new User(username, username, role);
        return userRepository.save(user);
    }

    @Test
    public void testCountAll() {
        projectRepository.save(new Project("KSTA", LocalDate.now()));
        projectRepository.save(new Project("LAGAPEO", LocalDate.now()));
        projectRepository.save(new Project("ZHQUEST", LocalDate.now()));
        projectRepository.save(new Project("SECUTIX", LocalDate.now()));
        Assert.assertEquals(9, projectRepository.count());
    }

    @Test
    public void testFindOneWithQueryDSL() {
        final String PROJECT_NAME = "KSTA";
        projectRepository.save(new Project(PROJECT_NAME, LocalDate.now()));
        Project project = new JPAQuery<Project>(em)
                .from(QProject.project)
                .where(QProject.project.name.eq(PROJECT_NAME))
                .fetchFirst();
        Assert.assertEquals(PROJECT_NAME, project.getName());
    }

    /**
     * Test 1: Xác minh việc lưu 1 dự án qua ProjectRepository
     */
    @Test
    public void testSaveOneProject() {
        Project project = new Project("PROJECT_SOLO", LocalDate.of(2026, 12, 31), "CUSTOMER_SOLO", ProjectStatus.NEW);
        project.setActivated(true);

        Project savedProject = projectRepository.save(project);
        Assert.assertNotNull("ID dự án phải được sinh tự động", savedProject.getId());

        Optional<Project> foundOpt = projectRepository.findById(savedProject.getId());
        Assert.assertTrue("Dự án phải tồn tại trong DB", foundOpt.isPresent());

        Project found = foundOpt.get();
        Assert.assertEquals("PROJECT_SOLO", found.getName());
        Assert.assertEquals("CUSTOMER_SOLO", found.getCustomer());
        Assert.assertEquals(ProjectStatus.NEW, found.getStatus());
        Assert.assertTrue(found.isActivated());
    }

    /**
     * Test 2: Xác minh việc lưu nhiều dự án theo đúng sơ đồ cây trong pasted-image-9.png:
     * - Group 1: Leader QMV
     *     + Project EFV (PL: HTV) -> Developers: TQP, NQN; QA: HNH
     *     + Project CXTRANET (PL: QKP) -> QA: PLH; Developer: HNL
     *     + Project CRYSTAL BALL (PL: MKN) -> QA: TBH; Developer: TDN
     * - Group 2: Leader HNH
     *     + Project IOC CLIENT EXTRANET (PL: APL) -> Developers: HPN, BNN, PNH; QA: HUN
     *     + Project KSTA MIGRATION (PL: XHP) -> QA: QMV; Developer: VVT
     */
    @Test
    public void testSaveMultipleProjectsTree() {
        // ==========================================
        // CÂY 1: Nhóm do QMV làm Group Leader
        // ==========================================
        User qmvGroupLeader = createOrGetUser("QMV_GL", "Group Leader");
        Group groupQmv = groupRepository.save(new Group("Group QMV", qmvGroupLeader));

        // 1. Dự án EFV (PL: HTV)
        User plHtv = createOrGetUser("HTV", "Project Leader");
        User tqp = createOrGetUser("TQP", "Developer");
        User hnhQa = createOrGetUser("HNH_QA", "Quality Agent");
        User nqn = createOrGetUser("NQN", "Developer");

        Project efv = new Project("EFV_TREE", LocalDate.of(2026, 6, 30), "ELCA", ProjectStatus.INP, groupQmv);
        efv.setProjectLeader(plHtv);
        efv.setMembers(new HashSet<>(Arrays.asList(tqp, hnhQa, nqn)));
        projectRepository.save(efv);

        // 2. Dự án CXTRANET (PL: QKP)
        User plQkp = createOrGetUser("QKP", "Project Leader");
        User plh = createOrGetUser("PLH", "Quality Agent");
        User hnl = createOrGetUser("HNL", "Developer");

        Project cxtranet = new Project("CXTRANET_TREE", LocalDate.of(2026, 7, 31), "ELCA", ProjectStatus.INP, groupQmv);
        cxtranet.setProjectLeader(plQkp);
        cxtranet.setMembers(new HashSet<>(Arrays.asList(plh, hnl)));
        projectRepository.save(cxtranet);

        // 3. Dự án CRYSTAL BALL (PL: MKN)
        User plMkn = createOrGetUser("MKN", "Project Leader");
        User tbh = createOrGetUser("TBH", "Quality Agent");
        User tdn = createOrGetUser("TDN", "Developer");

        Project crystalBall = new Project("CRYSTAL_BALL_TREE", LocalDate.of(2026, 8, 31), "ELCA", ProjectStatus.PLA, groupQmv);
        crystalBall.setProjectLeader(plMkn);
        crystalBall.setMembers(new HashSet<>(Arrays.asList(tbh, tdn)));
        projectRepository.save(crystalBall);

        // ==========================================
        // CÂY 2: Nhóm do HNH làm Group Leader
        // ==========================================
        User hnhGroupLeader = createOrGetUser("HNH_GL", "Group Leader");
        Group groupHnh = groupRepository.save(new Group("Group HNH", hnhGroupLeader));

        // 4. Dự án IOC CLIENT EXTRANET (PL: APL)
        User plApl = createOrGetUser("APL", "Project Leader");
        User hpn = createOrGetUser("HPN", "Developer");
        User hun = createOrGetUser("HUN", "Quality Agent");
        User bnn = createOrGetUser("BNN", "Developer");
        User pnh = createOrGetUser("PNH", "Developer");

        Project iocClient = new Project("IOC_CLIENT_EXTRANET_TREE", LocalDate.of(2026, 9, 30), "IOC", ProjectStatus.INP, groupHnh);
        iocClient.setProjectLeader(plApl);
        iocClient.setMembers(new HashSet<>(Arrays.asList(hpn, hun, bnn, pnh)));
        projectRepository.save(iocClient);

        // 5. Dự án KSTA MIGRATION (PL: XHP)
        User plXhp = createOrGetUser("XHP", "Project Leader");
        User qmvQa = createOrGetUser("QMV_QA", "Quality Agent");
        User vvt = createOrGetUser("VVT", "Developer");

        Project kstaMigration = new Project("KSTA_MIGRATION_TREE", LocalDate.of(2026, 10, 31), "KSTA", ProjectStatus.PLA, groupHnh);
        kstaMigration.setProjectLeader(plXhp);
        kstaMigration.setMembers(new HashSet<>(Arrays.asList(qmvQa, vvt)));
        projectRepository.save(kstaMigration);

        em.flush();
        em.clear();

        // ==========================================
        // KIỂM TRA TOÀN DIỆN DỮ LIỆU CÂY ĐÃ LƯU
        // ==========================================
        // Xác minh Group 1
        Group savedGroup1 = groupRepository.findById(groupQmv.getId()).orElse(null);
        Assert.assertNotNull(savedGroup1);
        Assert.assertEquals("QMV_GL", savedGroup1.getGroupLeader().getUsername());

        Project savedEfv = projectRepository.findByNameContainingIgnoreCase("EFV_TREE").get(0);
        Assert.assertEquals("HTV", savedEfv.getProjectLeader().getUsername());
        Assert.assertEquals(3, savedEfv.getMembers().size());
        Assert.assertEquals("Group QMV", savedEfv.getGroup().getName());

        Project savedCxtranet = projectRepository.findByNameContainingIgnoreCase("CXTRANET_TREE").get(0);
        Assert.assertEquals("QKP", savedCxtranet.getProjectLeader().getUsername());
        Assert.assertEquals(2, savedCxtranet.getMembers().size());

        Project savedCrystalBall = projectRepository.findByNameContainingIgnoreCase("CRYSTAL_BALL_TREE").get(0);
        Assert.assertEquals("MKN", savedCrystalBall.getProjectLeader().getUsername());
        Assert.assertEquals(2, savedCrystalBall.getMembers().size());

        // Xác minh Group 2
        Group savedGroup2 = groupRepository.findById(groupHnh.getId()).orElse(null);
        Assert.assertNotNull(savedGroup2);
        Assert.assertEquals("HNH_GL", savedGroup2.getGroupLeader().getUsername());

        Project savedIoc = projectRepository.findByNameContainingIgnoreCase("IOC_CLIENT_EXTRANET_TREE").get(0);
        Assert.assertEquals("APL", savedIoc.getProjectLeader().getUsername());
        Assert.assertEquals(4, savedIoc.getMembers().size());

        Project savedKsta = projectRepository.findByNameContainingIgnoreCase("KSTA_MIGRATION_TREE").get(0);
        Assert.assertEquals("XHP", savedKsta.getProjectLeader().getUsername());
        Assert.assertEquals(2, savedKsta.getMembers().size());
    }

    /**
     * Test 3: Xác minh việc xóa một dự án qua ProjectRepository
     */
    @Test
    public void testDeleteProject() {
        Project project = new Project("PROJECT_TO_DELETE", LocalDate.now(), "DELETE_CUSTOMER", ProjectStatus.NEW);
        Project saved = projectRepository.save(project);
        Long id = saved.getId();
        Assert.assertNotNull(id);
        Assert.assertTrue(projectRepository.findById(id).isPresent());

        // Xóa dự án
        projectRepository.delete(saved);
        em.flush();

        // Xác minh dự án đã bị xóa
        Assert.assertFalse("Dự án phải không còn trong cơ sở dữ liệu sau khi xóa", projectRepository.findById(id).isPresent());
    }

    /**
     * Test 4: Xác minh truy vấn đơn giản bằng QueryDSL theo các thuộc tính riêng của Project (name và status)
     */
    @Test
    public void testSimpleQueryDSLByNameAndStatus() {
        projectRepository.save(new Project("PROJECT_ALPHA", LocalDate.now(), "CUSTOMER_A", ProjectStatus.NEW));
        projectRepository.save(new Project("PROJECT_BETA", LocalDate.now(), "CUSTOMER_B", ProjectStatus.INP));
        projectRepository.save(new Project("PROJECT_GAMMA", LocalDate.now(), "CUSTOMER_C", ProjectStatus.FIN));

        QProject qProject = QProject.project;

        List<Project> results = new JPAQuery<Project>(em)
                .from(qProject)
                .where(qProject.name.eq("PROJECT_BETA")
                        .and(qProject.status.eq(ProjectStatus.INP)))
                .fetch();

        Assert.assertEquals("Chỉ được tìm thấy đúng 1 dự án thỏa mãn", 1, results.size());
        Project found = results.get(0);
        Assert.assertEquals("PROJECT_BETA", found.getName());
        Assert.assertEquals(ProjectStatus.INP, found.getStatus());
        Assert.assertEquals("CUSTOMER_B", found.getCustomer());
    }

    /**
     * Test 5: Xác minh truy vấn phức tạp bằng QueryDSL kết hợp thuộc tính của Project và thuộc tính quan hệ:
     * - Thuộc tính Project: name, status
     * - Thuộc tính quan hệ: group (tên group, group leader) và customer
     */
    @Test
    public void testComplexQueryDSLWithRelations() {
        User leader = createOrGetUser("COMPLEX_LEADER", "Group Leader");
        Group targetGroup = groupRepository.save(new Group("TARGET_GROUP", leader));
        Group otherGroup = groupRepository.save(new Group("OTHER_GROUP", leader));

        Project targetProject = new Project("COMPLEX_TARGET_PROJECT", LocalDate.now(), "ELCA_CUSTOMER", ProjectStatus.INP, targetGroup);
        Project noiseProject1 = new Project("COMPLEX_TARGET_PROJECT", LocalDate.now(), "OTHER_CUSTOMER", ProjectStatus.INP, targetGroup);
        Project noiseProject2 = new Project("COMPLEX_TARGET_PROJECT", LocalDate.now(), "ELCA_CUSTOMER", ProjectStatus.FIN, targetGroup);
        Project noiseProject3 = new Project("COMPLEX_TARGET_PROJECT", LocalDate.now(), "ELCA_CUSTOMER", ProjectStatus.INP, otherGroup);

        projectRepository.save(targetProject);
        projectRepository.save(noiseProject1);
        projectRepository.save(noiseProject2);
        projectRepository.save(noiseProject3);

        em.flush();

        QProject qProject = QProject.project;
        QGroup qGroup = QGroup.group;

        // Truy vấn phức tạp: JOIN Project với Group, lọc theo cả name, status, customer và group name
        List<Project> results = new JPAQuery<Project>(em)
                .from(qProject)
                .innerJoin(qProject.group, qGroup)
                .where(qProject.name.eq("COMPLEX_TARGET_PROJECT")
                        .and(qProject.status.eq(ProjectStatus.INP))
                        .and(qProject.customer.eq("ELCA_CUSTOMER"))
                        .and(qGroup.name.eq("TARGET_GROUP"))
                        .and(qGroup.groupLeader.username.eq("COMPLEX_LEADER")))
                .fetch();

        Assert.assertEquals("Chỉ duy nhất 1 dự án thỏa mãn tất cả tiêu chí riêng và quan hệ", 1, results.size());
        Project matched = results.get(0);
        Assert.assertEquals("COMPLEX_TARGET_PROJECT", matched.getName());
        Assert.assertEquals(ProjectStatus.INP, matched.getStatus());
        Assert.assertEquals("ELCA_CUSTOMER", matched.getCustomer());
        Assert.assertEquals("TARGET_GROUP", matched.getGroup().getName());
        Assert.assertEquals("COMPLEX_LEADER", matched.getGroup().getGroupLeader().getUsername());
    }
}
