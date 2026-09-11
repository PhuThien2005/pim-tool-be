# CẨM NANG HƯỚNG DẪN TỰ VIẾT TAY TỪNG BƯỚC CHO BÀI TẬP JAVA-06 (HIBERNATE & JPA)

Tài liệu này được thiết kế theo dạng **"Cầm tay chỉ việc" (Step-by-Step Tutorial)** giúp bạn tự gõ từng dòng code trên nhánh `main` (hoặc `Step1`), hiểu sâu bản chất kiến trúc và vượt qua các tiêu chí chấm điểm của ELCA Coach.

---

## MỤC LỤC
1. [Bước 1: Tạo Enum `ProjectStatus`](#bước-1-tạo-enum-projectstatus)
2. [Bước 2: Tạo Entity `Group` (Ánh xạ bảng `PROJECT_GROUP`)](#bước-2-tạo-entity-group-ánh-xạ-bảng-project_group)
3. [Bước 3: Nâng cấp Entity `Project` (Thêm Group, Project Leader, Members)](#bước-3-nâng-cấp-entity-project-thêm-group-project-leader-members)
4. [Bước 4: Nâng cấp Entity `User` (Điều hướng 2 chiều)](#bước-4-nâng-cấp-entity-user-điều-hướng-2-chiều)
5. [Bước 5: Tạo `GroupRepository` trong package `vn.elca.training.dao`](#bước-5-tạo-grouprepository-trong-package-vnelcatrainingdao)
6. [Bước 6: Cấu hình quét package trong `ApplicationWebConfig`](#bước-6-cấu-hình-quét-package-trong-applicationwebconfig)
7. [Bước 7: Viết JUnit Test `GroupRepositoryTest` & Chạy thử](#bước-7-viết-junit-test-grouprepositorytest--chạy-thử)
8. [Bước 8: Nâng cấp `ProjectRepositoryTest` với 5 bài test (Đồ thị đối tượng chuẩn)](#bước-8-nâng-cấp-projectrepositorytest-với-5-bài-test-đồ-thị-đối-tượng-chuẩn)
9. [Bước 9: Thêm tính năng Transactional trong `ProjectService`](#bước-9-thêm-tính-năng-transactional-trong-projectservice)
10. [Bước 10: Viết kiểm thử `ProjectServiceTransactionTest` chứng minh Truly Transactional](#bước-10-viết-kiểm-thử-projectservicetransactiontest-chứng-minh-truly-transactional)
11. [Bước 11: Kiểm tra H2 Console & Chạy kiểm thử toàn bộ dự án](#bước-11-kiểm-tra-h2-console--chạy-kiểm-thử-toàn-bộ-dự-án)

---

## Bước 1: Tạo Enum `ProjectStatus`

Trong tài liệu `JAVA-06.doc` (đoạn 188–189) yêu cầu truy vấn Project theo **name và status**. Dự án ban đầu chưa có thuộc tính `status`.

### File cần tạo:
`src/main/java/vn/elca/training/model/entity/ProjectStatus.java`

### Mã nguồn:
```java
package vn.elca.training.model.entity;

public enum ProjectStatus {
    NEW,
    PLA,
    INP,
    FIN,
    CLOSED
}
```

---

## Bước 2: Tạo Entity `Group` (Ánh xạ bảng `PROJECT_GROUP`)

### File cần tạo:
`src/main/java/vn/elca/training/model/entity/Group.java`

### Lưu ý quan trọng về kiến trúc:
* **Tên bảng:** Phải dùng `@Table(name = "PROJECT_GROUP")`. Trong SQL tiêu chuẩn (và H2), `GROUP` là từ khóa dành riêng của `GROUP BY`. Nếu để tên bảng là `GROUP`, câu lệnh DDL `CREATE TABLE GROUP ...` sẽ báo lỗi cú pháp.
* **Quan hệ với Leader:** Mỗi Group có 1 Group Leader là User $\rightarrow$ dùng `@ManyToOne(fetch = FetchType.LAZY)` và `@JoinColumn(name = "group_leader_id")`.
* **Quan hệ với Projects:** Một Group chứa nhiều Project $\rightarrow$ dùng `@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)`.

### Mã nguồn:
```java
package vn.elca.training.model.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROJECT_GROUP")
public class Group implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_leader_id")
    private User groupLeader;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

    public Group() {
    }

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, User groupLeader) {
        this.name = name;
        this.groupLeader = groupLeader;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getGroupLeader() {
        return groupLeader;
    }

    public void setGroupLeader(User groupLeader) {
        this.groupLeader = groupLeader;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }
}
```

---

## Bước 3: Nâng cấp Entity `Project` (Thêm Group, Project Leader, Members)

### File cần sửa:
`src/main/java/vn/elca/training/model/entity/Project.java`

### Các thuộc tính cần bổ sung:
1. `status` (`ProjectStatus`): Lưu dạng chuỗi qua `@Enumerated(EnumType.STRING)`.
2. `activated` (`Boolean`, mặc định `true`): Phục vụ cho yêu cầu tạo Maintenance Project.
3. `group` (`@ManyToOne` với `Group` qua cột `group_id`).
4. `projectLeader` (`@ManyToOne` với `User` qua cột `project_leader_id`).
5. `members` (`@ManyToMany` với `User` qua bảng trung gian `PROJECT_MEMBER`).

### Đoạn code cần thêm vào `Project.java`:
```java
    @Enumerated(EnumType.STRING)
    @Column
    private ProjectStatus status;

    @Column(columnDefinition = "boolean default true")
    private Boolean activated = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_leader_id")
    private User projectLeader;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "PROJECT_MEMBER",
            joinColumns = @JoinColumn(name = "project_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();
```
*(Đừng quên thêm các Constructor và Getters / Setters tương ứng cho các trường này).*

---

## Bước 4: Nâng cấp Entity `User` (Điều hướng 2 chiều)

### File cần sửa:
`src/main/java/vn/elca/training/model/entity/User.java`

### Lưu ý quan trọng:
Để một User có thể tham gia nhiều vai trò (vừa làm Group Leader ở Group 1, vừa làm Member ở dự án thuộc Group 2), và để Object Graph có thể điều hướng từ phía `User`, ta thêm 3 quan hệ:
```java
    @OneToMany(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    private Set<Group> leadingGroups = new HashSet<>();

    @OneToMany(mappedBy = "projectLeader", fetch = FetchType.LAZY)
    private Set<Project> leadingProjects = new HashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();
```
Đồng thời thêm:
* `import java.util.Set;` và `import java.util.HashSet;` ở đầu file.
* Các getter/setter: `getLeadingGroups()`, `setLeadingGroups()`, `getLeadingProjects()`, `setLeadingProjects()`, `getProjects()`, `setProjects()`.

---

## Bước 5: Tạo `GroupRepository` trong package `vn.elca.training.dao`

Đề bài yêu cầu: *"Define a new repository for entity Group named GroupRepository in package vn.elca.training.dao"*.

### File cần tạo:
`src/main/java/vn/elca/training/dao/GroupRepository.java`

### Mã nguồn:
```java
package vn.elca.training.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Group;

import java.util.List;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long>, QuerydslPredicateExecutor<Group> {
    List<Group> findByName(String name);
}
```

---

## Bước 6: Cấu hình quét package trong `ApplicationWebConfig`

Do repository nằm ở package mới `vn.elca.training.dao` (khác với `vn.elca.training.repository`), ta cần báo cho Spring Data JPA quét cả hai package.

### File cần sửa:
`src/main/java/vn/elca/training/ApplicationWebConfig.java`

### Sửa annotation `@EnableJpaRepositories`:
```java
@EnableJpaRepositories(basePackages = {"vn.elca.training.repository", "vn.elca.training.dao"})
```

---

## Bước 7: Viết JUnit Test `GroupRepositoryTest` & Chạy thử

### File cần tạo:
`src/test/java/vn/elca/training/dao/GroupRepositoryTest.java`

### Mã nguồn:
```java
package vn.elca.training.dao;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.QGroup;
import vn.elca.training.model.entity.User;
import vn.elca.training.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@Transactional
public class GroupRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindGroup() {
        User leader = userRepository.save(new User("GROUP_LEADER_1", "Group Leader"));
        Group group = new Group("ELCA Training Group", leader);
        Group savedGroup = groupRepository.save(group);

        Assert.assertNotNull(savedGroup.getId());

        Optional<Group> found = groupRepository.findById(savedGroup.getId());
        Assert.assertTrue(found.isPresent());
        Assert.assertEquals("ELCA Training Group", found.get().getName());
        Assert.assertEquals("GROUP_LEADER_1", found.get().getGroupLeader().getUsername());
    }

    @Test
    public void testQueryGroupWithQueryDSL() {
        User leader = userRepository.save(new User("QMV_LEADER", "Group Leader"));
        groupRepository.save(new Group("Group QMV", leader));
        em.flush();

        QGroup qGroup = QGroup.group;
        List<Group> results = new JPAQuery<Group>(em)
                .from(qGroup)
                .where(qGroup.name.eq("Group QMV")
                        .and(qGroup.groupLeader.username.eq("QMV_LEADER")))
                .fetch();

        Assert.assertEquals(1, results.size());
        Assert.assertEquals("Group QMV", results.get(0).getName());
    }

    @Test
    public void testDeleteGroup() {
        Group group = groupRepository.save(new Group("Group To Delete"));
        Long id = group.getId();
        Assert.assertTrue(groupRepository.findById(id).isPresent());

        groupRepository.delete(group);
        em.flush();

        Assert.assertFalse(groupRepository.findById(id).isPresent());
    }
}
```

### Chạy lệnh kiểm thử:
```powershell
mvn test -Dtest=GroupRepositoryTest
```

---

## Bước 8: Nâng cấp `ProjectRepositoryTest` với 5 bài test (Đồ thị đối tượng chuẩn)

### File cần sửa:
`src/test/java/vn/elca/training/repository/ProjectRepositoryTest.java`

### Điểm mấu chốt để không bị trừ điểm:
Trong `testSaveMultipleProjectsTree()`:
* Dùng hàm helper `createOrGetUser`:
  ```java
  private User createOrGetUser(String username, String role) {
      User existing = userRepository.findUserByUsername(username);
      if (existing != null) {
          return existing;
      }
      User user = new User(username, username, role);
      return userRepository.save(user);
  }
  ```
* Nhân sự **`QMV`** chỉ tạo 1 lần: vừa làm Group Leader của `Group QMV`, vừa làm QA Member trong `KSTA MIGRATION`.
* Nhân sự **`HNH`** chỉ tạo 1 lần: vừa làm Group Leader của `Group HNH`, vừa làm QA Member trong `EFV`.
* Không tạo các User giả lập như `QMV_GL` hay `QMV_QA`!

### 5 Test Cases cần có:
1. `testSaveOneProject()`: Lưu 1 dự án và assert ID, các trường.
2. `testSaveMultipleProjectsTree()`: Lưu cấu trúc cây phân cấp (`pasted-image-9.png`), assert quan hệ 2 chiều.
3. `testDeleteProject()`: Xóa 1 dự án và kiểm tra không còn trong DB.
4. `testSimpleQueryDSLByNameAndStatus()`: Tìm bằng QueryDSL theo `name` và `status`.
5. `testComplexQueryDSLWithRelations()`: QueryDSL kết hợp `Project` với `Group` (name, status, customer, group name, group leader).

### Chạy lệnh kiểm thử:
```powershell
mvn test -Dtest=ProjectRepositoryTest
```

---

## Bước 9: Thêm tính năng Transactional trong `ProjectService`

Đề bài yêu cầu: *"create a maintenance project from an existing one and ensure that it is running inside one transaction: Name: <old project's name> + Maint. + <current year>. The old project will be inactive (activated = false) as soon as the maintenance one is created. If there is any problem (exception) the new project must not be persisted into DB and the development one must not be updated."*

### 1. Thêm phương thức vào `ProjectService.java`:
```java
Project createMaintenanceProject(Long oldProjectId, boolean simulateError);
```

### 2. Cài đặt trong `ProjectServiceImpl.java`:
```java
@Override
@Transactional(rollbackFor = Throwable.class)
public Project createMaintenanceProject(Long oldProjectId, boolean simulateError) {
    Project oldProject = projectRepository.findById(oldProjectId)
            .orElseThrow(() -> new IllegalArgumentException("Project not found with id: " + oldProjectId));

    oldProject.setActivated(false);
    projectRepository.save(oldProject);

    int currentYear = LocalDate.now().getYear();
    String newProjectName = String.format("%s Maint. %d", oldProject.getName(), currentYear);

    Project maintenanceProject = new Project(newProjectName, oldProject.getFinishingDate());
    maintenanceProject.setCustomer(oldProject.getCustomer());
    maintenanceProject.setGroup(oldProject.getGroup());
    maintenanceProject.setStatus(ProjectStatus.NEW);
    maintenanceProject.setActivated(true);

    if (simulateError) {
        throw new ApplicationUnexpectedException("Lỗi giả lập để kích hoạt Rollback");
    }

    return projectRepository.save(maintenanceProject);
}
```

---

## Bước 10: Viết kiểm thử `ProjectServiceTransactionTest` chứng minh Truly Transactional

### File cần tạo:
`src/test/java/vn/elca/training/service/ProjectServiceTransactionTest.java`

### Gồm 3 bài test:
1. `testCreateMaintenanceProject_Success`: Chạy bình thường, project cũ `activated = false`, project mới được tạo.
2. `testCreateMaintenanceProject_RollbackWhenException`: Giả lập lỗi `simulateError = true`. Khẳng định rằng exception xảy ra làm rollback toàn bộ: project cũ vẫn giữ `activated = true`, project mới không xuất hiện trong DB.
3. `testVerifySpringTransactionalAopProxy`: Dùng `AopUtils.isAopProxy(projectService)` khẳng định phương thức được bọc bởi Spring CGLIB Dynamic Proxy.

### Chạy lệnh kiểm thử:
```powershell
mvn test -Dtest=ProjectServiceTransactionTest
```

---

## Bước 11: Kiểm tra H2 Console & Chạy kiểm thử toàn bộ dự án

1. **Khởi động ứng dụng:**
   Chạy class `ApplicationLauncher.java`.
2. **Mở trình duyệt:**
   Truy cập `http://localhost:8080/h2console`
   * JDBC URL: `jdbc:h2:mem:onboardingexercise`
   * User Name: `sa`
   * Password: `(để trống)`
   * Bấm **Connect** $\rightarrow$ Bạn sẽ thấy bảng `PROJECT_GROUP`, `PROJECT`, `PROJECT_MEMBER`, `USER`, `TASK`.
3. **Chạy kiểm thử toàn bộ dự án:**
   ```powershell
   mvn test
   ```
   Kết quả phải đạt: **`BUILD SUCCESS`** với toàn bộ test cases màu xanh!
