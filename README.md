# Project Information Management (PIM) Tool - Backend

Hệ thống quản lý thông tin dự án (**PIM Tool Backend**) được xây dựng trên nền tảng **Spring Boot**, **Hibernate/JPA**, và **QueryDSL**, tuân thủ các tiêu chuẩn kỹ thuật nghiêm ngặt về hiệu năng truy vấn, toàn vẹn dữ liệu, kiểm soát đồng thời (Optimistic Locking) và kiểm thử tự động toàn diện (**97.6% Line Coverage**, **147/147 tests passed**).

---

## 🚀 Công Nghệ Sử Dụng (Tech Stack)

| Thành phần | Phiên bản / Thư viện | Mục đích sử dụng |
|---|---|---|
| **Core Framework** | Spring Boot `2.6.6` | Web MVC, Dependency Injection, Transaction Management |
| **ORM / Persistence** | Hibernate `5.6.7.Final` / JPA | Quản lý vòng đời thực thể, ánh xạ quan hệ ORM |
| **Type-Safe Queries** | QueryDSL `5.0.0` | Xây dựng câu truy vấn động, an toàn kiểu dữ liệu |
| **SQL Profiling** | P6Spy `3.9.1` + Custom Formatter | Bắt và định dạng câu lệnh SQL runtime, phát hiện N+1 |
| **Database Engine** | H2 Database `1.4.200` | Cơ sở dữ liệu in-memory phục vụ Dev & Test tự động |
| **Mapping & Utils** | ModelMapper `3.1.0`, Lombok | Ánh xạ DTO - Entity, giảm boilerplate code |
| **Testing** | JUnit 4, Mockito, MockMvc, Jacoco | Kiểm thử đơn vị, kiểm thử tích hợp, đo lường độ phủ tự động |
| **Containerization** | Docker, Multi-stage build | Đóng gói container Java 11 siêu nhẹ, triển khai Render/Cloud |

---

## 📁 Cấu Trúc Thư Mục Tài Liệu (`docs/`) & Agent Ecosystem (`.agents/`)

Toàn bộ tài liệu kỹ thuật, đặc tả yêu cầu, báo cáo bài tập và tài liệu lý thuyết được tổ chức quy củ vào thư mục `docs/`:

```
pilot-project-back/
├── .agents/                    # Hệ sinh thái Subagents & Specialized Skills
│   ├── agents/
│   │   ├── backend_architect/agent.md
│   │   └── qa_coverage_engineer/agent.md
│   └── skills/
│       ├── elca-coding-standards/SKILL.md
│       ├── full-coverage-testing/SKILL.md
│       ├── hibernate-performance-tuning/SKILL.md
│       └── rest-api-architecture-and-specs/SKILL.md
├── docs/
│   ├── workflow/               # Tiêu chuẩn quy trình phát triển & Kế hoạch kiểm thử
│   │   ├── DEVELOPMENT_WORKFLOW.md
│   │   └── PLAN_FULL_COVERAGE_TESTING.md
│   ├── specifications/         # Đặc tả API và Yêu cầu nghiệp vụ dự án
│   │   ├── API_SPECIFICATION.md
│   │   ├── BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md
│   │   ├── PIMTOOL_REQUIREMENTS.md
│   │   ├── On-boarding-PIMTool-Requirements-v4-Exercise.docx
│   │   └── pilot-project-back.postman_collection.json
│   ├── reports/                # Báo cáo thực hiện bài tập & Tổng kết test suite
│   │   ├── TEST_SUITE_SUMMARY.md
│   │   ├── BAO_CAO_THUC_HIEN.md
│   │   ├── BAO_CAO_JAVA_06_HIBERNATE.md
│   │   └── BAO_CAO_BAI_TAP_TRUOC_SPRING_MVC.md
│   ├── guides/                 # Hướng dẫn kỹ thuật chuyên sâu & Phân tích TOCTOU
│   │   ├── GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md
│   │   ├── HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md
│   │   └── HUONG_DAN_TU_CODE_TAY_JAVA_06.md
│   └── theory/                 # Tài liệu lý thuyết nền tảng Spring & Hibernate
│       ├── CORE_THEORY_AND_ANNOTATIONS.md
│       ├── TAI_LIEU_LY_THUYET_HIBERNATE_17.md
│       └── originals/          # File thuyết trình, slides & bài tập gốc
│           ├── Basic knowhow Concept - AppSecurity-16 - original.pptx
│           ├── Basic knowhow Concepts - ConcurrentUpdate-Exercise-13 - original 1.pptx
│           ├── Basic knowhow Concepts - TechnicalId-Exercise-13 - original 1.pptx
│           ├── Basic knowhow SQL-Exercise-13 - original 1.pdf
│           └── HIBERNATE-17.doc
└── src/                        # Mã nguồn ứng dụng và bộ test suites
```

### Chi Tiết Từng Phân Hệ:

