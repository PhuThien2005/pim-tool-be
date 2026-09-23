# Tổng Hợp Toàn Bộ Test Suite - Dự Án PIM Tool (pilot-project-back)

Tài liệu này tổng hợp chi tiết toàn bộ các bộ kiểm thử (Test Suites) hiện có trong dự án backend `pilot-project-back`. Toàn bộ 64 test cases đều đã được thực thi tự động qua Maven (`mvn test`) và đạt tỷ lệ thành công 100% (64 passed, 0 failures, 0 errors, 0 skipped).

---

## 1. Tổng Quan Kiến Trúc Kiểm Thử (Testing Overview)

Hệ thống test được tổ chức phân tầng rõ ràng theo mô hình Testing Pyramid:

```
                  ┌──────────────────────────────┐
                  │    Controller Web MVC Test   │  (12 tests - MockMvc)
                  ├──────────────────────────────┤
                  │     Service Unit Test        │  (11 tests - Mockito)
                  ├──────────────────────────────┤
                  │   Data Repository Test (H2)  │  (11 tests - SpringBootTest)
                  ├──────────────────────────────┤
                  │   QueryDSL Predicate Test    │  (7 tests - Unit Test)
                  ├──────────────────────────────┤
                  │ Database Execution Plan Test │  (8 tests - EXPLAIN ANALYZE)
                  ├──────────────────────────────┤
                  │ Bean Validation Rules Test   │  (15 tests - Unit Test)
                  └──────────────────────────────┘
```

### Bảng Thống Kê Tổng Hợp

| STT | Test Class | Tầng (Layer) | Công nghệ / Framework | Số lượng Test | Kết quả |
|---|---|---|---|:---:|:---:|
| 1 | [`ProjectControllerTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/controller/ProjectControllerTest.java) | Presentation (Web MVC) | Spring Boot Test, MockMvc, Mockito | 12 | PASS (100%) |
| 2 | [`ProjectServiceTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/service/ProjectServiceTest.java) | Business Logic (Service) | JUnit 4, Mockito, ModelMapper | 11 | PASS (100%) |
| 3 | [`ProjectRepositoryTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/repository/ProjectRepositoryTest.java) | Data Access (Repository) | Spring Boot Test, JPA, H2 In-Memory | 11 | PASS (100%) |
| 4 | [`SearchProjectCriteriaTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/model/dto/request/SearchProjectCriteriaTest.java) | Criteria / QueryDSL | JUnit 4, QueryDSL | 7 | PASS (100%) |
| 5 | [`ProjectExecutionPlanTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/repository/ProjectExecutionPlanTest.java) | Database & Indexing Plan | Spring Boot Test, EXPLAIN ANALYZE, P6Spy | 8 | PASS (100%) |
| 6 | [`StartBeforeEndDateValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/StartBeforeEndDateValidatorTest.java) | Validation (Cross-field Date) | JUnit 4, Mockito, Bean Validation | 5 | PASS (100%) |
| 7 | [`VisasValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/VisasValidatorTest.java) | Validation (List of Visas) | JUnit 4, Bean Validation | 5 | PASS (100%) |
| 8 | [`VisaValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/VisaValidatorTest.java) | Validation (Single Visa) | JUnit 4, Bean Validation | 5 | PASS (100%) |
| **Tổng** | **8 Test Classes** | **Toàn bộ các tầng** | **JUnit 4 / Spring Boot / Mockito** | **64** | **PASS (100%)** |

---

## 2. Chi Tiết Từng Bộ Kiểm Thử (Detailed Test Suites)

### 2.1. Presentation Layer: [`ProjectControllerTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/controller/ProjectControllerTest.java)
- **Mục đích**: Kiểm tra toàn diện các RESTful HTTP endpoints của [`ProjectController`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/controller/ProjectController.java), bao gồm HTTP status codes, routing, binding tham số URL/query params, payload JSON, validation lỗi request và exception handling.
- **Số lượng test**: 12 tests.

