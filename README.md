# Project Information Management (PIM) Tool - Backend

Hệ thống quản lý thông tin dự án (**PIM Tool Backend**) được xây dựng trên nền tảng **Spring Boot**, **Hibernate/JPA**, và **QueryDSL**, tuân thủ các tiêu chuẩn kỹ thuật nghiêm ngặt về hiệu năng truy vấn, toàn vẹn dữ liệu, và kiểm thử tự động.

---

## 🚀 Công Nghệ Sử Dụng (Tech Stack)

| Thành phần | Phiên bản / Thư viện | Mục đích sử dụng |
|---|---|---|
| **Core Framework** | Spring Boot `2.6.6` | Web MVC, Dependency Injection, Transaction Management |
| **ORM / Persistence** | Hibernate `5.6.7.Final` / JPA | Quản lý vòng đời thực thể, ánh xạ ORM quan hệ |
| **Type-Safe Queries** | QueryDSL `5.0.0` | Xây dựng câu truy vấn động, an toàn kiểu dữ liệu |
| **SQL Profiling** | P6Spy `3.9.1` + Custom Formatter | Bắt và định dạng câu lệnh SQL thực thi, phát hiện N+1 |
| **Database Engine** | H2 Database `1.4.200` | Cơ sở dữ liệu in-memory phục vụ Dev & Test tự động |
| **Mapping & Utils** | ModelMapper `3.1.0`, Lombok | Ánh xạ DTO - Entity, giảm thiểu boilerplate code |
| **Testing** | JUnit 4, Mockito, MockMvc, AssertJ | Kiểm thử đơn vị, kiểm thử tích hợp, assert execution plan |

---

## 📁 Cấu Trúc Thư Mục Tài Liệu (`docs/`)

Toàn bộ tài liệu kỹ thuật, đặc tả yêu cầu, báo cáo bài tập và tài liệu lý thuyết được tổ chức quy củ vào thư mục `docs/`:

```
docs/
├── workflow/               # Tiêu chuẩn quy trình phát triển & cộng tác Agentic
│   └── DEVELOPMENT_WORKFLOW.md
├── specifications/         # Đặc tả API và Yêu cầu nghiệp vụ dự án
│   ├── API_SPECIFICATION.md
│   ├── BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md
│   └── PIMTOOL_REQUIREMENTS.md
├── reports/                # Báo cáo thực hiện bài tập & Tổng kết test suite
│   ├── BAO_CAO_BAI_TAP_TRUOC_SPRING_MVC.md
│   ├── BAO_CAO_JAVA_06_HIBERNATE.md
│   ├── BAO_CAO_THUC_HIEN.md
│   └── TEST_SUITE_SUMMARY.md
├── guides/                 # Hướng dẫn kỹ thuật chuyên sâu & Hướng dẫn tự code
│   ├── GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md
│   ├── HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md
│   └── HUONG_DAN_TU_CODE_TAY_JAVA_06.md
└── theory/                 # Tài liệu lý thuyết nền tảng Spring & Hibernate
    ├── CORE_THEORY_AND_ANNOTATIONS.md
    └── TAI_LIEU_LY_THUYET_HIBERNATE_17.md
```

### Chi Tiết Từng Phân Hệ Tài Liệu:

#### 1. Quy Trình & Tiêu Chuẩn Phát Triển ([docs/workflow/](docs/workflow/))
- [DEVELOPMENT_WORKFLOW.md](docs/workflow/DEVELOPMENT_WORKFLOW.md): Khung quy trình chuẩn 5 bước (**Plan -> Acceptance Criteria -> Test -> UAT -> Tracking Progress**), ma trận kỹ năng của AI Agent và Developer, tiêu chuẩn Conventional Commits.
- [PLAN_FULL_COVERAGE_TESTING.md](docs/workflow/PLAN_FULL_COVERAGE_TESTING.md): Kế hoạch và tiêu chuẩn nghiệm thu độ phủ kiểm thử toàn diện > 90%.

#### 2. Đặc Tả Nghiệp Vụ & API ([docs/specifications/](docs/specifications/))
- [API_SPECIFICATION.md](docs/specifications/API_SPECIFICATION.md): Đặc tả chi tiết các RESTful API endpoints (Project, Employee, Group, Exception payload).
- [BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md](docs/specifications/BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md): Kết quả kỳ vọng phản hồi Backend (HTTP method, payload, query SQL Hibernate/QueryDSL, status và JSON response) cho toàn bộ 32 test cases kiểm thử thủ công.
- [PIMTOOL_REQUIREMENTS.md](docs/specifications/PIMTOOL_REQUIREMENTS.md): Yêu cầu nghiệp vụ chi tiết của hệ thống quản lý dự án PIM Tool.

