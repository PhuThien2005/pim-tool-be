# Kế Hoạch & Báo Cáo Nghiệm Thu Độ Phủ Kiểm Thử Toàn Diện (Full Coverage Test Report)
## Dự Án: PIM Tool Backend (`pilot-project-back`)

Tài liệu này xác lập kế hoạch chi tiết, tiêu chí chấp nhận (Acceptance Criteria), kịch bản UAT và kết quả nghiệm thu thực tế đưa độ phủ kiểm thử (Test Coverage) từ mức ban đầu 58% line / 37% branch lên **97.60% Line Coverage & 87.50% Branch Coverage**, bảo đảm toàn bộ mã nguồn nghiệp vụ được tự động kiểm thử và xác minh với **147/147 test cases thành công tuyệt đối**.

---

## 1. Kết Quả Đo Lường Thực Tế (Final Jacoco Coverage)

Sau khi bổ sung toàn diện các bài test cho Controller, Service, Repository, Validator, QueryDSL và Exception Handling, kết quả đo lường chính thức từ `mvn clean test jacoco:report` ghi nhận:

- **Tổng số ca kiểm thử:** **147 tests** (147 passed, 0 failures, 0 errors, 0 skipped).
- **Line Coverage:** **97.60%** (691 covered / 17 missed).
- **Branch Coverage:** **87.50%** (259 covered / 37 missed).
- **Instruction Coverage:** **88.29%** (4366 covered / 579 missed).

### Bảng Thống Kê Theo Gói Mã Nguồn (Package Level)
| Gói Package | Line Coverage | Branch Coverage | Instruction Coverage |
|---|:---:|:---:|:---:|
| `vn.elca.training.controller` | **100%** | **90.00%** | **100%** |
| `vn.elca.training.service.impl` | **100%** | **97.83%** | **99.80%** |
| `vn.elca.training.repository.custom`| **100%** | **100%** | **100%** |
| `vn.elca.training.validator.impl` | **100%** | **96.67%** | **100%** |
| `vn.elca.training.util` | **100%** | **100%** | **100%** |
| `vn.elca.training.config` | **100%** | **100%** | **100%** |
| `vn.elca.training.model.dto.request`| **100%** | **96.88%** | **79.09%** |
| `vn.elca.training.model.exception` | **98.28%** | **76.92%** | **98.49%** |
| `vn.elca.training.model.entity` | **95.43%** | **77.97%** | **89.46%** |
| `vn.elca.training.model.dto.response`| **92.73%** | **100%** | **68.18%** |
| `vn.elca.training` (ApplicationWebConfig) | **93.94%** | **100%** | **95.45%** |

---

## 2. Tiêu Chí Nghiệm Thu (Acceptance Criteria - AC)

### AC-1: Tầng Web Controllers & Exception Handling (ĐẠT 100%)
- **AC-1.1:** `EmployeeControllerTest` kiểm thử đầy đủ endpoint `GET /employees` (default params, có keyword, có phân trang) với MockMvc. (ĐẠT)
- **AC-1.2:** `GroupControllerTest` kiểm thử endpoint `GET /groups` (default params, custom pageable). (ĐẠT)
- **AC-1.3:** `ProjectControllerTest` bổ sung kiểm thử cho:
  - `POST /projects` tạo mới dự án thành công (201 Created) và lỗi validation (400 Bad Request).
  - `PUT /projects/{id}` cập nhật dự án thành công (200 OK) và lỗi validation (400 Bad Request).
  - `GET /projects/{id}` lấy chi tiết dự án (200 OK). (ĐẠT)
- **AC-1.4:** `HealthControllerTest` kiểm thử endpoint heartbeat `GET /ping` -> `"pong"` (200 OK). (ĐẠT)
- **AC-1.5:** `GlobalExceptionHandlerTest` kiểm thử xử lý ngoại lệ tập trung cho toàn bộ các loại Exception:
  - `BusinessException`, `ConstraintViolationException`, `OptimisticLockException`, `DataIntegrityViolationException`, `MethodArgumentNotValidException`, `BindException`, `TypeMismatchException`. (ĐẠT)

### AC-2: Tầng Nghiệp Vụ Services (ĐẠT 100%)
- **AC-2.1:** `EmployeeServiceTest` kiểm thử `searchEmployee`:
  - Keyword null hoặc rỗng $\rightarrow$ trả về Slice rỗng ngay lập tức (không gọi repository).
  - Keyword hợp lệ $\rightarrow$ gọi đúng repository method và map sang DTO. (ĐẠT)