| STT | Tên Test Method | Kịch bản kiểm thử | Kỳ vọng đầu ra |
|---|---|---|---|
| 1 | `testSearchProjects_Success` | Gọi `GET /projects` không tham số | Trả về HTTP 200 OK, JSON format dạng Page chứa danh sách project. |
| 2 | `testSearchProjects_WithKeyword` | Gọi `GET /projects?keyword=EFV` | Trả về HTTP 200 OK, parse đúng param keyword sang tiêu chí tìm kiếm. |
| 3 | `testSearchProjects_InvalidLeaderVisa_Returns400` | Gọi `GET /projects?leaderVisa=INVALID_VISA` | Trả về HTTP 400 Bad Request, body chứa `errorCode: "VALIDATION_ERROR"` và field error `leaderVisa`. |
| 4 | `testSearchProjects_InvalidStartDateRange_Returns400` | Gọi `GET /projects` với `startDateFrom > startDateTo` | Trả về HTTP 400 Bad Request, chặn khoảng ngày không hợp lệ. |
| 5 | `testSearchProjects_InvalidEndDateRange_Returns400` | Gọi `GET /projects` với `endDateFrom > endDateTo` | Trả về HTTP 400 Bad Request, báo lỗi validation khoảng ngày kết thúc. |
| 6 | `testSearchProjects_WithStatus_Returns200` | Gọi `GET /projects?keyword=ee&status=NEW` | Trả về HTTP 200 OK, binding chính xác enum `ProjectStatus.NEW`. |
| 7 | `testSearchProjects_WithInvalidStatus_Returns400` | Gọi `GET /projects?status=INVALID_STATUS` | Trả về HTTP 400 Bad Request do sai enum mapping. |
| 8 | `testDeleteProject_Success_Returns204` | Gọi `DELETE /projects/1` | Trả về HTTP 204 No Content, gọi hàm service xóa thành công. |
| 9 | `testDeleteProjects_Success_Returns204` | Gọi `DELETE /projects` với body `[1, 2]` | Trả về HTTP 204 No Content, thực thi xóa hàng loạt dự án. |
| 10 | `testDeleteProject_NotFound_Returns404` | Gọi `DELETE /projects/999` (ID không tồn tại) | Trả về HTTP 404 Not Found, body chứa `errorCode: "PROJECT_NOT_FOUND"`. |
| 11 | `testDeleteProject_InvalidStatus_Returns400` | Gọi `DELETE /projects/1` (dự án khác trạng thái `NEW`) | Trả về HTTP 400 Bad Request, body chứa `errorCode: "INVALID_PROJECT_STATUS"`. |
| 12 | `testGetProject_Success_Returns200` | Gọi `GET /projects/1` | Trả về HTTP 200 OK cùng thông tin chi tiết [`ProjectDetailResponse`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/response/ProjectDetailResponse.java). |

---

### 2.2. Service Layer: [`ProjectServiceTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/service/ProjectServiceTest.java)
- **Mục đích**: Kiểm tra độc lập logic nghiệp vụ của [`ProjectServiceImpl`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java) bằng Mockito unit tests, đảm bảo xử lý chính xác các điều kiện biên, exception handling, mapping DTO và kiểm soát khóa lạc quan (Optimistic Locking).
- **Số lượng test**: 11 tests.