#### 3. Báo Cáo Thực Hiện & Báo Cáo Kiểm Thử ([docs/reports/](docs/reports/))
- [TEST_SUITE_SUMMARY.md](docs/reports/TEST_SUITE_SUMMARY.md): Báo cáo tổng hợp toàn bộ các bộ kiểm thử đa tầng (Tháp kiểm thử từ Unit Test, Repository Test đến Execution Plan Test).
- [BAO_CAO_THUC_HIEN.md](docs/reports/BAO_CAO_THUC_HIEN.md): Báo cáo tiến độ và chi tiết quá trình hiện thực hóa dự án.
- [BAO_CAO_JAVA_06_HIBERNATE.md](docs/reports/BAO_CAO_JAVA_06_HIBERNATE.md): Báo cáo chuyên đề bài tập Java 06 Hibernate.
- [BAO_CAO_BAI_TAP_TRUOC_SPRING_MVC.md](docs/reports/BAO_CAO_BAI_TAP_TRUOC_SPRING_MVC.md): Báo cáo bài tập giai đoạn trước Spring MVC.

#### 4. Hướng Dẫn Kỹ Thuật Chuyên Sâu ([docs/guides/](docs/guides/))
- [GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md](docs/guides/GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md): Phân tích sâu về cơ chế xử lý ngoại lệ tập trung với `@ControllerAdvice` và format lỗi batch delete.
- [HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md](docs/guides/HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md): Hướng dẫn tái hiện và phòng chống lỗi tranh chấp dữ liệu TOCTOU (Time-of-Check to Time-of-Use) bằng Optimistic Locking.
- [HUONG_DAN_TU_CODE_TAY_JAVA_06.md](docs/guides/HUONG_DAN_TU_CODE_TAY_JAVA_06.md): Hướng dẫn từng bước tự lập trình bài tập Java 06.

#### 5. Lý Thuyết Nền Tảng ([docs/theory/](docs/theory/))
- [CORE_THEORY_AND_ANNOTATIONS.md](docs/theory/CORE_THEORY_AND_ANNOTATIONS.md): Bách khoa toàn thư về các Annotation, vòng đời Entity, Cascade, FetchType trong Spring Boot & Hibernate.
- [TAI_LIEU_LY_THUYET_HIBERNATE_17.md](docs/theory/TAI_LIEU_LY_THUYET_HIBERNATE_17.md): 17 câu hỏi & chủ đề lý thuyết chuyên sâu thường gặp về Hibernate và tối ưu hóa ORM.

---

## 🛠️ Quy Trình Làm Việc Tiêu Chuẩn (Engineering Workflow)

Dự án áp dụng chặt chẽ quy trình 5 bước:

```
[PLAN] ──> [ACCEPTANCE CRITERIA] ──> [TEST] ──> [UAT & VERIFY] ──> [PROGRESS TRACKING]
```

1. **Plan:** Phân tích yêu cầu, khảo sát tác động DB Schema / Hibernate mapping, vạch ra các bước thực hiện.
2. **Acceptance Criteria (AC):** Xác định rõ tiêu chí hoàn thành (Functional, Non-Functional, Query count limit, Zero N+1).
3. **Test:** Viết test đa tầng (Unit Test, Integration Test, QueryDSL assertion, SQL capture).
4. **UAT & Verification:** Kiểm tra thực tế trên logs P6Spy, kiểm tra HTTP response body & status code.
5. **Progress Tracking:** Quản lý vòng đời trạng thái (`[BACKLOG]` -> `[IN_PROGRESS]` -> `[TESTING]` -> `[DONE]`), commit theo chuẩn Conventional Commits.

---

## ⚡ Hướng Dẫn Chạy Dự Án & Kiểm Thử

### Yêu Cầu Môi Trường
- **Java:** JDK 11 hoặc JDK 17
- **Maven:** 3.6+

### 1. Chạy Toàn Bộ Test Suite (Automated Testing)
```bash
mvn clean test
```
*Kết quả mong đợi:* Tất cả các test cases đều đạt trạng thái `BUILD SUCCESS` (0 failures, 0 errors).

### 2. Chạy Ứng Dụng (Spring Boot Run)
```bash
mvn spring-boot:run
```
- Server khởi chạy tại: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: *(để trống)*)

### 3. Giám Sát SQL Query với P6Spy
Dự án tích hợp bộ định dạng SQL tùy chỉnh (`P6SpySqlFormatter`) giúp format đẹp mắt và tô sáng các câu lệnh SQL runtime trong console:
```text
=== [P6Spy SQL Execution (2ms)] ===
SELECT
    project0_.ID,
    project0_.PROJECT_NUMBER,
    project0_.NAME
FROM
    PROJECT project0_
WHERE
    project0_.STATUS = 'NEW'
ORDER BY
    project0_.PROJECT_NUMBER ASC
LIMIT 5;
====================================
```
