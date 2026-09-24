# Hướng Dẫn Tái Hiện Lỗi Data Integrity Violation (Race Condition / TOCTOU)

Tài liệu này hướng dẫn chi tiết từng bước cách mô phỏng lỗi đồng thời **TOCTOU (Time-Of-Check to Time-Of-Use)** dẫn đến ngoại lệ **`DataIntegrityViolationException`** (vi phạm ràng buộc `UNIQUE` trên cột `PROJECT_NUMBER`) bằng cách kết hợp **IntelliJ Debugger**, **Bruno/Postman** và **H2 Web Console**.

---

## 1. Nguyên Lý Mô Phỏng

Lỗi TOCTOU xảy ra khi hai tiến trình cùng cố gắng tạo dự án với cùng một `projectNumber`:
```
   [Luồng 1 - Bruno API]                           [Luồng 2 - H2 Console]
             │                                                │
1. Gọi POST /projects (projectNumber = 8888)                  │
             │                                                │
2. Kiểm tra existsByProjectNumber(8888)?                      │
   ──► Trả về FALSE (Hợp lệ!)                                  │
             │                                                │
   [DỪNG LẠI TẠI BREAKPOINT Ở ĐÂY]                            │
             │                                                │
             │                           3. Chèn thủ công vào DB:
             │                              INSERT INTO PROJECT(..., 8888)
             │                           ──► DB lưu thành công!
             │                                                │
4. Nhả Breakpoint (F9)                                        │
   projectRepository.save(project)                            │
   Hibernate bắn SQL: INSERT INTO PROJECT(..., 8888)          │
             │                                                │
5. 💥 DATABASE BÁO LỖI:                                      │
   Unique index or primary key violation: "UK_PROJECT_NUMBER" │
   Spring bọc thành: DataIntegrityViolationException          │
```

---

## 2. Các Bước Thực Hiện Chi Tiết

### Bước 1: Đặt Breakpoint trong IntelliJ IDEA