| STT | Tên Test Method | Kịch bản kiểm thử | Kỳ vọng đầu ra |
|---|---|---|---|
| 1 | `testSearchProjects_SuccessfulMapping` | Tìm kiếm project và map sang [`ProjectListResponse`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/response/ProjectListResponse.java) | Trả về đúng Page, ánh xạ đúng các trường `projectNumber`, `name`, `customer`. |
| 2 | `testSearchProjects_EmptyResult` | Repository trả về danh sách rỗng | Không thực hiện map entity, trả về Page rỗng an toàn không gây lỗi. |
| 3 | `testDeleteProjects_Success` | Xóa batch các project trạng thái `NEW` | Gọi repository thực thi `deleteAll()` trên danh sách project hợp lệ. |
| 4 | `testDeleteProjects_NotFound_ThrowsException` | Truyền ID có phần tử không tồn tại trong database | Ném [`ProjectNotFoundException`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/exception/ProjectNotFoundException.java). |
| 5 | `testDeleteProjects_InvalidStatus_ThrowsException` | Danh sách xóa chứa project có trạng thái khác `NEW` (ví dụ: `INP`) | Ném [`InvalidProjectStatusException`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/exception/InvalidProjectStatusException.java). |
| 6 | `testDeleteProjects_EmptyList_DoesNothing` | Truyền danh sách rỗng hoặc null vào hàm xóa batch | Bỏ qua an toàn, không tương tác với database. |
| 7 | `testDeleteProject_Success` | Xóa 1 dự án hợp lệ theo ID | Tìm thấy entity và gọi `projectRepository.delete(project)`. |
| 8 | `testCreateProject_Success` | Tạo mới project với đầy đủ thông tin hợp lệ | Lưu thành công vào DB, trả về [`ProjectDetailResponse`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/response/ProjectDetailResponse.java). |
| 9 | `testCreateProject_DuplicateNumber_ThrowsException` | Tạo project với số `projectNumber` đã tồn tại | Ném [`ProjectNumberAlreadyExistsException`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/exception/ProjectNumberAlreadyExistsException.java). |
| 10 | `testUpdateProject_Success` | Cập nhật project với đúng phiên bản `version` | Cập nhật các trường được phép thay đổi và gọi `saveAndFlush(project)`. |
| 11 | `testUpdateProject_OptimisticLockingFailure_ThrowsException` | Cập nhật project với `version` cũ hơn version trong DB | Ném `ObjectOptimisticLockingFailureException` để ngăn chặn xung đột ghi đè đồng thời. |

---

### 2.3. Data Access Layer: [`ProjectRepositoryTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/repository/ProjectRepositoryTest.java)
- **Mục đích**: Kiểm tra tương tác thực tế với cơ sở dữ liệu H2 In-Memory qua JPA/Hibernate và QueryDSL, xác thực tính đúng đắn của dữ liệu truy vấn, phân trang, lọc nâng cao, giải quyết triệt để N+1 query và sắp xếp động.
- **Số lượng test**: 11 tests.

| STT | Tên Test Method | Kịch bản kiểm thử | Kỳ vọng đầu ra |
|---|---|---|---|
| 1 | `testSearchProjects_EmptyCriteria_ReturnsAll` | Tìm kiếm với criteria rỗng | Trả về toàn bộ 6 projects mẫu khởi tạo từ script data, mặc định sắp xếp theo `projectNumber ASC`. |
| 2 | `testSearchProjects_ByKeyword_Number` | Keyword là số "1003" | Khớp chính xác `projectNumber = 1003`, trả về dự án "CRYSTAL BALL". |
| 3 | `testSearchProjects_ByKeyword_Name` | Keyword là chuỗi chữ "EFV" | Khớp chứa chuỗi `name` không phân biệt hoa thường. |
| 4 | `testSearchProjects_ByKeyword_Customer` | Keyword theo khách hàng "Customer B" | Tìm đúng 2 dự án thuộc khách hàng tương ứng. |
| 5 | `testSearchProjects_ByStatus` | Lọc theo `ProjectStatus.NEW` | Trả về đúng 2 dự án có status `NEW`. |
| 6 | `testSearchProjects_ByLeaderVisa` | Lọc nâng cao theo visa trưởng nhóm "DTH" | Join qua `Group` và `Employee` leader, trả về chính xác 2 dự án. |
| 7 | `testSearchProjects_ByMemberVisa` | Lọc nâng cao theo visa thành viên "HTV" | Kiểm tra quan hệ Many-to-Many `employees`, trả về 2 dự án có thành viên tham gia. |
| 8 | `testSearchProjects_ByStartDateRange` | Lọc theo khoảng ngày `startDateFrom` và `startDateTo` | Trả về 3 dự án có ngày bắt đầu nằm trong năm 2025. |
| 9 | `testSearchProjects_Pagination` | Phân trang kích thước 2 bản ghi/trang (`PageRequest.of(0, 2)`) | Trả về đúng `totalPages = 3`, `totalElements = 6`, kích thước content trang 1 là 2. |
| 10 | `testFindDetailById` | Lấy chi tiết dự án qua EntityGraph `findDetailById(1L)` | Fetch eager `Group`, `GroupLeader`, `Employees` chỉ trong 1 câu truy vấn, loại bỏ hoàn toàn bài toán N+1 query. |
| 11 | `testSearchProjects_DynamicSorting_MultiColumn` | Sắp xếp động đa cột: `status DESC, projectNumber ASC` | QueryDSL sinh đúng `ORDER BY status DESC, projectNumber ASC`, trả về kết quả chuẩn xác. |

