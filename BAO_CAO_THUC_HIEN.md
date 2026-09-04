# BÁO CÁO CHI TIẾT THỰC HIỆN DỰ ÁN PILOT-PROJECT-BACK

Tài liệu này tổng hợp toàn bộ các bước thực hiện, phân tích kỹ thuật và giải pháp cho bài thực hành **`pilot-project-back`** theo đúng các yêu cầu từ đề bài.

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
   * *Khắc phục:* Loại bỏ dependency dư thừa này trong [`pom.xml`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/pom.xml).

2. **Lỗi biên dịch thiếu override method `searchByKeyword`:**
   * *Nguyên nhân:* Interface [`ProjectService`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/ProjectService.java) có khai báo `searchByKeyword(String keyword)`, nhưng các class dummy gồm [`FirstDummyProjectServiceImpl`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/dummy/FirstDummyProjectServiceImpl.java) và [`SecondDummyProjectServiceImpl`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/dummy/SecondDummyProjectServiceImpl.java) chưa override phương thức này.
   * *Khắc phục:* Bổ sung cài đặt cho `searchByKeyword` trong các class dummy và lớp cha [`AbstractDummyProjectService`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/dummy/AbstractDummyProjectService.java).

3. **Lỗi thiếu import QueryDSL trong file kiểm thử:**
   * *Nguyên nhân:* [`ProjectRepositoryTest.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/repository/ProjectRepositoryTest.java) thiếu `import vn.elca.training.model.entity.QProject;` và [`TaskServiceTest.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/service/TaskServiceTest.java) thiếu `import vn.elca.training.model.entity.QTask;`.
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
* Giao diện chính là [`TaskRepository`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/TaskRepository.java):
  ```java
  public interface TaskRepository extends JpaRepository<Task, Long>, QuerydslPredicateExecutor<Task>, TaskRepositoryCustom
  ```
* Interface chứa các hàm mở rộng là [`TaskRepositoryCustom`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/custom/TaskRepositoryCustom.java).
* **Quy ước của Spring Data JPA (Custom Repository Implementations):**
  Mặc định, Spring Data JPA tìm kiếm lớp thực thi có tên theo định dạng:
  $$\text{<Tên Repository Interface chính>} + \text{Impl}$$
  Do đó, đối với `TaskRepository`, tên class bắt buộc phải là **`TaskRepositoryImpl`**.
* File gốc được đặt tên là `RenameThisClass.java` (hoặc `TaskRepositoryCustomImpl.java`). Do không khớp quy ước hậu tố của Spring Data, Spring Data JPA không nhận diện được implementation này để ghép vào proxy của `TaskRepository`, dẫn đến lỗi khởi động hoặc không gọi được các phương thức mở rộng.

