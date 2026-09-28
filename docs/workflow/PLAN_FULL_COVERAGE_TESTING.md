# Kế Hoạch & Tiêu Chuẩn Nâng Cao Độ Phủ Kiểm Thử (Full Coverage Test Plan)
## Dự Án: PIM Tool Backend (`pilot-project-back`)

Tài liệu này xác lập kế hoạch chi tiết, tiêu chí chấp nhận (Acceptance Criteria), kịch bản UAT và bảng theo dõi tiến độ để đưa độ phủ kiểm thử (Test Coverage) từ mức 58% line / 37% branch lên **> 90% Line Coverage & > 80% Branch Coverage**, bảo đảm toàn bộ mã nguồn nghiệp vụ được tự động kiểm thử và xác minh.

---

## 1. Phân Tích Thực Trạng Độ Phủ (Baseline Coverage Analysis)

Theo số liệu đo lường trực quan từ IntelliJ IDEA:
- **Toàn bộ `vn.elca.training`:**
  - Class: 81% (9/11)
  - Method: 61% (40/65)
  - Line: 58% (112/191)
  - Branch: 37% (40/106)
- **Chi tiết các thành phần còn thiếu sót kiểm thử:**
  1. `controller`: Method 50%, Line 44%, Branch 21%
     - `EmployeeController`: **0% (chưa có test)**
     - `GroupController`: **0% (chưa có test)**
     - `ProjectController`: Thiếu test cho `createProject`, `updateProject`, và validation payload.
     - `GlobalExceptionHandler`: Thiếu test bao phủ các nhánh ngoại lệ (`OptimisticLockException`, `DataIntegrityViolationException`, `TypeMismatchException`, `ConstraintViolationException`).
  2. `service`:
     - `EmployeeServiceImpl`: **0% (chưa có test)**
     - `GroupServiceImpl`: **0% (chưa có test)**
     - `ProjectServiceImpl`: Thiếu test cho `getProject`, `GroupNotFoundException`, `VisaNotFoundException`, `resolveEmployeesByVisas` null/empty.
  3. `repository.custom`: Branch 57%
     - `ProjectRepositoryImpl`: Thiếu test cho các nhánh `criteria == null`, `pageable == null`, `Pageable.unpaged()`, và sort tùy chỉnh có chứa sẵn `projectNumber`.
  4. `model`: Class 66%, Method 60%, Line 50%, Branch 34%
     - Chưa kiểm thử getter, setter, builder, equals, hashCode của các DTOs, Entities và Exception constructors.
  5. `ApplicationWebConfig`: Line 64%, Branch 0%
     - Chưa test converter null source, servlet registration, cors configuration, validator bean.

---

## 2. Tiêu Chí Nghiệm Thu (Acceptance Criteria - AC)

### AC-1: Tầng Web Controllers & Exception Handling
- **AC-1.1:** `EmployeeControllerTest` kiểm thử đầy đủ endpoint `GET /employees` (default params, có keyword, có phân trang) với MockMvc.
- **AC-1.2:** `GroupControllerTest` kiểm thử endpoint `GET /groups` (default params, custom pageable).
- **AC-1.3:** `ProjectControllerTest` bổ sung kiểm thử cho:
  - `POST /projects` tạo mới dự án thành công (201 Created) và lỗi validation (400 Bad Request).
  - `PUT /projects/{id}` cập nhật dự án thành công (200 OK) và lỗi validation (400 Bad Request).
  - `GET /projects/{id}` lấy chi tiết dự án (200 OK).
- **AC-1.4:** `GlobalExceptionHandlerTest` kiểm thử xử lý ngoại lệ tập trung cho toàn bộ các loại Exception:
  - `BusinessException`, `ConstraintViolationException`, `OptimisticLockException`, `DataIntegrityViolationException`, `MethodArgumentNotValidException`, `TypeMismatchException`, `Exception` không xác định.

### AC-2: Tầng Nghiệp Vụ Services
- **AC-2.1:** `EmployeeServiceTest` kiểm thử `searchEmployee`:
  - Keyword null hoặc rỗng $\rightarrow$ trả về Slice rỗng ngay lập tức (không gọi repository).
  - Keyword hợp lệ $\rightarrow$ gọi đúng repository method và map sang DTO.
- **AC-2.2:** `GroupServiceTest` kiểm thử `getAll` gọi đúng repository và map sang DTO.
- **AC-2.3:** `ProjectServiceTest` bổ sung:
  - `getProject` thành công và `ProjectNotFoundException`.
  - `createProject` ném `GroupNotFoundException` khi groupId không tồn tại.
  - `createProject` ném `VisaNotFoundException` khi danh sách visa chứa visa không tồn tại.
  - `updateProject` ném `GroupNotFoundException` và `VisaNotFoundException`.
  - `deleteProject` (đơn lẻ) ném `InvalidProjectStatusException` khi status khác `NEW`.