---

### 2.4. Criteria / QueryDSL Layer: [`SearchProjectCriteriaTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/model/dto/request/SearchProjectCriteriaTest.java)
- **Mục đích**: Kiểm tra đơn vị logic xây dựng biểu thức vị từ (QueryDSL `BooleanBuilder` / `Predicate`) trong [`SearchProjectCriteria.toPredicate()`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/request/SearchProjectCriteria.java).
- **Số lượng test**: 7 tests.

| STT | Tên Test Method | Kịch bản kiểm thử | Kỳ vọng đầu ra |
|---|---|---|---|
| 1 | `testToPredicate_EmptyCriteria` | Tiêu chí rỗng không có điều kiện nào | Sinh Predicate hợp lệ, không ném NullPointerException. |
| 2 | `testToPredicate_TextKeyword` | Keyword dạng chuỗi chữ (không parse được sang int) | Sinh điều kiện OR tìm kiếm trên cả 2 trường `project.name` và `project.customer`. |
| 3 | `testToPredicate_NumericKeyword` | Keyword dạng số "1001" | Sinh điều kiện tìm kiếm bao quát cả `project.projectNumber = 1001` lẫn `name` và `customer`. |
| 4 | `testToPredicate_StatusFilter` | Tiêu chí có trạng thái `INP` | Sinh biểu thức `project.status = INP`. |
| 5 | `testToPredicate_LeaderVisaFilter` | Tiêu chí có trưởng nhóm `leaderVisa = "DTH"` | Sinh biểu thức lọc trên `group.groupLeader.visa`. |
| 6 | `testToPredicate_MemberVisaFilter` | Tiêu chí có thành viên `memberVisa = "BHU"` | Sinh biểu thức lọc trên tập hợp `any(project.employees).visa`. |
| 7 | `testToPredicate_DateRangeFilters` | Tiêu chí có khoảng ngày `startDate` và `endDate` | Sinh đầy đủ các biểu thức so sánh `>=` và `<=`. |

---

### 2.5. Database Indexing & Execution Plan Layer: [`ProjectExecutionPlanTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/repository/ProjectExecutionPlanTest.java)
- **Mục đích**: Đo lường và phân tích kế hoạch thực thi câu lệnh SQL trực tiếp trong cơ sở dữ liệu bằng `EXPLAIN ANALYZE`, kiểm tra xem chỉ mục (Indexes) có thực sự được kích hoạt (Index Scan / Index Lookup) hay bị rơi vào quét toàn bảng (Full Table Scan), đồng thời đối chiếu với câu lệnh Hibernate thực tế sinh ra tại runtime qua `SqlCaptureInspector`.
- **Số lượng test**: 8 tests.