### 2.3. Khắc phục
1. Đổi tên file và tên class thành [`TaskRepositoryImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/custom/TaskRepositoryImpl.java).
2. Đính kèm annotation `@Repository` lên class:
```java
package vn.elca.training.repository.custom;

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
> From Postman, send a request to `http://localhost:8080/main` and you will see NullPointerException (actually 3 NullPointerException(s) respectively). Can you find the causes and fix them?

### 3.2. Phân tích 3 lỗi NullPointerException tuần tự

Mã nguồn ban đầu của [`MainController.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/web/MainController.java):
```java
@RestController
public class MainController extends AbstractApplicationController {
    private ProjectService projectService;
    private String title;

    @Value("${application.message}")
    private String message;

    @GetMapping("/main")
    public String main() {
        return title + ". " + String.format(message, projectService.count());
    }
}
```

#### 💥 Nguyên nhân 1 (NPE thứ nhất): `projectService` bị `null`
* **Vấn đề:** Biến `projectService` trong `MainController` không có `@Autowired` và không có hàm dựng (Constructor Injection).
* **Hậu quả:** Khi gọi `projectService.count()`, do `projectService` là `null` nên JVM ném `NullPointerException`.
* **Cách khắc phục:** Thêm Constructor Injection vào `MainController`:
  ```java
  public MainController(ProjectService projectService) {
      this.projectService = projectService;
  }
  ```

#### 💥 Nguyên nhân 2 (NPE thứ hai): `projectRepository` bị `null` trong `ProjectServiceImpl`
* **Vấn đề:** Sau khi `projectService` được inject, hàm `projectService.count()` được gọi:
  ```java
  // ProjectServiceImpl.java (nguyên bản)
  public class ProjectServiceImpl implements ProjectService {
      private ProjectRepository projectRepository;

      @Override
      public long count() {
          return projectRepository.count(); // projectRepository bị null!
      }
  }
  ```
  `projectRepository` trong `ProjectServiceImpl` cũng chưa được tiêm phụ thuộc.
* **Hậu quả:** Khi gọi `projectRepository.count()`, JVM ném `NullPointerException`.
* **Cách khắc phục:** Thêm Constructor Injection vào [`ProjectServiceImpl`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java):
  ```java
  public ProjectServiceImpl(ProjectRepository projectRepository, ApplicationMapper mapper) {
      this.projectRepository = projectRepository;
      this.mapper = mapper;
  }
  ```

#### 💥 Nguyên nhân 3 (NPE thứ ba): `title` và `message` không được nạp giá trị
* **Vấn đề:** 
  1. Biến `title` trong `MainController` không có annotation `@Value("${application.title}")` để đọc cấu hình từ file properties.
  2. Biến `message` nếu không được nạp từ file `messages.properties` thì sẽ mang giá trị `null`. Khi hàm `String.format(message, count)` thực thi với tham số đầu tiên `format == null`, phương thức `String.format` sẽ lập tức quăng `NullPointerException`:
     ```java
     // java.util.Formatter
     if (format == null) throw new NullPointerException();
     ```
* **Cách khắc phục:** 
  1. Thêm annotation `@Value("${application.title}")` cho trường `title` trong `MainController`.
  2. Đảm bảo file cấu hình [`ApplicationWebConfig.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/ApplicationWebConfig.java#L29) có đầy đủ:
     ```java
     @PropertySource({"classpath:/application.properties", "classpath:/messages.properties"})
     ```

### 3.3. Kết quả sau khi sửa
Gửi request `GET http://localhost:8080/main`:
* **HTTP Status:** `200 OK`
* **Response Body:**
  ```text
  Project Management Tool. Total number of projects: 5
  ```

---

## 4. Yêu cầu 4: Xây dựng GET endpoint tìm kiếm Project theo "keyword"

### 4.1. Yêu cầu đề bài
> Make the necessary changes in pilot-project-back to have an GET endpoint that receive a “keyword”. The response must return a list of projects with name containing the keyword.
> 
> *Notes:* The implementation of `ProjectServiceImpl` must be used.

### 4.2. Khắc phục lỗi xung đột ánh xạ (Ambiguous Mapping)
Trước đó, trong [`ProjectController`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/web/ProjectController.java) có 2 phương thức cùng gắn `@GetMapping("/search")` mà không có điều kiện phân biệt, dẫn đến lỗi:
`IllegalStateException: Ambiguous mapping. Cannot map 'projectController' method ... to {GET /projects/search}`.

### 4.3. Giải pháp cài đặt
Gộp việc xử lý vào phương thức `@GetMapping({"", "/search"})` nhận tham số tùy chọn `keyword`:
```java
@GetMapping({"", "/search"})
public List<ProjectDto> search(@RequestParam(value = "keyword", required = false) String keyword) {
    if (StringUtils.isNotBlank(keyword)) {
        return projectService.searchByKeyword(keyword);
    }
    return projectService.findAll()
            .stream()
            .map(mapper::projectToProjectDto)
            .collect(Collectors.toList());
}
```
* Trong [`ProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java#L36-L42), phương thức `searchByKeyword`:
  ```java
  @Override
  public List<ProjectDto> searchByKeyword(String keyword) {
      return projectRepository.findAll().stream()
              .filter(p -> p.getName().contains(keyword))
              .map(mapper::projectToProjectDto)
              .collect(Collectors.toList());
  }
  ```
* **Kiểm tra thực tế:**
  * Request: `GET http://localhost:8080/projects/search?keyword=EFV`
  * Response:
    ```json
    [
      {
        "id": 1,
        "name": "EFV",
        "customer": null,
        "finishingDate": "20/04/2020"
      }
    ]
    ```

---

## 5. Yêu cầu 5: Bổ sung tính năng mới (Tìm theo ID & Cập nhật Project với Dummy Service)

### 5.1. Yêu cầu đề bài
> In pilot-project-back, add new features:
> * GET endpoint to find a project by its ID and return the following information: ID, name, customer, finishing date.
> * POST endpoint to update a project, the following fields can be updated: name, customer, finishing date. Date format must be “dd/MM/yyyy”.
> 
> *Notes:* The dummy implementation of `ProjectService` must be used.

### 5.2. Các bước triển khai kỹ thuật

#### Bước 1: Cập nhật `ProjectDto` và định dạng ngày `"dd/MM/yyyy"`
Bổ sung trường `customer`, các hàm khởi tạo và annotation `@JsonFormat(pattern = "dd/MM/yyyy")` vào [`ProjectDto.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/ProjectDto.java):
```java
public class ProjectDto {
    private Long id;
    private String name;
    private String customer;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate finishingDate;
    // Getters and Setters...
}
```
Đồng thời cập nhật [`ApplicationMapper.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/util/ApplicationMapper.java) để ánh xạ trường `customer` giữa Entity và DTO.

#### Bước 2: Bổ sung phương thức vào interface `ProjectService`
```java
public interface ProjectService {
    List<Project> findAll();
    List<ProjectDto> searchByKeyword(String keyword);
    long count();
    ProjectDto findProjectById(Long id);
    ProjectDto updateProject(Long id, ProjectDto projectDto);
}
```

#### Bước 3: Cài đặt Dummy Service với lưu trữ trong bộ nhớ (In-Memory)
* Trong [`AbstractDummyProjectService.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/dummy/AbstractDummyProjectService.java), tạo `ConcurrentHashMap<Long, ProjectDto>` lưu trữ sẵn dữ liệu mẫu (EFV, CXTRANET, CRYSTAL BALL, ...).
* Cài đặt logic cho:
  * `findProjectById(Long id)`: Tìm trong map và trả về `ProjectDto`.
  * `updateProject(Long id, ProjectDto projectDto)`: Cập nhật 3 trường `name`, `customer`, `finishingDate`.
* Đánh dấu [`FirstDummyProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/dummy/FirstDummyProjectServiceImpl.java) với `@Primary` và `@Profile("dummy")`. Nhờ đó, khi kích hoạt profile `dummy`, Spring inject chính xác bean này mà không bị lỗi `NoUniqueBeanDefinitionException`.
* Bổ sung cài đặt tương ứng vào [`ProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java) để hệ thống hoạt động ổn định ở cả profile `dev` lẫn `dummy`.
* Bật profile dummy trong [`application.properties`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/resources/application.properties):
  ```properties
  spring.profiles.active=dummy
  ```

#### Bước 4: Xây dựng các Endpoints trong `ProjectController`
Trong [`ProjectController.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/web/ProjectController.java):
1. **GET endpoint tìm theo ID:**
   ```java
   @GetMapping({"/{id:\\d+}", "/id/{id:\\d+}"})
   public ResponseEntity<ProjectDto> findById(@PathVariable("id") Long id) {
       ProjectDto dto = projectService.findProjectById(id);
       if (dto == null) {
           return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(dto);
   }
   ```
2. **POST endpoint cập nhật project:**
   ```java
   @PostMapping({"", "/update", "/{id:\\d+}", "/update/{id:\\d+}"})
   @PutMapping({"", "/update", "/{id:\\d+}", "/update/{id:\\d+}"})
   public ResponseEntity<ProjectDto> updateProject(
           @PathVariable(value = "id", required = false) Long pathId,
           @RequestBody ProjectDto projectDto) {
       if (projectDto == null) {
           return ResponseEntity.badRequest().build();
       }
       Long id = pathId != null ? pathId : projectDto.getId();
       if (id == null) {
           return ResponseEntity.badRequest().build();
       }
       ProjectDto updated = projectService.updateProject(id, projectDto);
       if (updated == null) {
           return ResponseEntity.notFound().build();
       }
       return ResponseEntity.ok(updated);
   }
   ```

---

## 6. Yêu cầu 6: Kiểm thử bằng Postman (Postman Collection)

File Postman Collection v2.1 đã được tạo sẵn tại thư mục gốc dự án:
[**`pilot-project-back.postman_collection.json`**](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/pilot-project-back.postman_collection.json)

### Chi tiết các Request kiểm thử:

| STT | Tên Request | Method | URL | Request Body | Phản hồi thực tế (Response) |
|---|---|---|---|---|---|
| **1** | **GET Main Info** | `GET` | `http://localhost:8080/main` | Không có | `200 OK`<br>`Project Management Tool. Total number of projects: 5` |
| **2** | **Search by Keyword** | `GET` | `http://localhost:8080/projects/search?keyword=EFV` | Không có | `200 OK`<br>`[{"id":1,"name":"EFV","customer":"ELCA","finishingDate":"20/04/2020"}]` |
| **3** | **Find Project by ID** | `GET` | `http://localhost:8080/projects/1` | Không có | `200 OK`<br>`{"id":1,"name":"EFV","customer":"ELCA","finishingDate":"20/04/2020"}` |
| **4** | **Update Project by ID** | `POST` | `http://localhost:8080/projects/1` | `{"name":"EFV Dummy Updated","customer":"Dummy Customer ELCA","finishingDate":"15/11/2026"}` | `200 OK`<br>`{"id":1,"name":"EFV Dummy Updated","customer":"Dummy Customer ELCA","finishingDate":"15/11/2026"}` |
| **5** | **Update via `/update`** | `POST` | `http://localhost:8080/projects/update` | `{"id":1,"name":"EFV Updated via /update","customer":"ELCA Vietnam","finishingDate":"31/12/2026"}` | `200 OK`<br>`{"id":1,"name":"EFV Updated via /update","customer":"ELCA Vietnam","finishingDate":"31/12/2026"}` |

### Hướng dẫn import vào Postman:
1. Mở Postman.
2. Bấm nút **Import** ở góc trên bên trái.
3. Kéo thả hoặc chọn file [`pilot-project-back.postman_collection.json`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/pilot-project-back.postman_collection.json).
4. Thực thi tuần tự các request để kiểm tra kết quả backend.
