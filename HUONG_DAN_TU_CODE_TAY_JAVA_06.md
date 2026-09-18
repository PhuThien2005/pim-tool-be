# CẨM NANG HƯỚNG DẪN TỰ CODE TAY TOÀN DIỆN BÀI TẬP HIBERNATE & JPA (JAVA-06)

Tài liệu này được thiết kế theo dạng **"Cầm tay chỉ việc" (Step-by-Step Tutorial)** từ bài 1 đến bài cuối cùng trong `HIBERNATE-17.doc`. Hướng dẫn giúp bạn tự gõ từng dòng code, hiểu rõ nguyên nhân gốc rễ (Root Cause) của từng lỗi, giải pháp kỹ thuật chuẩn xác theo JPA/Hibernate và các lệnh `mvn test` để kiểm chứng.

Toàn bộ code trong cẩm nang đã được tích hợp **Lombok** (`@Getter`, `@Setter`, `@Builder`, `@NoArgsConstructor`, `@AllArgsConstructor`) giúp code ngắn gọn, sạch đẹp, đúng chuẩn Clean Code và không dính anti-pattern của Hibernate.

---

## MỤC LỤC TỔNG QUAN

- [PHẦN 1: CẤU HÌNH DỰ ÁN & LOMBOK](#phần-1-cấu-hình-dự-án--lombok)
  - [Bước 0: Thêm Lombok vào `pom.xml`](#bước-0-thêm-lombok-vào-pomxml)
- [PHẦN 2: THIẾT KẾ ENTITY & OBJECT GRAPH (LOMBOK + BUILDER)](#phần-2-thiết-kế-entity--object-graph-lombok--builder)
  - [Bước 1: Tạo Enum `ProjectStatus`](#bước-1-tạo-enum-projectstatus)
  - [Bước 2: Tạo Entity `Group` (Bảng `PROJECT_GROUP`)](#bước-2-tạo-entity-group-bảng-project_group)
  - [Bước 3: Nâng cấp Entity `Project`](#bước-3-nâng-cấp-entity-project)
  - [Bước 4: Nâng cấp Entity `Employee`](#bước-4-nâng-cấp-entity-user)
  - [Bước 5: Cập nhật Entity `Task` & `TaskAudit`](#bước-5-cập-nhật-entity-task--taskaudit)
- [PHẦN 3: SPRING DATA JPA & QUERYDSL REPOSITORIES](#phần-3-spring-data-jpa--querydsl-repositories)
  - [Bước 6: Tạo `GroupRepository` trong package `vn.elca.training.dao`](#bước-6-tạo-grouprepository-trong-package-vnelcatrainingdao)
  - [Bước 7: Cấu hình quét package trong `ApplicationWebConfig`](#bước-7-cấu-hình-quét-package-trong-applicationwebconfig)
  - [Bước 8: Viết JUnit Test `GroupRepositoryTest`](#bước-8-viết-junit-test-grouprepositorytest)
  - [Bước 9: Nâng cấp `ProjectRepositoryTest` với 5 bài test chuẩn](#bước-9-nâng-cấp-projectrepositorytest-với-5-bài-test-chuẩn)
- [PHẦN 4: QUẢN TRỊ TRANSACTION & TÍNH NGUYÊN TỬ](#phần-4-quản-trị-transaction--tính-nguyên-tử)
  - [Bước 10: Cài đặt tính năng Maintenance Project trong `ProjectService`](#bước-10-cài-đặt-tính-năng-maintenance-project-trong-projectservice)
  - [Bước 11: Viết kiểm thử `ProjectServiceTransactionTest` chứng minh Truly Transactional](#bước-11-viết-kiểm-thử-projectservicetransactiontest-chứng-minh-truly-transactional)
  - [Bước 12: Xác minh CSDL H2 Console](#bước-12-xác-minh-csdl-h2-console)
- [PHẦN 5: TỐI ƯU HIỆU NĂNG TRUY VẤN JPA & SESSION](#phần-5-tối-ưu-hiệu-năng-truy-vấn-jpa--session)
  - [Bước 13: Khắc phục `LazyInitializationException` (JOIN FETCH)](#bước-13-khắc-phục-lazyinitializationexception-join-fetch)
  - [Bước 14: Khắc phục vấn đề SELECT N+1 thứ nhất (Fetch Join)](#bước-14-khắc-phục-vấn-đề-select-n1-thứ-nhất-fetch-join)
  - [Bước 15: Khắc phục vấn đề SELECT N+1 thứ hai (Batch Query)](#bước-15-khắc-phục-vấn-đề-select-n1-thứ-hai-batch-query)
- [PHẦN 6: TRANSACTION NÂNG CAO & TOÀN VẸN DỮ LIỆU](#phần-6-transaction-nâng-cao--toàn-vẹn-dữ-liệu)
  - [Bước 16: Khắc phục vi phạm Single-Unit-of-Work (`rollbackFor`)](#bước-16-khắc-phục-vi-phạm-single-unit-of-work-rollbackfor)
  - [Bước 17: Lưu Audit Log độc lập (`Propagation.REQUIRES_NEW`)](#bước-17-lưu-audit-log-độc-lập-propagationrequires_new)
- [PHẦN 7: REST API & ĐỒNG BỘ QUAN HỆ 2 CHIỀU](#phần-7-rest-api--đồng-bộ-quan-hệ-2-chiều)
  - [Bước 18: Sửa lỗi không lưu Foreign Key `user_id` khi thêm Task](#bước-18-sửa-lỗi-không-lưu-foreign-key-user_id-khi-thêm-task)
  - [Bước 19: Khắc phục vòng lặp đệ quy vô tận JSON (Jackson Infinite Recursion)](#bước-19-khắc-phục-vòng-lặp-đệ-quy-vô-tận-json-jackson-infinite-recursion)
- [PHẦN 8: KIỂM THỬ TOÀN DỰ ÁN & ĐÓNG GÓI WAR](#phần-8-kiểm-thử-toàn-dự-án--đóng-gói-war)

---

# PHẦN 1: CẤU HÌNH DỰ ÁN & LOMBOK

## Bước 0: Thêm Lombok vào `pom.xml`

### File cần sửa:
`pom.xml`

### Thao tác:
Thêm dependency Lombok vào phần `<dependencies>`:
```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <scope>provided</scope>
</dependency>
```
> [!NOTE]
> Plugin `apt-maven-plugin` của QueryDSL trong `pom.xml` sẽ tự động phối hợp mượt mà với Lombok mà không bị xung đột khi compile.

---

# PHẦN 2: THIẾT KẾ ENTITY & OBJECT GRAPH (LOMBOK + BUILDER)

## Bước 1: Tạo Enum `ProjectStatus`

Trong `HIBERNATE-17.doc` yêu cầu viết query QueryDSL tìm theo `name and status`. Entity `Project` ban đầu chưa có thuộc tính này.

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

## Bước 2: Tạo Entity `Group` (Bảng `PROJECT_GROUP`)

### File cần tạo:
`src/main/java/vn/elca/training/model/entity/Group.java`

### Quy tắc kiến trúc:
1. **Tên bảng:** Bắt buộc dùng `@Table(name = "PROJECT_GROUP")`. Trong SQL và H2, từ khóa `GROUP` là từ khóa dành riêng (`GROUP BY`). Nếu đặt tên bảng là `GROUP`, lệnh `CREATE TABLE` sẽ gặp lỗi cú pháp.
2. **Quan hệ với Leader:** `@ManyToOne(fetch = FetchType.LAZY)` kết hợp `@JoinColumn(name = "group_leader_id")`.
3. **Quan hệ với Projects:** `@OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)`.

### Mã nguồn:

```java
package vn.elca.training.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROJECT_GROUP")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_leader_id")
  private Employee groupLeader;

  @Builder.Default
  @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private Set<Project> projects = new HashSet<>();

  public Group(String name) {
    this.name = name;
  }

  public Group(String name, Employee groupLeader) {
    this.name = name;
    this.groupLeader = groupLeader;
  }
}
```

---

## Bước 3: Nâng cấp Entity `Project`

### File cần sửa:
`src/main/java/vn/elca/training/model/entity/Project.java`

### Quy tắc kiến trúc:
1. Thêm `status` dạng Enum: `@Enumerated(EnumType.STRING)`.
2. Thêm `activated` kiểu `Boolean`: `@Column(columnDefinition = "boolean default true")`.
3. Thêm quan hệ Group: `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "group_id")`.
4. Thêm quan hệ Project Leader: `@ManyToOne(fetch = FetchType.LAZY)` + `@JoinColumn(name = "project_leader_id")`.
5. Thêm quan hệ Members (N-N): `@ManyToMany` với bảng trung gian `PROJECT_MEMBER`.

### Mã nguồn:

```java
package vn.elca.training.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Project {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column
  private LocalDate finishingDate;

  @Column
  private String customer;

  @Enumerated(EnumType.STRING)
  @Column
  private ProjectStatus status;

  @Builder.Default
  @Column(columnDefinition = "boolean default true")
  private Boolean activated = true;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "group_id")
  private Group group;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "project_leader_id")
  private Employee projectLeader;

  @Builder.Default
  @ManyToMany(fetch = FetchType.LAZY)
  @JoinTable(
          name = "PROJECT_MEMBER",
          joinColumns = @JoinColumn(name = "project_id"),
          inverseJoinColumns = @JoinColumn(name = "user_id")
  )
  private Set<Employee> members = new HashSet<>();

  @Builder.Default
  @OneToMany(mappedBy = "project", fetch = FetchType.LAZY)
  private Set<Task> tasks = new HashSet<>();

  public Project(String name, LocalDate finishingDate) {
    this.name = name;
    this.finishingDate = finishingDate;
  }

  public Project(Long id, String name, LocalDate finishingDate) {
    this.id = id;
    this.name = name;
    this.finishingDate = finishingDate;
  }

  public Project(String name, LocalDate finishingDate, String customer, ProjectStatus status) {
    this.name = name;
    this.finishingDate = finishingDate;
    this.customer = customer;
    this.status = status;
  }

  public Project(String name, LocalDate finishingDate, String customer, ProjectStatus status, Group group) {
    this.name = name;
    this.finishingDate = finishingDate;
    this.customer = customer;
    this.status = status;
    this.group = group;
  }

  public Boolean isActivated() {
    return activated;
  }
}
```

---

## Bước 4: Nâng cấp Entity `Employee`

### File cần sửa:
`src/main/java/vn/elca/training/model/entity/User.java`

### Thao tác:
* Thêm quan hệ đảo chiều `leadingGroups`, `leadingProjects`, `projects`.
* Thêm `@JsonIgnore` trên các collection này để chống lỗi đệ quy JSON vô tận.
* Giữ nguyên getter/setter đặc biệt `@Access(AccessType.PROPERTY)` của `usernameLength`.

### Mã nguồn:
```java
package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String fullName;

    @Column
    private String role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @Builder.Default
    @OneToMany(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Group> leadingGroups = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "projectLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Project> leadingProjects = new HashSet<>();

    @Builder.Default
    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    public User(String username) {
        this.username = username;
    }

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public User(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    @Access(AccessType.PROPERTY)
    @Column(nullable = true)
    public Long getUsernameLength() {
        return getUsername() != null ? (long) getUsername().length() : 0L;
    }

    public void setUsernameLength(Long length) {

    }
}
```

---

## Bước 5: Cập nhật Entity `Task` & `TaskAudit`

Rút gọn boilerplate code cho `Task.java` và `TaskAudit.java` bằng Lombok.

### File: `src/main/java/vn/elca/training/model/entity/Task.java`

```java
package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Task implements Serializable {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column
  private LocalDate deadline;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  @JsonIgnore
  private Project project;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn
  @JsonIgnore
  private Employee user;

  public Task(Project project, String name) {
    this.project = project;
    this.name = name;
    this.deadline = LocalDate.now();
  }
}
```

---

# PHẦN 3: SPRING DATA JPA & QUERYDSL REPOSITORIES

## Bước 6: Tạo `GroupRepository` trong package `vn.elca.training.dao`

Đề bài yêu cầu: *"Define a new repository for entity Group named IGroupRepository in package vn.elca.training.dao"*.

### File cần tạo:
`src/main/java/vn/elca/training/dao/GroupRepository.java`

### Mã nguồn:
```java
package vn.elca.training.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import vn.elca.training.model.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long>, QuerydslPredicateExecutor<Group> {
}
```

---

## Bước 7: Cấu hình quét package trong `ApplicationWebConfig`

### File cần sửa:
`src/main/java/vn/elca/training/ApplicationWebConfig.java`

### Thao tác:
Khai báo quét cả 2 package: `vn.elca.training.repository` và `vn.elca.training.dao`:
```java
@EnableJpaRepositories(basePackages = {
        "vn.elca.training.repository",
        "vn.elca.training.dao"
})
```

---

## Bước 8: Viết JUnit Test `GroupRepositoryTest`

### File cần tạo:
`src/test/java/vn/elca/training/dao/GroupRepositoryTest.java`

### Thao tác kiểm thử:
1. Lưu và tìm lại Group: `testSaveAndFindGroup`.
2. Truy vấn Group bằng QueryDSL: `testQueryDSLForGroup`.
3. Xóa Group: `testDeleteGroup`.

### Mã nguồn:

```java
package vn.elca.training.dao;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.QGroup;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@ActiveProfiles("dev")
@Transactional
public class GroupRepositoryTest {

  @Autowired
  private GroupRepository groupRepository;

  @Autowired
  private UserRepository userRepository;

  @PersistenceContext
  private EntityManager em;

  @Test
  public void testSaveAndFindGroup() {
    Employee leader = userRepository.save(new Employee("QMV_LEADER", "QMV Leader", "Group Leader"));
    Group group = groupRepository.save(new Group("Group QMV", leader));

    Assert.assertNotNull("ID của Group phải được sinh tự động", group.getId());

    Optional<Group> foundGroupOpt = groupRepository.findById(group.getId());
    Assert.assertTrue("Phải tìm thấy Group vừa lưu", foundGroupOpt.isPresent());
    Assert.assertEquals("Group QMV", foundGroupOpt.get().getName());
    Assert.assertEquals("QMV_LEADER", foundGroupOpt.get().getGroupLeader().getUsername());
  }

  @Test
  public void testQueryDSLForGroup() {
    Employee leader = userRepository.save(new Employee("HNH_LEADER", "HNH Leader", "Group Leader"));
    groupRepository.save(new Group("Group HNH", leader));

    Group queryResult = new JPAQuery<Group>(em)
            .from(QGroup.group)
            .where(QGroup.group.name.eq("Group HNH")
                    .and(QGroup.group.groupLeader.username.eq("HNH_LEADER")))
            .fetchOne();

    Assert.assertNotNull("QueryDSL phải tìm thấy Group HNH", queryResult);
    Assert.assertEquals("Group HNH", queryResult.getName());
  }

  @Test
  public void testDeleteGroup() {
    Employee leader = userRepository.save(new Employee("DEL_LEADER", "DEL Leader", "Group Leader"));
    Group savedGroup = groupRepository.save(new Group("Group To Delete", leader));
    Long groupId = savedGroup.getId();

    groupRepository.delete(savedGroup);
    em.flush();

    Optional<Group> deletedOpt = groupRepository.findById(groupId);
    Assert.assertFalse("Group phải không còn tồn tại sau khi xóa", deletedOpt.isPresent());
  }
}
```

### Chạy kiểm thử:
```powershell
mvn test -Dtest=GroupRepositoryTest
```

---

## Bước 9: Nâng cấp `ProjectRepositoryTest` với 5 bài test chuẩn

### File cần sửa:
`src/test/java/vn/elca/training/repository/ProjectRepositoryTest.java`

### 5 Bài test theo yêu cầu của `HIBERNATE-17.doc`:
1. `testSaveOneProject`: Lưu 1 dự án thành công.
2. `testSaveMultipleProjectsTree`: Lưu sơ đồ cây 2 nhóm dự án (QMV và HNH).
   * **Đặc biệt lưu ý:** QMV là Group Leader của Group 1, nhưng là QA Member ở Group 2. HNH là Member ở Group 1 nhưng là Group Leader của Group 2. Phải dùng hàm helper `createOrGetUser` để đảm bảo cùng 1 User entity, không tạo trùng username!
3. `testDeleteProject`: Xóa dự án thành công.
4. `testSimpleQueryDSLByNameAndStatus`: QueryDSL lọc theo `name` và `status`.
5. `testComplexQueryDSLByNameStatusGroupCustomer`: QueryDSL lọc theo quan hệ `name`, `status`, `group.name`, `customer`.

### Chạy kiểm thử:
```powershell
mvn test -Dtest=ProjectRepositoryTest
```

---

# PHẦN 4: QUẢN TRỊ TRANSACTION & TÍNH NGUYÊN TỬ

## Bước 10: Cài đặt tính năng Maintenance Project trong `ProjectService`

Đề bài yêu cầu: *"create a maintenance project from an existing one and ensure that it is running inside one transaction: Name: <old project's name> + Maint. + <current year>. The old project will be inactive (activated = false) as soon as the maintenance one is created. If there is any problem (exception) the new project must not be persisted into DB and the development one must not be updated."*

### 1. Khai báo trong `ProjectService.java`:
```java
Project createMaintenanceProject(Long oldProjectId);
```

### 2. Cài đặt trong `ProjectServiceImpl.java` (Sử dụng Builder Pattern):
```java
@Override
@Transactional(rollbackFor = Exception.class)
public Project createMaintenanceProject(Long oldProjectId) {
    Project oldProject = projectRepository.findById(oldProjectId)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án với ID: " + oldProjectId));

    // 1. Chuyển dự án cũ sang không kích hoạt
    oldProject.setActivated(false);
    projectRepository.save(oldProject);

    // 2. Tạo tên dự án mới: <tên cũ> + " Maint. " + <năm hiện tại>
    int currentYear = LocalDate.now().getYear();
    String maintenanceProjectName = String.format("%s Maint. %d", oldProject.getName(), currentYear);

    // 3. Khởi tạo đối tượng bảo trì mới bằng Builder Pattern gọn gàng
    Project maintenanceProject = Project.builder()
            .name(maintenanceProjectName)
            .finishingDate(LocalDate.now().plusYears(1))
            .customer(oldProject.getCustomer())
            .group(oldProject.getGroup())
            .status(ProjectStatus.NEW)
            .activated(true)
            .build();

    // (Khi muốn kiểm thử rollback thực nghiệm: bạn có thể chèn dòng throw new RuntimeException("Test rollback"); tại đây)

    // 4. Lưu vào database
    return projectRepository.save(maintenanceProject);
}
```

---

## Bước 11: Viết kiểm thử `ProjectServiceTransactionTest` chứng minh Truly Transactional

### File cần tạo:
`src/test/java/vn/elca/training/service/ProjectServiceTransactionTest.java`

### Gồm 3 bài test:
1. `testCreateMaintenanceProjectSuccess`: Chạy bình thường, project cũ `activated = false`, project mới được tạo.
2. `testCreateMaintenanceProjectRollbackOnException`: Khi bạn chèn lệnh ném lỗi (`throw Exception`) vào `createMaintenanceProject`, test này khẳng định rollback sạch sẽ: project cũ vẫn giữ `activated = true`, project mới không tồn tại trong DB.
3. `testAopProxyVerification`: Dùng `AopUtils.isAopProxy(projectService)` khẳng định phương thức được bọc bởi Spring Dynamic Proxy.

### Chạy kiểm thử:
```powershell
mvn test -Dtest=ProjectServiceTransactionTest
```

---

## Bước 12: Xác minh CSDL H2 Console

1. Khởi động ứng dụng bằng cách chạy `ApplicationLauncher.java`.
2. Mở trình duyệt truy cập: `http://localhost:8080/h2console`
   * JDBC URL: `jdbc:h2:mem:onboardingexercise`
   * User Name: `sa`
   * Password: `(để trống)`
3. Bấm **Connect** $\rightarrow$ Bạn sẽ thấy cấu trúc các bảng: `PROJECT_GROUP`, `PROJECT`, `PROJECT_MEMBER`, `USER`, `TASK`.

---

# PHẦN 5: TỐI ƯU HIỆU NĂNG TRUY VẤN JPA & SESSION

## Bước 13: Khắc phục `LazyInitializationException` (JOIN FETCH)

Đề bài yêu cầu: *"Disable annotation @Ignore in TaskServiceTest class. Please solve the failed test case 'testListNumberOfTasks' in TaskServiceTest. Scenario: queries all projects having tasks named 'Task 1', shows number of tasks in each. Changing field 'tasks' in Project entity is not allowed."*

## Bước 13: Đổi tên `RenameThisClass` và Khắc phục `LazyInitializationException`

### 1. Đổi tên file theo chuẩn Spring Data JPA:
* Đổi tên file: `src/main/java/vn/elca/training/repository/custom/RenameThisClass.java`  
  $\rightarrow$ `src/main/java/vn/elca/training/repository/custom/TaskRepositoryImpl.java`.
* Đổi tên class: `public class TaskRepositoryImpl implements TaskRepositoryCustom`.
* Thêm annotation `@Repository` lên đầu class.
> **Giải thích:** Spring Data JPA có quy ước (Convention): Để thêm logic tùy biến vào `TaskRepository`, lớp triển khai bắt buộc phải có tên kết thúc bằng `Impl` (tức `TaskRepositoryImpl`) thì Spring mới tự động ghép (stitch) vào repository bean.

### 2. Bản chất lỗi `LazyInitializationException`:
`Project.tasks` được cấu hình `FetchType.LAZY`. Khi transaction của service kết thúc, `EntityManager` đóng session. Khi `TaskServiceTest#testListNumberOfTasks` gọi `project.getTasks().size()`, proxy không thể nạp dữ liệu do đã mất Session.

### 3. Sửa hàm `findProjectsByTaskName`:
Mở file `src/main/java/vn/elca/training/repository/custom/TaskRepositoryImpl.java`:
```java
@Override
public List<Project> findProjectsByTaskName(String taskName) {
    // Cách 1: Thêm .fetchJoin() trực tiếp (Cách nhanh gọn trong tài liệu đề bài)
    return new JPAQuery<Project>(em)
            .from(QProject.project)
            .innerJoin(QProject.project.tasks, QTask.task).fetchJoin()
            .where(QTask.task.name.eq(taskName))
            .distinct()
            .fetch();
}
```
*(Hoặc dùng Subquery/2 bước để tránh lọc mất các task khác của project như đã phân tích).*

### Chạy kiểm thử:
```powershell
mvn test -Dtest=TaskServiceTest#testListNumberOfTasks
```

---

## Bước 14: Khắc phục vấn đề SELECT N+1 thứ nhất (`listRecentTasks`)

Đề bài yêu cầu: *"In TaskServiceTest, see test case 'testShowProjectNameOfTopTenNewTasks'. You see lines in red (unnecessary queries that slow down the function). Do you have any suggestion to solve this issue? Any change in TaskServiceTest is not allowed."*

### 1. File cần sửa:
`src/main/java/vn/elca/training/repository/custom/TaskRepositoryImpl.java`

### 2. Bản chất lỗi SELECT N+1:
Trong `TaskServiceImpl.java`, phương thức `listProjectNameOfRecentTasks()` gọi:
```java
List<Task> tasks = taskRepository.listRecentTasks(FETCH_LIMIT);
for (Task task : tasks) {
    projectNames.add(task.getProject().getName()); // <-- GÂY LỖI N+1 Ở ĐÂY!
}
```
Vì hàm gốc `listRecentTasks` chỉ query bảng `task`:
```sql
select * from task order by id desc limit 10;
```
Khi duyệt qua 10 task, mỗi lần gọi `task.getProject().getName()`, do `Task.project` chưa được fetch nên Hibernate lại phát sinh thêm 1 câu lệnh `select * from project where id = ?` cho từng task (tổng cộng 1 + 10 câu queries).

### 3. Mã nguồn sửa đổi (Thêm `.fetchJoin()` bằng QueryDSL):
Trong `TaskRepositoryImpl.java`, sửa hàm `listRecentTasks` bằng cách thêm `.innerJoin(QTask.task.project, QProject.project).fetchJoin()`:
```java
@Override
public List<Task> listRecentTasks(int limit) {
    return new JPAQuery<Task>(em)
            .from(QTask.task)
            .innerJoin(QTask.task.project, QProject.project).fetchJoin() // THÊM DÒNG NÀY ĐỂ FIX SELECT N+1
            .orderBy(QTask.task.id.desc())
            .limit(limit)
            .fetch();
}
```

### 4. Chạy kiểm thử:
```powershell
mvn test -Dtest=TaskServiceTest#testShowProjectNameOfTopTenNewTasks
```
Quan sát console log: Toàn bộ 10 câu `select project` bôi đỏ trước đây biến mất hoàn toàn! Chỉ còn lại **đúng 1 câu SQL JOIN DUY NHẤT**!

---

## Bước 15: Khắc phục vấn đề SELECT N+1 thứ hai (Batch Query)

Đề bài yêu cầu: *"Remove 'SELECT N + 1' issue. The same issue happens for 'testListTasksByIds' due to inefficient implementation in TaskServiceImpl."*

### Bản chất lỗi:
Trong `TaskServiceImpl`, phương thức `listTasksByIds(List<Long> ids)` duyệt vòng lặp `for (Long id : ids)` và gọi `taskRepository.findById(id)` $\rightarrow$ sinh ra $N$ câu truy vấn đơn lẻ.

### Giải pháp kỹ thuật:
Thay thế toàn bộ vòng for bằng phương thức **Batch Query**: `taskRepository.findAllById(ids)` (hoặc truy vấn SQL `WHERE id IN (...)`).

### File cần sửa:
`src/main/java/vn/elca/training/service/impl/TaskServiceImpl.java`

### Mã nguồn:
```java
@Override
public List<TaskDto> listTasksByIds(List<Long> ids) {
    if (ids == null || ids.isEmpty()) {
        return Collections.emptyList();
    }
    // Dùng findAllById để lấy tất cả trong 1 câu truy vấn WHERE IN duy nhất
    return StreamSupport.stream(taskRepository.findAllById(ids).spliterator(), false)
            .map(task -> ApplicationMapper.toTaskDto(task))
            .collect(Collectors.toList());
}
```

---

# PHẦN 6: TRANSACTION NÂNG CAO & TOÀN VẸN DỮ LIỆU

## Bước 16: Khắc phục vi phạm Single-Unit-of-Work (`rollbackFor`)

Đề bài yêu cầu: *"Solve a failed test case 'TaskServiceTest.testUpdateDeadline'. Why the data of the task is not rollbacked even an exception is thrown? What do you suggest to do at service level with @Transactional annotation?"*

### Bản chất lỗi:
1. `DeadlineAfterFinishingDateException` kế thừa từ `java.lang.Exception` (đây là **Checked Exception**).
2. Theo cơ chế mặc định của Spring `@Transactional`, transaction **CHỈ ROLLBACK** khi gặp **Unchecked Exception** (`RuntimeException` và `Error`). Khi gặp Checked Exception, Spring mặc định coi là nghiệp vụ đã lường trước và vẫn tiến hành COMMIT!

### Giải pháp kỹ thuật:
Khai báo tường minh thuộc tính `rollbackFor = {DeadlineAfterFinishingDateException.class, Exception.class}` trên `@Transactional`.

### File cần sửa:
`src/main/java/vn/elca/training/service/impl/TaskServiceImpl.java`

### Mã nguồn:
```java
@Override
@Transactional(rollbackFor = {DeadlineAfterFinishingDateException.class, Exception.class})
public void updateDeadline(Long taskId, LocalDate newDeadline) throws DeadlineAfterFinishingDateException {
    Task task = taskRepository.findById(taskId)
            .orElseThrow(() -> new IllegalArgumentException("Task not found"));

    task.setDeadline(newDeadline);
    taskRepository.save(task);

    if (task.getProject() != null && task.getProject().getFinishingDate() != null
            && newDeadline.isAfter(task.getProject().getFinishingDate())) {
        throw new DeadlineAfterFinishingDateException("Hạn chót công việc không được sau ngày kết thúc dự án");
    }
}
```

### Chạy kiểm thử:
```powershell
mvn test -Dtest=TaskServiceTest#testUpdateDeadline
```

---

## Bước 17: Lưu Audit Log độc lập (`Propagation.REQUIRES_NEW`)

Đề bài yêu cầu: *"Enable annotation @TaskDeadlineValid in Task. When creating task for project, in case exception occurs, all data should be rollbacked except audit log of task for tracing purpose. Solve failed test case 'testCreateTaskForProject'."*

### Bản chất lỗi:
Nếu phương thức ghi Audit log chạy chung trong cùng transaction với nghiệp vụ tạo Task, khi nghiệp vụ tạo Task bị lỗi văng exception, toàn bộ transaction bị rollback $\rightarrow$ bản ghi Audit Log cũng bị rollback mất theo!

### Giải pháp kỹ thuật:
Cấu hình thuộc tính truyền dẫn Transaction: **`Propagation.REQUIRES_NEW`** trên phương thức lưu audit log. Khi được gọi, Spring sẽ tạm dừng (suspend) transaction hiện tại và mở một transaction vật lý mới độc lập. Dù transaction cha bị rollback, transaction của Audit log vẫn commit thành công.

### File cần sửa:
`src/main/java/vn/elca/training/service/impl/AuditServiceImpl.java`

### Mã nguồn:
```java
package vn.elca.training.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.entity.TaskAudit;
import vn.elca.training.repository.TaskAuditRepository;
import vn.elca.training.service.AuditService;

@Service
public class AuditServiceImpl implements AuditService {

    @Autowired
    private TaskAuditRepository taskAuditRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveAuditDataForTask(TaskAudit audit) {
        taskAuditRepository.save(audit);
    }
}
```

### Chạy kiểm thử:
Bỏ comment `@TaskDeadlineValid` trong `Task.java` và chạy:
```powershell
mvn test -Dtest=TaskServiceTest#testCreateTaskForProject
```

---

# PHẦN 7: REST API & ĐỒNG BỘ QUAN HỆ 2 CHIỀU

## Bước 18: Sửa lỗi không lưu Foreign Key `user_id` khi thêm Task

Đề bài yêu cầu: *"Send request users/{username}/addTasks. The second get request is not able to get the newly added list of tasks. Check DB: the foreign key user_id of task table isn't updated. Explain why and fix."*

### Bản chất lỗi:
Trong mối quan hệ hai chiều `Employee` (1) ⇋ `Task` (N):
* Phía `Employee` có `@OneToMany(mappedBy = "user")` $\rightarrow$ Đây là **Inverse Side** (không nắm giữ cột khóa ngoại dưới DB).
* Phía `Task` có `@ManyToOne` + `@JoinColumn(name = "user_id")` $\rightarrow$ Đây là **Owning Side** (bên trực tiếp cập nhật cột `user_id` trong DB).
* Nếu code chỉ gọi `user.getTasks().add(task)` mà quên gán `task.setUser(user)`, Hibernate sẽ bỏ qua không sinh câu lệnh `UPDATE TASK SET USER_ID = ?` $\rightarrow$ Cột `user_id` vẫn là `NULL`!

### Giải pháp kỹ thuật:
Đồng bộ cả hai đầu của quan hệ hai chiều:
1. Gán phía sở hữu: `task.setUser(user);`
2. Gán phía tập hợp: `user.getTasks().add(task);`
3. Lưu đối tượng `task` thông qua `taskRepository.save(task);`.

### File cần sửa:
`src/main/java/vn/elca/training/service/impl/UserServiceImpl.java`

### Mã nguồn:
```java
@Override
@Transactional
public void addTasksToUser(String username, List<Long> taskIds) {
    User user = userRepository.findByUsername(username);
    if (user == null) {
        throw new IllegalArgumentException("User không tồn tại: " + username);
    }

    List<Task> tasks = StreamSupport.stream(taskRepository.findAllById(taskIds).spliterator(), false)
            .collect(Collectors.toList());

    for (Task task : tasks) {
        // BẮT BUỘC: Thiết lập owning side để cập nhật khóa ngoại user_id xuống CSDL
        task.setUser(user);
        if (user.getTasks() != null) {
            user.getTasks().add(task);
        }
        taskRepository.save(task);
    }
}
```

### Chạy kiểm thử:
```powershell
mvn test -Dtest=UserServiceTest#testAddTasksToUser_SyncBidirectionalRelationship
```

---

## Bước 19: Khắc phục vòng lặp đệ quy vô tận JSON (Jackson Infinite Recursion)

Đề bài yêu cầu: *"Send request for users/id/{id}. Inspect the response. Explain why and suggest solutions."*

### Bản chất lỗi:
Khi gọi endpoint trả về trực tiếp Entity `Employee`:
1. Jackson serializer đọc `Employee` $\rightarrow$ thấy thuộc tính `tasks`.
2. Jackson đọc từng `Task` trong `tasks` $\rightarrow$ thấy thuộc tính `user`.
3. Jackson đọc lại `Employee` $\rightarrow$ lại thấy `tasks` $\rightarrow$ tiếp tục lặp vô tận.
4. Kết quả: Ném ngoại lệ **`JsonMappingException: Infinite recursion (StackOverflowError)`**.

### Giải pháp kỹ thuật:
Áp dụng **DTO Pattern (Data Transfer Object)** - Đây là Best Practice tại ELCA:
* Không bao giờ trả Entity JPA trực tiếp ra Controller.
* Chuyển đổi Entity sang `UserDto` và `TaskDto`, loại bỏ tham chiếu ngược.

Ngoài ra, nếu muốn an toàn ở mức Entity, gắn `@JsonIgnore` trên thuộc tính tham chiếu ngược trong `Task.java`:
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn
@JsonIgnore
private User user;
```

---

# PHẦN 8: KIỂM THỬ TOÀN DỰ ÁN & ĐÓNG GÓI WAR

Sau khi hoàn tất cả 19 bước trên, bạn thực thi các lệnh sau để đảm bảo 100% dự án đạt chuẩn:

### 1. Chạy toàn bộ 27 Unit Test:
```powershell
mvn clean test
```
**Kết quả mong đợi:**
```
[INFO] Results:
[INFO] Tests run: 27, Failures: 0, Errors: 0, Skipped: 1
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### 2. Đóng gói ứng dụng sang file WAR:
```powershell
mvn package -DskipTests
```
**Kết quả mong đợi:**
```
[INFO] Building war: C:\Users\dptn\IdeaProjects\pilot-project-back\target\onboarding-exercise-0.0.1-SNAPSHOT.war
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```
File WAR sẵn sàng để deploy lên Tomcat hoặc chạy trực tiếp qua `ApplicationLauncher.java`!