| STT | Tên Test Method | Kịch bản kiểm thử | Đánh giá Kế Hoạch Thực Thi (Execution Plan) |
|---|---|---|---|
| 1 | `testExplainAnalyze_FindProjectDetailById` | Chi tiết dự án JOIN nhiều bảng theo ID | Tận dụng chỉ mục khóa chính `PRIMARY_KEY` trên các bảng `PROJECT`, `GROUP`, `EMPLOYEE`, `PROJECT_EMPLOYEE`. |
| 2 | `testExplainAnalyze_SearchProjectsByStatusAndCustomer` | Tìm theo trạng thái và khách hàng | Tận dụng chỉ mục tổng hợp `idx_project_status_customer` trên bảng `PROJECT`. |
| 3 | `testExplainAnalyze_SearchProjectsByMemberVisa` | Tìm dự án theo VISA thành viên tham gia | Sử dụng chỉ mục khóa chính trên bảng liên kết `PROJECT_EMPLOYEE` và chỉ mục `idx_employee_visa`. |
| 4 | `testExplainAnalyze_FindEmployeeByVisa` | Tìm nhân viên theo mã định danh VISA | Sử dụng chỉ mục duy nhất `idx_employee_visa` (`INDEX (VISA)`), tra cứu cực nhanh độ phức tạp $O(1) \sim O(\log N)$. |
| 5 | `testExplainAnalyze_FullTableScan_UnindexedColumn` | Lọc dự án theo cột `START_DATE` (chưa đánh chỉ mục) | Nhận diện kế hoạch `tableScan` để phân tích tác động hiệu năng khi bảng dữ liệu lớn. |
| 6 | `testExplainAnalyze_FullTableScan_LeadingWildcard` | Tìm kiếm tên có wildcard phía trước (`%ball%`) | Nhận diện hiện tượng B-Tree index không thể sử dụng khi có wildcard đầu chuỗi, dẫn tới `tableScan`. |
| 7 | `testExplainAnalyze_ActualHibernateQuery_FindDetailById` | Bắt câu SQL do Hibernate sinh ra từ `findDetailById(1L)` và chạy `EXPLAIN ANALYZE` | Xác thực Hibernate chỉ sinh đúng 1 câu lệnh SQL duy nhất và thực thi tối ưu qua index khóa chính. |
| 8 | `testExplainAnalyze_ActualHibernateQuery_FullTableScan_FindByNameContaining` | Bắt câu SQL do Spring Data JPA sinh ra từ `findByNameContainingIgnoreCase` | Đo lường chính xác kế hoạch thực thi câu lệnh truy vấn tìm kiếm LIKE của framework. |

---

### 2.6. Bean Validation Layer: Custom Validators
- **Mục đích**: Đảm bảo toàn bộ các annotation kiểm tra tính hợp lệ dữ liệu tuân thủ chuẩn JSR-380 / Hibernate Validator, bắt lỗi ngay tại tầng tiếp nhận dữ liệu trước khi đi vào xử lý nghiệp vụ.
- **Tổng số test**: 15 tests (chia đều trên 3 validator classes).

#### A. [`StartBeforeEndDateValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/StartBeforeEndDateValidatorTest.java) (5 tests)
Kiểm tra tính hợp lệ của cặp ngày bắt đầu và kết thúc (`@StartBeforeEndDate`):
1. `testValid_StartDateBeforeEndDate`: Ngày bắt đầu trước ngày kết thúc -> Hợp lệ (`true`).
2. `testInvalid_StartDateAfterEndDate`: Ngày bắt đầu sau ngày kết thúc -> Bị từ chối (`false`).
3. `testEqualDates_WhenAllowEqualTrue_IsValid`: Ngày bắt đầu trùng ngày kết thúc khi cấu hình `allowEqual = true` -> Hợp lệ (`true`).
4. `testEqualDates_WhenAllowEqualFalse_IsInvalid`: Ngày bắt đầu trùng ngày kết thúc khi cấu hình `allowEqual = false` -> Bị từ chối (`false`).
5. `testValid_NullDates`: Xử lý an toàn khi một hoặc cả hai ngày là null -> Không ném exception, hợp lệ (`true`).