### AC-3: Tầng Truy Vấn Repository Custom
- **AC-3.1:** `ProjectRepositoryTest` bổ sung các nhánh phân nhánh trong `ProjectRepositoryImpl`:
  - `criteria = null` (truy vấn không có filter).
  - `pageable = null` (tự động fallback sang `Sort.unsorted()` và `Pageable.unpaged()`).
  - `pageable = Pageable.unpaged()` (không gán limit/offset).
  - `pageable` có Sort đã chứa sẵn `projectNumber` DESC (kiểm tra không bị duplicate sort order).

### AC-4: Tầng Cấu Hình & Mô Hình Dữ Liệu
- **AC-4.1:** `ApplicationWebConfigTest` kiểm thử các Beans (`modelMapper`, `messageSource`, `getValidator`, `corsConfigurer`, `h2servletRegistration`).
- **AC-4.2:** `ModelAndExceptionTest` kiểm thử toàn bộ DTOs, Entities và Exception constructors/getters.

### AC-5: Đo Lường & Tự Động Xác Minh (Self-Verification)
- **AC-5.1:** Tích hợp `jacoco-maven-plugin` vào `pom.xml`.
- **AC-5.2:** Lệnh `mvn clean test` chạy thành công 100% với 0 failures, 0 errors.
- **AC-5.3:** Đo lường tổng thể đạt **Line Coverage > 90%** và **Branch Coverage > 80%**.

---

## 3. Kịch Bản UAT (User Acceptance Testing Scenarios)

| Mã UAT | Kịch Bản Kiểm Thử | Điều Kiện Đầu Vào | Kết Quả Mong Đợi |
|---|---|---|---|
| **UAT-01** | MockMvc Employee Controller | Gọi `GET /employees?keyword=DTH` | Trả về 200 OK, payload chứa danh sách nhân viên |
| **UAT-02** | MockMvc Group Controller | Gọi `GET /groups?page=0&size=5` | Trả về 200 OK, payload chứa danh sách nhóm |
| **UAT-03** | MockMvc Project Create & Update | Gửi POST/PUT với JSON hợp lệ | Trả về 201 Created và 200 OK tương ứng |
| **UAT-04** | Global Exception Handler Error Formats | Kích hoạt OptimisticLock, DataIntegrity | Trả về HTTP 409 Conflict hoặc 400 Bad Request với ErrorResponse chuẩn |
| **UAT-05** | Service Edge Cases (Visas, Groups) | Input visa không tồn tại vào service | Ném `VisaNotFoundException`, `GroupNotFoundException` |
| **UAT-06** | Jacoco Report Verification | Chạy `mvn test jacoco:report` | Báo cáo `target/site/jacoco/index.html` được sinh ra, đạt tỷ lệ > 90% |

---

## 4. Bảng Theo Dõi Tiến Độ (Progress Tracking Checklist)

- [x] **Giai đoạn 1: Kế Hoạch & Thiết Kế Kiến Trúc Kiểm Thử**
  - [x] Phân tích hiện trạng từ IntelliJ Coverage Report.
  - [x] Lập tài liệu kế hoạch, AC và kịch bản UAT.
  - [x] Tích hợp `jacoco-maven-plugin` vào `pom.xml`.
  - [x] Khởi tạo Skill `full-coverage-testing` và Subagent `qa_coverage_engineer`.
- [x] **Giai đoạn 2: Hiện Thực Hóa Bộ Kiểm Thử (Unit & Slice Tests)**
  - [x] Viết `EmployeeControllerTest` (MockMvc).
  - [x] Viết `GroupControllerTest` (MockMvc).
  - [x] Mở rộng `ProjectControllerTest` (create, update, validation).
  - [x] Viết `GlobalExceptionHandlerTest` (tất cả nhánh exception).
  - [x] Viết `EmployeeServiceTest` (Mockito).
  - [x] Viết `GroupServiceTest` (Mockito).
  - [x] Mở rộng `ProjectServiceTest` (get, exceptions, visas resolution).
  - [x] Mở rộng `ProjectRepositoryTest` (all branches in `ProjectRepositoryImpl`).
  - [x] Viết `ApplicationWebConfigTest` (Beans, converter null handling, CORS).
  - [x] Viết `ModelAndExceptionTest` (DTOs, Entities, Exceptions).
- [x] **Giai đoạn 3: Thực Thi Kiểm Thử & Tự Động Xác Minh (Self-Run & Verify)**
  - [x] Thực thi `mvn clean test jacoco:report`.
  - [x] Kiểm tra 100% test cases pass (119/119 passed).
  - [x] Đọc và tổng hợp số liệu độ phủ từ Jacoco CSV/HTML.
- [x] **Giai đoạn 4: Nghiệm Thu UAT & Bàn Giao**
  - [x] Đối chiếu với Acceptance Criteria.
  - [x] Đề xuất lệnh `/goal` cho chu kỳ phát triển autonomous tiếp theo.