1. Mở file [`ProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java).
2. Tìm đến phương thức `createProject(CreateProjectRequest request)` (khoảng dòng 61 - 67).
3. Đặt một **Line Breakpoint** (chấm đỏ) tại **dòng 66**:
   ```java
   if (projectRepository.existsByProjectNumber(request.getProjectNumber())) {
       throw new ProjectNumberAlreadyExistsException(request.getProjectNumber());
   }

   Group group = groupRepository.findById(request.getGroupId()) // <--- ĐẶT BREAKPOINT TẠI ĐÂY
           .orElseThrow(() -> new GroupNotFoundException(request.getGroupId()));
   ```
4. Khởi động ứng dụng Spring Boot ở chế độ **DEBUG** (bấm icon con bọ 🐞 trong IntelliJ).

---

### Bước 2: Chuẩn Bị Request Trong Bruno / Postman

Tạo một Request tạo mới dự án với một `projectNumber` hoàn toàn mới (ví dụ `8888`):

- **Method**: `POST`
- **URL**: `http://localhost:8080/projects`
- **Headers**: 
  - `Content-Type: application/json`
- **Body** (`raw JSON`):
  ```json
  {
    "projectNumber": 8888,
    "name": "TEST TOCTOU RACE CONDITION",
    "customer": "Swisscom",
    "groupId": 1,
    "status": "NEW",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "visas": ["HTV", "NQN"]
  }
  ```

👉 **Hành động**: Bấm nút **Send** trong Bruno.
- Ngay lập tức, IntelliJ IDEA sẽ nhảy lên và luồng thực thi bị **DỪNG LẠI** tại dòng 66.
- Lúc này, câu lệnh `existsByProjectNumber(8888)` đã chạy xong và xác nhận: số `8888` **chưa tồn tại** (đã lọt qua cổng soát vé số 1).

---

### Bước 3: Mở H2 Console và Chèn Thủ Công (Giả lập Luồng 2)

Trong lúc IntelliJ vẫn đang **tạm dừng luồng 1**:

1. Mở trình duyệt web và truy cập vào:
   ```text
   http://localhost:8080/h2console/
   ```
2. Điền thông tin kết nối (chuẩn theo [`application.properties`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/resources/application.properties)):
   - **Driver Class**: `org.h2.Driver`
   - **JDBC URL**: `jdbc:h2:mem:onboardingexercise`
   - **User Name**: `sa`
   - **Password**: *(để trống)*
   - Bấm nút **Connect**.
3. Trong khung soạn thảo SQL của H2 Console, nhập và chạy câu lệnh sau:
   ```sql
   INSERT INTO PROJECT (ID, VERSION, GROUP_ID, PROJECT_NUMBER, NAME, CUSTOMER, STATUS, START_DATE, END_DATE) 
   VALUES (999, 0, 1, 8888, 'CONCURRENT PROJECT INSERTED FROM H2', 'Rolex', 'NEW', '2025-01-01', '2025-12-31');
   ```
4. Bấm nút **Run** (Ctrl + Enter).
   - Kết quả hiển thị: `Update count: 1`.
   - Lúc này, Database đã chính thức lưu một bản ghi mang số `PROJECT_NUMBER = 8888`.

---

### Bước 4: Nhả Breakpoint trong IntelliJ (Resume Execution)

1. Quay trở lại cửa sổ **IntelliJ IDEA**.
2. Bấm nút **Resume Program** (phím tắt **F9** trên Windows) để cho Luồng 1 tiếp tục chạy.
3. Luồng 1 sẽ chạy qua các dòng tiếp theo và gọi:
   ```java
   Project saved = projectRepository.save(project);
   ```
4. Đến cuối transaction (hoặc khi flush), Hibernate gửi lệnh `INSERT INTO PROJECT (..., PROJECT_NUMBER=8888)` xuống H2 Database.

---

### Bước 5: Quan Sát Hiện Tượng & Kết Quả Thực Tế

#### A. Trong Terminal / Console của IntelliJ:
Bạn sẽ thấy Exception nổ ra màu đỏ với StackTrace:
```text
org.springframework.dao.DataIntegrityViolationException: could not execute statement; SQL [n/a]; constraint ["UK_LU0HN5S04GTK9WKXS6729W9O9 ON PUBLIC.PROJECT(PROJECT_NUMBER)..."]
    at org.springframework.orm.jpa.vendor.HibernateJpaDialect.convertHibernateAccessException(HibernateJpaDialect.java:298)
    ...
Caused by: org.hibernate.exception.ConstraintViolationException: could not execute statement
    ...
Caused by: org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException: Unique index or primary key violation: "UK_LU0HN5S04GTK9WKXS6729W9O9 ON PUBLIC.PROJECT(PROJECT_NUMBER NULLS FIRST) VALUES ( /* 999 */ 8888 )"; SQL statement:
insert into project (customer, end_date, group_id, name, project_number, start_date, status, version, id) values (?, ?, ?, ?, ?, ?, ?, ?, ?) [23505-193]
```

#### B. Trong Response của Bruno:
Vì hiện tại [`GlobalExceptionHandler.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/controller/GlobalExceptionHandler.java) chưa có handler riêng cho ngoại lệ này, nó sẽ rơi vào `handleGeneralException` và trả về:

- **HTTP Status**: **`500 Internal Server Error`**
- **JSON Response**:
  ```json
  {
    "status": 500,
    "errorCode": "INTERNAL_SERVER_ERROR",
    "message": "Unexpected error occurred [could not execute statement; SQL [n/a]; constraint [\"UK_LU0HN5S04GTK9WKXS6729W9O9 ON PUBLIC.PROJECT(PROJECT_NUMBER)...\"]]. Please contact your administrator",
    "timestamp": "2026-09-23T17:30:00.123456"
  }
  ```

---

## 3. Cách Khắc Phục Triệt Để (Clean RESTful Response)

Để biến lỗi `500 Internal Server Error` này thành phản hồi nghiệp vụ chuẩn xác (**`409 Conflict`** hoặc **`400 Bad Request`**), thêm phương thức sau vào [`GlobalExceptionHandler.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/controller/GlobalExceptionHandler.java):

```java
import org.springframework.dao.DataIntegrityViolationException;

@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, Locale locale) {
    log.warn("Data integrity violation (concurrent race condition): {}", ex.getMessage());

    String message = getLocalizedMessage("error.project.number.exist", 
            "The project number already existed", locale);

    ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value()) // HTTP 409 Conflict
            .errorCode("PROJECT_NUMBER_ALREADY_EXISTS")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
}
```

Sau khi thêm handler trên, nếu lặp lại các bước 1-4 ở trên, Bruno sẽ nhận về mã HTTP **`409 Conflict`** sạch đẹp:
```json
{
  "status": 409,
  "errorCode": "PROJECT_NUMBER_ALREADY_EXISTS",
  "message": "The project number already existed",
  "timestamp": "2026-09-23T17:30:00.123456"
}
```