#### B. [`VisasValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/VisasValidatorTest.java) (5 tests)
Kiểm tra danh sách các mã VISA nhân viên (`@Visas`):
1. `testValidVisas_AllUppercaseThreeLetters`: Danh sách các mã đúng 3 ký tự viết hoa (ví dụ: `["DTH", "BHU", "JHV"]`) -> Hợp lệ (`true`).
2. `testValidVisas_NullOrEmpty`: Danh sách rỗng hoặc null -> Hợp lệ (`true`).
3. `testInvalidVisas_ContainsLowercase`: Danh sách chứa ký tự viết thường (ví dụ: `["DTH", "bhu"]`) -> Bị từ chối (`false`).
4. `testInvalidVisas_ContainsInvalidLength`: Danh sách chứa mã không đúng độ dài 3 ký tự (ví dụ: `"LONGVISA"`, `"NO"`) -> Bị từ chối (`false`).
5. `testInvalidVisas_ContainsNull`: Danh sách chứa phần tử null -> Bị từ chối (`false`).

#### C. [`VisaValidatorTest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/validator/VisaValidatorTest.java) (5 tests)
Kiểm tra một mã định danh VISA đơn lẻ (`@Visa`):
1. `testValidVisa_ExactlyThreeUppercaseLetters`: Mã đúng 3 chữ cái in hoa (`"DTH"`, `"BHU"`, `"XYZ"`) -> Hợp lệ (`true`).
2. `testValidVisa_NullOrEmpty`: Chuỗi rỗng, null hoặc chỉ có khoảng trắng -> Hợp lệ (`true`).
3. `testInvalidVisa_Lowercase`: Chứa chữ thường (`"dth"`, `"DTh"`) -> Bị từ chối (`false`).
4. `testInvalidVisa_LengthNotThree`: Độ dài khác 3 ký tự (`"D"`, `"DT"`, `"DTHX"`) -> Bị từ chối (`false`).
5. `testInvalidVisa_ContainsNumbersOrSpecialChars`: Chứa số hoặc ký tự đặc biệt (`"DT1"`, `"123"`, `"D-T"`) -> Bị từ chối (`false`).

---

## 3. Công Cụ Hỗ Trợ Chẩn Đoán Kiểm Thử (Test Utilities)

1. **[`SqlCaptureInspector`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/test/java/vn/elca/training/util/SqlCaptureInspector.java)**:
   - Triển khai interface `StatementInspector` của Hibernate Core.
   - Cho phép bắt (intercept) toàn bộ câu lệnh SQL nguyên bản do Hibernate tạo ra trong suốt vòng đời của 1 transaction kiểm thử mà không phụ thuộc vào log file.
   - Cung cấp các hàm tiện ích: `getQueries()`, `getLastQuery()`, `clear()`.

2. **Cấu hình P6Spy (`spy.properties`)**:
   - Ghi lại vết truy vấn database với thời gian thực thi (execution time tính theo mili-giây) và câu SQL hoàn chỉnh đã được điền sẵn giá trị tham số (bound parameters), hỗ trợ đắc lực cho việc kiểm thử `EXPLAIN ANALYZE`.

---

## 4. Ma Trận Truy Xuất Yêu Cầu Nghiệp Vụ (Requirements Traceability Matrix)