#### 1. Hệ Sinh Thái Agent & Kỹ Năng Kỹ Thuật ([.agents/](.agents/))
- **`backend_architect`**: Kiến trúc sư backend Spring Boot, giải quyết bài toán concurrency control (`@Version`), indexing, clean architecture.
- **`qa_coverage_engineer`**: Kỹ sư kiểm thử tự động, phụ trách tối ưu branch coverage và loại bỏ flaky tests.
- **`elca-coding-standards`**: Tiêu chuẩn lập trình theo *ELCA Coding Best Practices v1.16* (Mục 2: General programming, Mục 3: Exception handling, Mục 5: Architecture & layering, Mục 9: Persistence & concurrency).
- **`hibernate-performance-tuning`**: Tiêu chuẩn triệt tiêu N+1 query, cấu hình batch fetching, EntityGraph và profiling P6Spy.
- **`rest-api-architecture-and-specs`**: Quy chuẩn thiết kế REST API, mã HTTP và định dạng `ErrorResponse` thống nhất.
- **`full-coverage-testing`**: Quy trình tự động hóa kiểm thử đa tầng JUnit 4 / Mockito / MockMvc.

#### 2. Đặc Tả Nghiệp Vụ & API ([docs/specifications/](docs/specifications/))
- [API_SPECIFICATION.md](docs/specifications/API_SPECIFICATION.md): Đặc tả chi tiết các RESTful API endpoints (`/projects`, `/employees`, `/groups`, `/ping`, Exception payload).
- [BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md](docs/specifications/BACKEND_EXPECTED_RESPONSES_MANUAL_TESTS.md): Kết quả kỳ vọng phản hồi Backend (HTTP method, payload, query SQL Hibernate/QueryDSL, status và JSON response) cho 32 test cases kiểm thử thủ công.
- [PIMTOOL_REQUIREMENTS.md](docs/specifications/PIMTOOL_REQUIREMENTS.md): Yêu cầu nghiệp vụ chi tiết của hệ thống quản lý dự án PIM Tool.

#### 3. Báo Cáo Kiểm Thử & Đo Lường Độ Phủ ([docs/reports/](docs/reports/))
- [TEST_SUITE_SUMMARY.md](docs/reports/TEST_SUITE_SUMMARY.md): Báo cáo chi tiết **147 test cases** trên **18 test classes**, đạt **97.6% Line Coverage** và **87.5% Branch Coverage**.
- [BAO_CAO_THUC_HIEN.md](docs/reports/BAO_CAO_THUC_HIEN.md): Báo cáo tiến độ và chi tiết quá trình hiện thực hóa dự án.

#### 4. Hướng Dẫn Kỹ Thuật & Tài Liệu Lý Thuyết ([docs/guides/](docs/guides/) & [docs/theory/](docs/theory/))
- [GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md](docs/guides/GLOBAL_EXCEPTION_HANDLER_DEEP_DIVE.md): Phân tích cơ chế xử lý ngoại lệ tập trung với `@ControllerAdvice`.
- [HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md](docs/guides/HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md): Hướng dẫn tái hiện và phòng chống lỗi tranh chấp dữ liệu TOCTOU (Time-of-Check to Time-of-Use) bằng Optimistic Locking.
- [CORE_THEORY_AND_ANNOTATIONS.md](docs/theory/CORE_THEORY_AND_ANNOTATIONS.md): Bách khoa toàn thư về các Annotation, vòng đời Entity, Cascade, FetchType trong Spring Boot & Hibernate.
- [originals/](docs/theory/originals/): Lưu trữ an toàn các slide và tài liệu bài giảng gốc của ELCA.

---

## ⚡ Hướng Dẫn Chạy Dự Án & Kiểm Thử

### Yêu Cầu Môi Trường
- **Java:** JDK 11 hoặc JDK 17
- **Maven:** 3.6+
- **Docker:** (Tùy chọn) để đóng gói và deploy container

### 1. Chạy Toàn Bộ Test Suite & Sinh Báo Cáo Jacoco
```bash
mvn clean test jacoco:report
```
*Kết quả:* **147 tests run, 0 failures, 0 errors, 0 skipped**. File báo cáo HTML tại `target/site/jacoco/index.html`.

### 2. Chạy Ứng Dụng (Spring Boot Local)
```bash
mvn spring-boot:run
```
- Server khởi chạy tại: `http://localhost:8080`
- Heartbeat / Health Check: `GET http://localhost:8080/ping` $\rightarrow$ `"pong"`
- H2 Console: `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:testdb`, User: `sa`, Password: *(để trống)*)

### 3. Đóng Gói & Chạy Bằng Docker
Dự án được tích hợp sẵn `Dockerfile` đa tầng (Multi-stage build) tối ưu bộ nhớ:
```bash
# Build Docker image
docker build -t pim-tool-backend .

# Chạy container
docker run -p 8080:8080 pim-tool-backend
```

---

## 🌐 Triển Khai Lên Render (Deployment Guide)

Backend được cấu hình tương thích hoàn toàn để deploy lên nền tảng **Render** qua Docker:
1. **Port động:** Đã cấu hình `server.port=${PORT:8080}` trong `application.properties` để tự động nhận cổng ngẫu nhiên từ Render.
2. **CORS:** Đã kích hoạt `CorsConfig` cho phép frontend gọi API mà không bị chặn trình duyệt.
3. **Kiểm soát RAM:** `Dockerfile` giới hạn RAM an toàn `-Xmx350m -Xms256m`, hoạt động mượt mà trong gói Free (512MB RAM) của Render mà không bị OOM Kill.
4. **Health Check URL:** Sử dụng endpoint `/ping` để cấu hình Health Check Path trên Dashboard của Render.
