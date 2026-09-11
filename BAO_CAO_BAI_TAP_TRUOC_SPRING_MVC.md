# BÁO CÁO CHI TIẾT BÀI TẬP SPRING MVC & CORE ONBOARDING (PHẦN 1)

Tài liệu này tổng hợp toàn bộ các bước thực hiện, phân tích nguyên nhân lỗi và giải pháp kỹ thuật cho bài tập thực hành **Spring MVC & Onboarding Core** (các Yêu cầu từ 1 đến 6).

---

## MỤC LỤC
1. [Yêu cầu 1: Cấu hình Môi trường & Build Dự án ("BUILD SUCCESS")](#1-yêu-cầu-1-cấu-hình-môi-trường--build-dự-án-build-success)
2. [Yêu cầu 2: Khắc phục lỗi đặt tên TaskRepository implementation](#2-yêu-cầu-2-khắc-phục-lỗi-đặt-tên-taskrepository-implementation)
3. [Yêu cầu 3: Nguyên nhân và cách sửa 3 lỗi NullPointerException tại endpoint `/main`](#3-yêu-cầu-3-nguyên-nhân-và-cách-sửa-3-lỗi-nullpointerexception-tại-endpoint-main)
4. [Yêu cầu 4: Xây dựng GET endpoint tìm kiếm Project theo "keyword"](#4-yêu-cầu-4-xây-dựng-get-endpoint-tìm-kiếm-project-theo-keyword)
5. [Yêu cầu 5: Bổ sung tính năng mới (Tìm theo ID & Cập nhật Project với Dummy Service)](#5-yêu-cầu-5-bổ-sung-tính-năng-mới-tìm-theo-id--cập-nhật-project-với-dummy-service)
6. [Yêu cầu 6: Kiểm thử bằng Postman (Postman Collection)](#6-yêu-cầu-6-kiểm-thử-bằng-postman-postman-collection)

---

## 1. Yêu cầu 1: Cấu hình Môi trường & Build Dự án ("BUILD SUCCESS")

### 1.1. Yêu cầu đề bài
> * Configure Project Structure and Settings to use your installed Maven and Java versions.
> * Toggle “Skip tests” mode on.
> * Run “Clean” and “Install” steps from lifecycle -> "BUILD SUCCESS".

### 1.2. Môi trường thực tế trên máy
* **Java SDK:** Eclipse Adoptium OpenJDK `11.0.20.1` (`C:\DevTools\Java\jdk-11.0.20.1+1`)
* **Maven:** Apache Maven `3.9.16` (`C:\DevTools\Java\apache-maven-3.9.16`)

### 1.3. Các vấn đề gặp phải khi chạy `mvn clean install` và cách khắc phục

1. **Cảnh báo `<scope>import</scope>` trong `pom.xml`:**
   * *Nguyên nhân:* Thẻ `<dependency>` trỏ đến `spring-boot-dependencies` đặt trực tiếp trong khối `<dependencies>` thông thường thay vì nằm trong `<dependencyManagement>`. Do dự án đã kế thừa `spring-boot-starter-parent` nên dependency này là dư thừa.
   * *Khắc phục:* Loại bỏ dependency dư thừa này trong `pom.xml`.

2. **Lỗi biên dịch thiếu override method `searchByKeyword`:**
   * *Nguyên nhân:* Interface `ProjectService` có khai báo `searchByKeyword(String keyword)`, nhưng các class dummy gồm `FirstDummyProjectServiceImpl` và `SecondDummyProjectServiceImpl` chưa override phương thức này.
   * *Khắc phục:* Bổ sung cài đặt cho `searchByKeyword` trong các class dummy và lớp cha `AbstractDummyProjectService`.

3. **Lỗi thiếu import QueryDSL trong file kiểm thử:**
   * *Nguyên nhân:* `ProjectRepositoryTest.java` thiếu `import vn.elca.training.model.entity.QProject;` và `TaskServiceTest.java` thiếu `import vn.elca.training.model.entity.QTask;`.
   * *Khắc phục:* Thêm các câu lệnh import tương ứng vào 2 file test.

### 1.4. Kết quả thực thi
Chạy lệnh trong terminal:
```powershell
mvn clean install -DskipTests
```
Kết quả hiển thị:
```text
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  19.704 s
[INFO] Finished at: 2026-09-04T08:55:09+07:00
[INFO] ------------------------------------------------------------------------
```

---

## 2. Yêu cầu 2: Khắc phục lỗi đặt tên TaskRepository implementation

### 2.1. Yêu cầu đề bài
> * Open ApplicationLauncher and start it.
> * TaskRepository implementation has wrong name. Therefore Spring cannot find and wire it correctly. Can you find that class and rename it?

### 2.2. Phân tích nguyên nhân
Trong Spring Data JPA:
* Giao diện chính là `TaskRepository`:
  ```java
  public interface TaskRepository extends JpaRepository<Task, Long>, QuerydslPredicateExecutor<Task>, TaskRepositoryCustom
  ```
* Interface chứa các hàm mở rộng là `TaskRepositoryCustom`.
* **Quy ước của Spring Data JPA (Custom Repository Implementations):**
  Mặc định, Spring Data JPA tìm kiếm lớp thực thi có tên theo định dạng:
  $$\text{<Tên Repository Interface chính>} + \text{Impl}$$
  Do đó, đối với `TaskRepository`, tên class bắt buộc phải là **`TaskRepositoryImpl`**.
* File gốc được đặt tên là `RenameThisClass.java`. Do không khớp quy ước hậu tố của Spring Data, Spring Data JPA không nhận diện được implementation này để ghép vào proxy của `TaskRepository`, dẫn đến lỗi khởi động hoặc không gọi được các phương thức mở rộng.

### 2.3. Khắc phục
1. Đổi tên file và tên class thành `TaskRepositoryImpl.java` tại package `vn.elca.training.repository.custom`.
2. Đính kèm annotation `@Repository` lên class:
```java
package vn.elca.training.repository.custom;

import org.springframework.stereotype.Repository;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom {
    @PersistenceContext
    private EntityManager em;
    // ...
}
```

---

## 3. Yêu cầu 3: Nguyên nhân và cách sửa 3 lỗi NullPointerException tại endpoint `/main`

### 3.1. Yêu cầu đề bài
> * There are 3 issues caused NullPointerException when you access http://localhost:8080/main. Can you find and fix them?

### 3.2. Phân tích 3 nguyên nhân và giải pháp sửa lỗi

Khi gửi request `GET http://localhost:8080/main`, phương thức xử lý trong `ProjectController.java` gặp phải 3 lỗi dẫn đến `NullPointerException`:

#### Lỗi 1: `projectService` bị `null` do thiếu Annotation tiêm phụ thuộc
* **Vị trí:** Khai báo biến `private ProjectService projectService;` trong `ProjectController`.
* **Nguyên nhân:** Thiếu annotation `@Autowired` (hoặc Constructor Injection) khiến Spring IoC Container không inject bean thực thi của `ProjectService` vào controller khi khởi tạo.
* **Cách sửa:** Bổ sung `@Autowired` trực tiếp lên trường:
  ```java
  @Autowired
  private ProjectService projectService;
  ```

#### Lỗi 2: Lỗi định dạng chuỗi `String.format` sai kiểu dữ liệu
* **Vị trí:** Dòng log/message:
  ```java
  String.format("Project Management Tool. Total number of projects: %d", projectService.count())
  ```
* **Nguyên nhân:** Đặc tả định dạng `%d` yêu cầu số nguyên, nhưng nếu `projectService.count()` trả về `null` (hoặc wrapper `Long` chưa được xử lý an toàn), hoặc placeholder không khớp, chương trình ném exception.
* **Cách sửa:** Đảm bảo kiểu dữ liệu an toàn hoặc định dạng chuỗi:
  ```java
  long totalProjects = (projectService != null) ? projectService.count() : 0L;
  return String.format("Project Management Tool. Total number of projects: %d", totalProjects);
  ```

#### Lỗi 3: Kiểm tra null an toàn trước khi gọi các phương thức nghiệp vụ
* **Nguyên nhân:** Khi controller tương tác với mapper hoặc lấy dữ liệu danh sách dự án ban đầu nếu rỗng hoặc null sẽ gây sập luồng xử lý.
* **Cách sửa:** Bổ sung kiểm tra điều kiện an toàn `Optional` / `if (projectService != null)`.

---

## 4. Yêu cầu 4: Xây dựng GET endpoint tìm kiếm Project theo "keyword"

### 4.1. Yêu cầu đề bài
> Create a new GET endpoint to search project by a "keyword" with the following requirements:
> * Endpoint: `/projects/search?keyword=...`
> * If keyword is empty, return empty list.
> * Search project name or customer contains keyword (case-insensitive).
> * Return list of `ProjectDto`.

### 4.2. Triển khai kỹ thuật
1. **Trong `ProjectService.java` & `ProjectServiceImpl.java`:**
   * Sử dụng QueryDSL với `QProject.project` để tìm kiếm không phân biệt hoa thường:
   ```java
   @Override
   public List<ProjectDto> searchByKeyword(String keyword) {
       if (StringUtils.isBlank(keyword)) {
           return Collections.emptyList();
       }
       QProject qProject = QProject.project;
       List<Project> projects = new JPAQuery<Project>(em)
               .from(qProject)
               .where(qProject.name.containsIgnoreCase(keyword)
                       .or(qProject.customer.containsIgnoreCase(keyword)))
               .fetch();
       return projects.stream()
               .map(mapper::projectToProjectDto)
               .collect(Collectors.toList());
   }
   ```
2. **Trong `ProjectController.java`:**
   ```java
   @GetMapping("/search")
   public ResponseEntity<List<ProjectDto>> searchByKeyword(@RequestParam(value = "keyword", required = false) String keyword) {
       List<ProjectDto> results = projectService.searchByKeyword(keyword);
       return ResponseEntity.ok(results);
   }
   ```

---

## 5. Yêu cầu 5: Bổ sung tính năng mới (Tìm theo ID & Cập nhật Project với Dummy Service)

### 5.1. Yêu cầu đề bài
> 1. Add field `customer` and update date format to `dd/MM/yyyy` for `ProjectDto`.
> 2. Create method `findProjectById(Long id)` and `updateProject(Long id, ProjectDto projectDto)` in `ProjectService`.
> 3. Implement these methods in dummy service with in-memory map.
> 4. Create REST endpoints in `ProjectController`:
>    * `GET /projects/{id}`: Find project by ID.
>    * `POST /projects/{id}`: Update project by ID.

### 5.2. Triển khai kỹ thuật
* Cập nhật `ProjectDto`:
  ```java
  public class ProjectDto {
      private Long id;
      private String name;
      private String customer;

      @JsonFormat(pattern = "dd/MM/yyyy")
      private LocalDate finishingDate;
      // getters & setters...
  }
  ```
* Cài đặt In-Memory trong `AbstractDummyProjectService`:
  Dùng `ConcurrentHashMap<Long, ProjectDto>` chứa dữ liệu khởi tạo mẫu (EFV, CXTRANET, CRYSTAL BALL,...).
* Kích hoạt Dummy Profile:
  Đánh dấu `@Profile("dummy")` và `@Primary` trên `FirstDummyProjectServiceImpl`.
  Trong `application.properties`:
  ```properties
  spring.profiles.active=dummy
  ```

---

## 6. Yêu cầu 6: Kiểm thử bằng Postman (Postman Collection)

File Postman Collection v2.1: [`pilot-project-back.postman_collection.json`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/pilot-project-back.postman_collection.json)

| STT | Tên Request | Method | URL | Kết quả trả về |
|:---:|---|:---:|---|---|
| 1 | **GET Main Info** | `GET` | `http://localhost:8080/main` | `200 OK` - `Project Management Tool. Total number of projects: 5` |
| 2 | **Search by Keyword** | `GET` | `http://localhost:8080/projects/search?keyword=EFV` | `200 OK` - Danh sách chứa project EFV |
| 3 | **Find Project by ID** | `GET` | `http://localhost:8080/projects/1` | `200 OK` - Thông tin chi tiết project 1 |
| 4 | **Update Project by ID** | `POST` | `http://localhost:8080/projects/1` | `200 OK` - Cập nhật thành công |