| Yêu cầu nghiệp vụ (PIMTOOL_REQUIREMENTS.md) | Test Cases đảm bảo kiểm thử |
|---|---|
| **Tìm kiếm cơ bản (Keyword & Status)** | `ProjectControllerTest.testSearchProjects_WithKeyword`<br>`ProjectControllerTest.testSearchProjects_WithStatus_Returns200`<br>`ProjectRepositoryTest.testSearchProjects_ByKeyword_Number`<br>`ProjectRepositoryTest.testSearchProjects_ByKeyword_Name`<br>`ProjectRepositoryTest.testSearchProjects_ByKeyword_Customer`<br>`ProjectRepositoryTest.testSearchProjects_ByStatus`<br>`SearchProjectCriteriaTest.testToPredicate_TextKeyword`<br>`SearchProjectCriteriaTest.testToPredicate_NumericKeyword` |
| **Tìm kiếm nâng cao (Leader Visa, Member Visa, Date Ranges)** | `ProjectControllerTest.testSearchProjects_InvalidLeaderVisa_Returns400`<br>`ProjectControllerTest.testSearchProjects_InvalidStartDateRange_Returns400`<br>`ProjectControllerTest.testSearchProjects_InvalidEndDateRange_Returns400`<br>`ProjectRepositoryTest.testSearchProjects_ByLeaderVisa`<br>`ProjectRepositoryTest.testSearchProjects_ByMemberVisa`<br>`ProjectRepositoryTest.testSearchProjects_ByStartDateRange`<br>`SearchProjectCriteriaTest.testToPredicate_LeaderVisaFilter`<br>`SearchProjectCriteriaTest.testToPredicate_MemberVisaFilter`<br>`SearchProjectCriteriaTest.testToPredicate_DateRangeFilters` |
| **Xóa một hoặc nhiều dự án (Chỉ cho phép xóa status NEW)** | `ProjectControllerTest.testDeleteProject_Success_Returns204`<br>`ProjectControllerTest.testDeleteProjects_Success_Returns204`<br>`ProjectControllerTest.testDeleteProject_NotFound_Returns404`<br>`ProjectControllerTest.testDeleteProject_InvalidStatus_Returns400`<br>`ProjectServiceTest.testDeleteProjects_Success`<br>`ProjectServiceTest.testDeleteProjects_NotFound_ThrowsException`<br>`ProjectServiceTest.testDeleteProjects_InvalidStatus_ThrowsException`<br>`ProjectServiceTest.testDeleteProject_Success` |
| **Ngăn chặn xung đột đồng thời (Optimistic Locking)** | `ProjectServiceTest.testUpdateProject_OptimisticLockingFailure_ThrowsException`<br>`ProjectServiceTest.testUpdateProject_Success` |
| **Trùng lặp số thứ tự dự án (Unique Project Number)** | `ProjectServiceTest.testCreateProject_DuplicateNumber_ThrowsException` |
| **Giải quyết bài toán N+1 Query (EntityGraph & Fetch Join)** | `ProjectRepositoryTest.testFindDetailById`<br>`ProjectExecutionPlanTest.testExplainAnalyze_FindProjectDetailById`<br>`ProjectExecutionPlanTest.testExplainAnalyze_ActualHibernateQuery_FindDetailById` |
| **Sắp xếp động đa cột (Dynamic Multi-column Sorting)** | `ProjectRepositoryTest.testSearchProjects_DynamicSorting_MultiColumn` |
| **Đánh giá hiệu năng và chỉ mục (Database Index Scan)** | `ProjectExecutionPlanTest.testExplainAnalyze_SearchProjectsByStatusAndCustomer`<br>`ProjectExecutionPlanTest.testExplainAnalyze_FindEmployeeByVisa` |

---

## 5. Hướng Dẫn Chạy Kiểm Thử (How to Run Tests)

```bash
# Chạy toàn bộ 64 bài test
mvn test

# Chạy một lớp test cụ thể
mvn test -Dtest=ProjectRepositoryTest

# Chạy một test method cụ thể
mvn test -Dtest=ProjectRepositoryTest#testSearchProjects_DynamicSorting_MultiColumn

# Chạy kiểm thử kế hoạch thực thi EXPLAIN ANALYZE
mvn test -Dtest=ProjectExecutionPlanTest
```