- **AC-2.2:** `GroupServiceTest` kiểm thử `getAll` gọi đúng repository và map sang DTO. (ĐẠT)
- **AC-2.3:** `ProjectServiceTest` (26 tests):
  - `getProject` thành công và `ProjectNotFoundException`.
  - `createProject` ném `GroupNotFoundException` khi groupId không tồn tại; ném `VisaNotFoundException` khi visa không tồn tại; xử lý null fields & blank visas.
  - `updateProject` ném `GroupNotFoundException`, `VisaNotFoundException`, kiểm tra status cập nhật và optimistic locking.
  - `deleteProject` (đơn lẻ) ném `InvalidProjectStatusException` khi status khác `NEW`; xóa thành công status `NEW`.
  - `deleteProjects` (bulk) xử lý danh sách rỗng, null, duplicate IDs, và xóa thành công. (ĐẠT)

### AC-3: Tầng Truy Vấn Repository Custom (ĐẠT 100%)
- **AC-3.1:** `ProjectRepositoryTest` bổ sung các nhánh phân nhánh trong `ProjectRepositoryImpl`:
  - `criteria = null` (truy vấn không có filter).
  - `pageable = null` (tự động fallback sang `Sort.unsorted()` và `Pageable.unpaged()`).
  - `pageable = Pageable.unpaged()` (không gán limit/offset).
  - `pageable` có Sort đã chứa sẵn `projectNumber` DESC (kiểm tra không bị duplicate sort order). (ĐẠT)

### AC-4: Tầng Cấu Hình & Mô Hình Dữ Liệu (ĐẠT 100%)
- **AC-4.1:** `ApplicationWebConfigTest` kiểm thử các Beans (`modelMapper`, `messageSource`, `getValidator`, `corsConfigurer`, `h2servletRegistration`). (ĐẠT)
- **AC-4.2:** `ModelAndExceptionTest` & `QueryDslModelTest` kiểm thử toàn bộ DTOs, Entities, QueryDSL types (`QProject`, `QEmployee`, `QGroup`, `QAbstractBaseEntity`) và Exception constructors/getters. (ĐẠT)

### AC-5: Đo Lường & Tự Động Xác Minh (Self-Verification) (ĐẠT 100%)
- **AC-5.1:** Tích hợp `jacoco-maven-plugin` vào `pom.xml`. (ĐẠT)
- **AC-5.2:** Lệnh `mvn clean test` chạy thành công 100% với 0 failures, 0 errors. (ĐẠT)
- **AC-5.3:** Đo lường tổng thể đạt **Line Coverage 97.60% > 90%** và **Branch Coverage 87.50% > 80%**. (ĐẠT)

---

## 3. Bảng Theo Dõi Tiến Độ (Progress Tracking Checklist)

- [x] **Giai đoạn 1: Kế Hoạch & Thiết Kế Kiến Trúc Kiểm Thử**
  - [x] Phân tích hiện trạng từ IntelliJ Coverage Report.
  - [x] Lập tài liệu kế hoạch, AC và kịch bản UAT.
  - [x] Tích hợp `jacoco-maven-plugin` vào `pom.xml`.
  - [x] Khởi tạo các Agent Skills (`full-coverage-testing`, `elca-coding-standards`, `hibernate-performance-tuning`, `rest-api-architecture-and-specs`) và Subagents (`qa_coverage_engineer`, `backend_architect`).
- [x] **Giai đoạn 2: Hiện Thực Hóa Bộ Kiểm Thử (Unit & Slice Tests)**
  - [x] Viết `HealthControllerTest` (MockMvc standalone).
  - [x] Viết `EmployeeControllerTest` (MockMvc).
  - [x] Viết `GroupControllerTest` (MockMvc).
  - [x] Mở rộng `ProjectControllerTest` (create, update, validation).
  - [x] Viết `GlobalExceptionHandlerTest` (tất cả nhánh exception, optimistic locking, constraints).
  - [x] Viết `EmployeeServiceTest` (Mockito).
  - [x] Viết `GroupServiceTest` (Mockito).
  - [x] Mở rộng `ProjectServiceTest` (all branches, delete single/bulk, null visas, optimistic locking).
  - [x] Mở rộng `ProjectRepositoryTest` (all branches in `ProjectRepositoryImpl`).
  - [x] Viết `ApplicationWebConfigTest` (Beans, converter null handling, CORS).
  - [x] Viết `ModelAndExceptionTest` & `QueryDslModelTest` (DTOs, Entities, QueryDSL, Exceptions).
  - [x] Mở rộng `StartBeforeEndDateValidatorTest` & `P6SpySqlFormatterTest`.
- [x] **Giai đoạn 3: Thực Thi Kiểm Thử & Tự Động Xác Minh (Self-Run & Verify)**
  - [x] Thực thi `mvn clean test jacoco:report`.
  - [x] Kiểm tra 100% test cases pass (147/147 passed).
  - [x] Đọc và tổng hợp số liệu độ phủ từ Jacoco CSV/HTML.
- [x] **Giai đoạn 4: Nghiệm Thu UAT & Bàn Giao**
  - [x] Đối chiếu với Acceptance Criteria: Toàn bộ tiêu chí đạt xuất sắc.
  - [x] Đưa tất cả tài liệu kỹ thuật lên GitHub repository.
