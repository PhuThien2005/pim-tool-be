# Cẩm Nang Lý Thuyết Chuyên Sâu & Tra Cứu Toàn Bộ Annotation (pilot-project-back)

Tài liệu này tổng hợp toàn bộ các điểm lý thuyết cốt lõi (Core Architectural Points) và bảng tra cứu chuyên sâu các Annotation được sử dụng trong dự án backend `pilot-project-back`.

---

## MỤC LỤC

1. [Các Điểm Lý Thuyết Cốt Lõi Về Kiến Trúc & Thiết Kế Hệ Thống](#1-các-điểm-lý-thuyết-cốt-lõi-về-kiến-trúc--thiết-kế-hệ-thống)
   - [1.1. Kiến Trúc 3 Tầng (3-Tier Layered Architecture) & Phân Tách Trách Nhiệm (SoC)](#11-kiến-trúc-3-tầng-3-tier-layered-architecture--phân-tách-trách-nhiệm-soc)
   - [1.2. Mẫu DTO & Tại Sao Không Được Expose JPA Entity Ra Controller?](#12-mẫu-dto--tại-sao-không-được-expose-jpa-entity-ra-controller)
   - [1.3. Bài Toán N+1 Query & Giải Pháp `@EntityGraph` vs `FETCH JOIN`](#13-bài-toán-n1-query--giải-pháp-entitygraph-vs-fetch-join)
   - [1.4. Kiểm Soát Cạnh Tranh Đồng Thời & Khóa Lạc Quan (Optimistic Locking)](#14-kiểm-soát-cạnh-tranh-đồng-thời--khóa-lạc-quan-optimistic-locking)
   - [1.5. Tối Ưu Phân Trang: `Page<T>` vs `Slice<T>`](#15-tối-ưu-phân-trang-paget-vs-slicet)
   - [1.6. Cơ Sở Dữ Liệu: Chỉ Mục B-Tree & Phân Tích Kế Hoạch Thực Thi (EXPLAIN ANALYZE)](#16-cơ-sở-dữ-liệu-chỉ-mục-b-tree--phân-tích-kế-hoạch-thực-thi-explain-analyze)
   - [1.7. Nguyên Tắc Thiết Kế RESTful API Chuẩn Mực](#17-nguyên-tắc-thiết-kế-restful-api-chuẩn-mực)
   - [1.8. Cơ Chế Xử Lý Lỗi Tập Trung (Global Exception Handling)](#18-cơ-chế-xử-lý-lỗi-tập-trung-global-exception-handling)
   - [1.9. Cơ Chế Kiểm Tra Ràng Buộc Dữ Liệu (Bean Validation JSR-380)](#19-cơ-chế-kiểm-tra-ràng-buộc-dữ-liệu-bean-validation-jsr-380)
2. [Bảng Tra Cứu Toàn Diện Các Annotation Sử Dụng Trong Dự Án](#2-bảng-tra-cứu-toàn-diện-các-annotation-sử-dụng-trong-dự-án)
   - [2.1. Nhóm Spring Core & Spring Boot](#21-nhóm-spring-core--spring-boot)
   - [2.2. Nhóm Spring Web MVC (REST Controller)](#22-nhóm-spring-web-mvc-rest-controller)
   - [2.3. Nhóm Spring Data JPA & Transaction Management](#23-nhóm-spring-data-jpa--transaction-management)
   - [2.4. Nhóm JPA / Hibernate ORM (Entity Mapping)](#24-nhóm-jpa--hibernate-orm-entity-mapping)
   - [2.5. Nhóm Project Lombok](#25-nhóm-project-lombok)
   - [2.6. Nhóm Bean Validation (JSR-380 & Custom Annotations)](#26-nhóm-bean-validation-jsr-380--custom-annotations)
   - [2.7. Nhóm Testing (JUnit, Mockito, SpringBootTest)](#27-nhóm-testing-junit-mockito-springboottest)

---

## 1. CÁC ĐIỂM LÝ THUYẾT CỐT LÕI VỀ KIẾN TRÚC & THIẾT KẾ HỆ THỐNG

### 1.1. Kiến Trúc 3 Tầng (3-Tier Layered Architecture) & Phân Tách Trách Nhiệm (SoC)

Dự án áp dụng mô hình phân tầng chặt chẽ theo nguyên lý Separation of Concerns:

```
[ Client: Frontend / Postman ]
               │  HTTP (JSON)
               ▼
[ 1. Presentation Layer: @RestController ]
   ├── Tiếp nhận HTTP Request, routing URI
   ├── Validate dữ liệu đầu vào (@Valid, @Validated)
   ├── Gọi Service Layer
   └── Trả về HTTP Response chuẩn (Status Code, Body JSON, Headers)
               │  DTO / Criteria
               ▼
[ 2. Business Service Layer: @Service ]
   ├── Điều phối luồng nghiệp vụ (Business Rules)
   ├── Quản lý ranh giới Transaction (@Transactional)
   ├── Ánh xạ giữa Entity và DTO (ModelMapper)
   └── Xử lý ngoại lệ nghiệp vụ (Business Exceptions)
               │  Entity / QueryDSL Predicate / Pageable
               ▼
[ 3. Data Access Layer: @Repository ]
   ├── Tương tác trực tiếp với Database qua Spring Data JPA & QueryDSL
   ├── Thực thi các câu truy vấn phức tạp (Dynamic SQL, Search, Filter, Sort)
   └── Tối ưu hóa truy vấn (Fetch Join, EntityGraph, Index Lookup)
               │  SQL Query / JDBC
               ▼
[ Database: H2 In-Memory / PostgreSQL / MySQL ]
```

- **Quy tắc phụ thuộc một chiều**: Tầng trên chỉ phụ thuộc vào interface của tầng ngay dưới nó. Controller không bao giờ gọi trực tiếp Repository. Repository không chứa logic nghiệp vụ.

---

### 1.2. Mẫu DTO & Tại Sao Không Được Expose JPA Entity Ra Controller?

Trong dự án, chúng ta tách biệt hoàn toàn giữa `Entity` ([`Project`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/Project.java)) và các `DTO` ([`ProjectListResponse`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/response/ProjectListResponse.java), [`ProjectDetailResponse`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/response/ProjectDetailResponse.java), [`CreateProjectRequest`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/request/CreateProjectRequest.java)).

**4 Lý do cốt tử không được expose trực tiếp JPA Entity ra ngoài REST Controller:**
1. **Tránh Lỗi `LazyInitializationException`**: Khi Jackson ObjectMapper serialize Entity thành JSON, nó sẽ truy cập vào các getter của quan hệ `@ManyToOne` hoặc `@ManyToMany` đã cấu hình `FetchType.LAZY`. Do Transaction đã đóng ở Service Layer, Hibernate Session không còn mở, dẫn đến lỗi runtime sập API.
2. **Ngăn Chặn Vòng Lặp Vô Tận (Circular Reference)**: Quan hệ hai chiều (Bidirectional) như `Group` -> `Employee` -> `Group` sẽ khiến Jackson serialize đệ quy vô tận gây tràn bộ nhớ `StackOverflowError`.
3. **Bảo Mật Dữ Liệu (Over-Posting & Information Leakage)**: Entity ánh xạ toàn bộ bảng database, bao gồm cả các trường nhạy cảm hoặc trường hệ thống (`version`, metadata). Dùng DTO giúp kiểm soát tuyệt đối những gì client được xem và được phép gửi lên.
4. **Tính Tiến Hóa Độc Lập (Loose Coupling)**: Cấu trúc database có thể thay đổi (thêm cột, đổi kiểu dữ liệu) mà không làm vỡ API contract đã ký kết với Frontend.

---

### 1.3. Bài Toán N+1 Query & Giải Pháp `@EntityGraph` vs `FETCH JOIN`

#### Bản chất của bài toán N+1 Query
Khi truy vấn danh sách gồm $N$ bản ghi dự án, mỗi dự án có quan hệ `Lazy` với `Group` hoặc `Employees`.
- Truy vấn ban đầu: $1$ câu `SELECT * FROM PROJECT LIMIT 10`.
- Khi duyệt qua danh sách để lấy thông tin nhóm hoặc nhân viên, Hibernate kích hoạt thêm $N$ câu truy vấn con:
  $$\text{Tổng số truy vấn} = 1 + N$$
Nếu $N = 1000$, database sẽ phải chịu tải $1001$ truy vấn mạng riêng biệt, làm giảm hiệu năng nghiêm trọng.

#### So sánh các giải pháp xử lý

| Giải pháp | Cơ chế hoạt động | Ưu điểm | Nhược điểm |
|---|---|---|---|
| `FetchType.EAGER` | Cấu hình trực tiếp trên Entity annotation | Đơn giản, luôn tải sẵn quan hệ | **Cực kỳ nguy hiểm**: Luôn thực hiện join ngay cả khi nghiệp vụ chỉ cần đọc 1 trường đơn giản; không thể tắt động. |
| `JOIN FETCH` (JPQL) | Viết tường minh trong câu JPQL: `SELECT p FROM Project p JOIN FETCH p.group` | Tải dữ liệu chỉ trong 1 câu truy vấn duy nhất | Phải viết JPQL bằng tay; khó kết hợp với phân trang hoặc sắp xếp động. |
| `@EntityGraph` (JPA 2.1) | Khai báo `attributePaths = {"group", "group.groupLeader"}` trên Repository method | Linh hoạt theo từng hàm; tự động sinh LEFT OUTER JOIN; loại bỏ triệt để N+1 mà không cần sửa cấu hình Entity | Cần lưu ý khi fetch nhiều tập hợp Collection cùng lúc có thể gây hiện tượng MultipleBagFetchException. |
| `@BatchSize` / `default_batch_fetch_size` | Gom các ID cần fetch thành mệnh đề `WHERE id IN (?, ?, ...)` theo từng khối | Cứu cánh cho các mối quan hệ lồng nhau phức tạp mà JOIN không tối ưu | Vẫn tốn nhiều hơn 1 câu truy vấn ($\lceil N / \text{batchSize} \rceil + 1$). |

**Ứng dụng trong dự án**:
- [`ProjectRepository.findDetailById`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/ProjectRepository.java#L24-L26) sử dụng `@EntityGraph(attributePaths = {"group", "group.groupLeader", "employees"})` để nạp toàn bộ cấu trúc phân cấp chi tiết dự án chỉ bằng duy nhất 1 câu SQL JOIN.
- [`GroupRepository.findAllBy`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/GroupRepository.java#L14-L16) dùng `@EntityGraph(attributePaths = {"groupLeader"})` giúp dropdown nhóm nạp kèm tên Leader với chi phí tối ưu.

---

### 1.4. Kiểm Soát Cạnh Tranh Đồng Thời & Khóa Lạc Quan (Optimistic Locking)

#### Bài toán Cập nhật Bị Mất (Lost Update Problem)
- User A và User B cùng mở chi tiết Project số 1001 tại cùng một thời điểm. Cả hai đều thấy thông tin phiên bản ban đầu.
- User A chỉnh sửa khách hàng thành "Customer Alpha" và bấm Lưu lúc 10:00:00.
- User B chỉnh sửa tên dự án thành "Beta New" và bấm Lưu lúc 10:00:05.
- Nếu không có cơ chế kiểm soát, dữ liệu của User A sẽ bị User B vô tình ghi đè toàn bộ mà User B không hề hay biết.

#### Cơ chế Khóa Lạc Quan với `@Version`
Thay vì khóa cứng dòng dữ liệu ở database (Pessimistic Locking - làm nghẽn kết nối và giảm throughput), hệ thống sử dụng **Optimistic Locking**:
1. Entity [`Project`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/Project.java) bổ sung trường `@Version private Long version;`.
2. Mỗi lần cập nhật, Hibernate tự động sinh câu lệnh SQL có điều kiện kiểm tra phiên bản:
   ```sql
   UPDATE PROJECT 
   SET NAME = ?, CUSTOMER = ?, VERSION = VERSION + 1 
   WHERE ID = ? AND VERSION = ?
   ```
3. Nếu User B gửi lên `version = 1`, nhưng trước đó User A đã cập nhật khiến version trong DB nhảy lên `2`:
   - Câu lệnh `UPDATE` của User B trả về số dòng ảnh hưởng bằng `0` (`row count = 0`).
   - Hibernate phát hiện bất thường và ném ra ngoại lệ `StaleObjectStateException`, được Spring dịch thành `ObjectOptimisticLockingFailureException`.
   - Backend bắt ngoại lệ này và trả về mã lỗi HTTP `409 Conflict` kèm thông báo cho Frontend yêu cầu nạp lại dữ liệu mới nhất.

---

### 1.5. Tối Ưu Phân Trang: `Page<T>` vs `Slice<T>`

Spring Data cung cấp hai cấu trúc trừu tượng cho việc phân đoạn dữ liệu:

```
                     Phân Trang Trong Spring Data
                                   │
                 ┌─────────────────┴─────────────────┐
                 ▼                                   ▼
              Page<T>                             Slice<T>
   ├── Thực thi 2 truy vấn SQL         ├── Thực thi đúng 1 truy vấn SQL
   │   (1 query lấy data + LIMIT/OFFSET)  │   (query lấy data với LIMIT size + 1)
   │   (1 query SELECT COUNT(*))           ├── Không đếm tổng bản ghi
   ├── Biết được tổng số trang         ├── Chỉ biết có trang tiếp hay không
   │   (totalPages, totalElements)     │   (hasNext())
   └── Thích hợp cho: Bảng danh sách  └── Thích hợp cho: Infinite Scroll,
       cần hiển thị phân trang số              Dropdown Autocomplete (Group, Employee)
```

**Tại sao việc dùng `Slice<T>` cho Autocomplete là tối quan trọng?**
Khi bảng `EMPLOYEE` hoặc `GROUP` đạt hàng chục ngàn hay hàng triệu bản ghi, câu lệnh `SELECT COUNT(*)` mỗi lần người dùng gõ phím tìm kiếm trong dropdown sẽ gây lãng phí tài nguyên CPU và I/O khủng khiếp của cơ sở dữ liệu. `Slice<T>` loại bỏ 100% chi phí đếm tổng này.

---

### 1.6. Cơ Sở Dữ Liệu: Chỉ Mục B-Tree & Phân Tích Kế Hoạch Thực Thi (EXPLAIN ANALYZE)

#### Cấu trúc B-Tree Index
Chỉ mục trong các hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) thường được tổ chức dưới dạng cây cân bằng B-Tree. Độ phức tạp tìm kiếm trên B-Tree là $O(\log N)$, vượt trội hoàn toàn so với quét toàn bộ bảng (Full Table Scan) có độ phức tạp $O(N)$.

#### Nguyên tắc Tiền Tố Bên Trái (Leftmost Prefix Rule)
Khi tạo chỉ mục phức hợp (Composite Index) trên nhiều cột, ví dụ:
```java
@Index(name = "idx_project_status_customer", columnList = "STATUS, CUSTOMER")
```
- Các truy vấn sau sẽ tận dụng được chỉ mục:
  - `WHERE STATUS = 'NEW'`
  - `WHERE STATUS = 'NEW' AND CUSTOMER = 'Customer A'`
- Nhưng truy vấn sau **KHÔNG THỂ** sử dụng chỉ mục:
  - `WHERE CUSTOMER = 'Customer A'` (do thiếu tiền tố bên trái là `STATUS`).

#### Những cạm bẫy làm vô hiệu hóa B-Tree Index (Bypassing Index)
1. **Ký tự đại diện ở đầu chuỗi (Leading Wildcard)**:
   - `LIKE 'EFV%'`: Sử dụng được Index (tìm kiếm theo tiền tố).
   - `LIKE '%EFV%'` hoặc `LIKE '%EFV'`: Bắt buộc phải **Full Table Scan** vì cây B-Tree được sắp xếp theo thứ tự từ điển từ trái sang phải, không thể định vị chuỗi khi chưa biết ký tự bắt đầu.
2. **Áp dụng hàm hoặc phép toán lên cột đã đánh Index**:
   - `WHERE LOWER(NAME) = 'efv'`: Gây Table Scan vì cơ sở dữ liệu phải chạy hàm `LOWER()` trên từng dòng dữ liệu trước khi so sánh, trừ khi sử dụng Function-based Index.
   - `WHERE START_DATE + 5 > CURRENT_DATE`: Gây Table Scan.
3. **Chuyển đổi kiểu dữ liệu ngầm định (Implicit Type Casting)**:
   - Khi so sánh cột chuỗi `VARCHAR` với số nguyên `INT`, hệ quản trị tự động gọi hàm cast kiểu dữ liệu trên từng hàng.

#### Phân tích kế hoạch thực thi bằng `EXPLAIN ANALYZE`
Lệnh `EXPLAIN ANALYZE` không chỉ hiển thị dự đoán của bộ tối ưu hóa (Optimizer) mà còn trực tiếp thực thi câu lệnh trong một sandbox để đo lường chính xác:
- `scanCount`: Số lượng bản ghi thực tế đã phải đọc từ đĩa hoặc bộ nhớ đệm.
- Kế hoạch sử dụng: `direct lookup` / `indexScan` (Tốt) hay `tableScan` (Cần tối ưu).

---

### 1.7. Nguyên Tắc Thiết Kế RESTful API Chuẩn Mực

RESTful API trong dự án được chuẩn hóa theo các tiêu chuẩn quốc tế:

1. **Định danh tài nguyên qua danh từ số nhiều (Plural Nouns)**:
   - `/projects` (đại diện cho tập hợp dự án)
   - `/projects/{id}` (đại diện cho một phần tử dự án cụ thể)
   - `/groups`, `/employees`
2. **Sử dụng đúng động từ HTTP (HTTP Verbs)**:
   - `GET`: Truy xuất dữ liệu (An toàn & Idempotent).
   - `POST`: Tạo mới tài nguyên (Không Idempotent).
   - `PUT`: Cập nhật toàn bộ hoặc thay thế tài nguyên (Idempotent).
   - `DELETE`: Xóa tài nguyên (Idempotent).
3. **Mã trạng thái phản hồi HTTP (HTTP Status Codes)**:
   - `200 OK`: Truy xuất hoặc cập nhật thành công, có trả về dữ liệu.
   - `201 Created`: Tạo mới tài nguyên thành công, kèm header hoặc body mô tả tài nguyên mới.
   - `204 No Content`: Thao tác xóa thành công, không cần trả về body.
   - `400 Bad Request`: Lỗi dữ liệu đầu vào không hợp lệ (Validation Failure).
   - `404 Not Found`: Không tìm thấy tài nguyên theo định danh ID.
   - `409 Conflict`: Xung đột phiên bản dữ liệu (Optimistic Locking failure).
   - `500 Internal Server Error`: Lỗi hệ thống không lường trước.

---

### 1.8. Cơ Chế Xử Lý Lỗi Tập Trung (Global Exception Handling)

Thay vì bọc các khối `try-catch` lặp đi lặp lại ở từng Controller method, dự án sử dụng mô hình AOP (Aspect-Oriented Programming) thông qua [`GlobalExceptionHandler`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/exception/GlobalExceptionHandler.java):

```
                   Exception Xảy Ra Ở Bất Kỳ Đâu
                                 │
                                 ▼
                   @RestControllerAdvice Intercept
                                 │
     ┌───────────────────────────┼───────────────────────────┐
     ▼                           ▼                           ▼
MethodArgumentNotValid    ProjectNotFoundException    ObjectOptimisticLocking...
     │                           │                           │
     ▼                           ▼                           ▼
  400 Bad Request             404 Not Found               409 Conflict
"VALIDATION_ERROR"         "PROJECT_NOT_FOUND"         "CONCURRENT_UPDATE"
     │                           │                           │
     └───────────────────────────┼───────────────────────────┘
                                 ▼
                     Trả về ErrorResponse thống nhất:
                     {
                       "errorCode": "...",
                       "message": "...",
                       "errors": { "field": "lý do" }
                     }
```

---

### 1.9. Cơ Chế Kiểm Tra Ràng Buộc Dữ Liệu (Bean Validation JSR-380)

#### Phân biệt Field-level vs Class-level Validation
- **Field-level**: Đặt trên từng thuộc tính đơn lẻ. Ví dụ: `@NotBlank`, `@Size(max = 50)`, custom annotation `@Visa`. Chỉ kiểm tra tính độc lập của giá trị trường đó.
- **Class-level**: Đặt ở cấp độ Class của DTO. Dùng khi tính hợp lệ của trường này phụ thuộc vào giá trị của trường khác (Cross-field validation).
  - Ví dụ: [`@StartBeforeEndDate`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/StartBeforeEndDate.java) kiểm tra `startDate` phải đứng trước `endDate`. Nếu đặt ở cấp độ field thì không thể lấy được giá trị của trường còn lại.

#### Vòng đời và Cấu trúc của một Custom Constraint Annotation
Mỗi Custom Constraint gồm 2 thành phần chính:
1. **Interface Annotation**:
   - Chứa `@Constraint(validatedBy = TênValidator.class)`.
   - Bắt buộc phải có 3 phương thức chuẩn:
     - `String message() default "...";` (Thông báo lỗi mặc định).
     - `Class<?>[] groups() default {};` (Phân nhóm validation).
     - `Class<? extends Payload>[] payload() default {};` (Mang metadata).
2. **Lớp Triển Khai Validator**:
   - Triển khai `ConstraintValidator<AnnotationName, TargetType>`.
   - `initialize(AnnotationName constraintAnnotation)`: Đọc cấu hình từ annotation (ví dụ cờ `allowEqual`).
   - `boolean isValid(TargetType value, ConstraintValidatorContext context)`: Thực thi thuật toán kiểm tra logic. Cho phép tùy chỉnh gán lỗi vào đúng thuộc tính mong muốn thông qua `context.buildConstraintViolationWithTemplate(...).addPropertyNode(...).addConstraintViolation()`.

---

## 2. BẢNG TRA CỨU TOÀN DIỆN CÁC ANNOTATION SỬ DỤNG TRONG DỰ ÁN

---

### 2.1. Nhóm Spring Core & Spring Boot

#### `@SpringBootApplication`
- **Vị trí**: Cấp Class cấu hình khởi động (`ApplicationWebConfig`).
- **Ý nghĩa & Bản chất**: Là meta-annotation tổng hợp của 3 annotation cốt lõi:
  - `@SpringBootConfiguration`: Khai báo class là Spring Configuration class.
  - `@EnableAutoConfiguration`: Kích hoạt cơ chế tự động cấu hình các bean dựa trên thư viện trong classpath.
  - `@ComponentScan`: Quét toàn bộ package hiện tại và các package con để đăng ký các Spring Bean (`@Component`, `@Service`, `@Repository`, `@Controller`).

#### `@Configuration`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Đánh dấu class chứa các định nghĩa bean (thông qua các method `@Bean`). Spring CGLIB proxy sẽ chặn các lời gọi method `@Bean` bên trong class này để đảm bảo nguyên lý Singleton (chỉ tạo duy nhất một instance).

#### `@Bean`
- **Vị trí**: Cấp Method trong class `@Configuration`.
- **Ý nghĩa & Bản chất**: Chỉ định đối tượng trả về từ method sẽ được quản lý bởi Spring IoC Container.
- **Ví dụ trong dự án**: Cấu hình bean `ModelMapper` trong `ApplicationWebConfig`.

#### `@Component`, `@Service`, `@Repository`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**:
  - `@Component`: Đánh dấu một class thông thường là Spring Bean.
  - `@Service`: Đặc tả hóa của `@Component`, đại diện cho tầng Business Logic. Giúp làm rõ ngữ cảnh kiến trúc.
  - `@Repository`: Đặc tả hóa của `@Component`, đại diện cho tầng Data Access. Điểm đặc biệt: Tự động dịch chuyển các ngoại lệ của JDBC/Hibernate (`SQLException`, `HibernateException`) thành hệ thống ngoại lệ phân cấp `DataAccessException` của Spring.

#### `@Autowired`
- **Vị trí**: Cấp Field, Constructor hoặc Setter.
- **Ý nghĩa & Bản chất**: Yêu cầu Spring IoC Container tự động tiêm (inject) bean tương ứng theo cơ chế Dependency Injection (DI). Dự án ưu tiên Constructor Injection (kết hợp với `@RequiredArgsConstructor` của Lombok) để đảm bảo tính bất biến (Immutability) và dễ dàng viết Unit Test mà không cần Spring context.

---

### 2.2. Nhóm Spring Web MVC (REST Controller)

#### `@RestController`
- **Vị trí**: Cấp Class (`ProjectController`, `GroupController`, `EmployeeController`).
- **Ý nghĩa & Bản chất**: Là sự kết hợp giữa `@Controller` và `@ResponseBody`. Mọi dữ liệu trả về từ các method sẽ được tự động chuyển đổi thẳng thành định dạng JSON/XML qua `HttpMessageConverter` và ghi trực tiếp vào HTTP Response Body thay vì render ra View template (JSP/HTML).

#### `@RequestMapping`
- **Vị trí**: Cấp Class hoặc Method.
- **Ý nghĩa & Bản chất**: Định tuyến URL pattern cho controller hoặc method. Khi đặt ở cấp class (ví dụ `@RequestMapping("/projects")`), nó đóng vai trò là base path chung cho tất cả các endpoint bên trong.

#### `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- **Vị trí**: Cấp Method.
- **Ý nghĩa & Bản chất**: Là các shortcut tương ứng cho `@RequestMapping(method = RequestMethod.GET/POST/PUT/DELETE)`. Chuẩn hóa hành vi theo đúng ngữ nghĩa RESTful.

#### `@PathVariable`
- **Vị trí**: Tham số của Method.
- **Ý nghĩa & Bản chất**: Trích xuất giá trị từ biến định tuyến trên URI (URI Template Variable).
- **Ví dụ**: Với pattern `/projects/{id}`, `@PathVariable Long id` sẽ bắt giá trị `1` khi client gọi `/projects/1`.

#### `@RequestParam`
- **Vị trí**: Tham số của Method.
- **Ý nghĩa & Bản chất**: Bắt các tham số query string từ URL (sau dấu `?`).
- **Ví dụ**: `GET /projects?keyword=EFV&status=NEW`, Spring tự động bind giá trị vào DTO hoặc tham số tương ứng.

#### `@RequestBody`
- **Vị trí**: Tham số của Method.
- **Ý nghĩa & Bản chất**: Yêu cầu Jackson deserialize nội dung của HTTP Request Body (chuỗi JSON) thành đối tượng Java DTO.

#### `@ResponseStatus`
- **Vị trí**: Cấp Method hoặc Exception Class.
- **Ý nghĩa & Bản chất**: Định nghĩa trước HTTP Status Code trả về cho response khi method thực thi xong hoặc khi exception tương ứng bị ném ra.
- **Ví dụ**: `@ResponseStatus(HttpStatus.CREATED)` trên phương thức tạo mới project, trả về mã `201 Created`.

#### `@RestControllerAdvice` & `@ExceptionHandler`
- **Vị trí**:
  - `@RestControllerAdvice`: Cấp Class (`GlobalExceptionHandler`).
  - `@ExceptionHandler`: Cấp Method xử lý lỗi.
- **Ý nghĩa & Bản chất**: Cơ chế AOP bắt tập trung các ngoại lệ phát sinh từ bất kỳ Controller nào trong toàn bộ ứng dụng. Cho phép chuẩn hóa định dạng lỗi JSON trả về cho client mà không làm phân tán mã nguồn.

---

### 2.3. Nhóm Spring Data JPA & Transaction Management

#### `@Transactional`
- **Vị trí**: Cấp Class hoặc Method ở Service/Repository layer.
- **Ý nghĩa & Bản chất**: Quản lý ranh giới giao dịch (Transaction Boundary) theo thuộc tính ACID:
  - Tự động mở transaction khi bước vào method.
  - Tự động gọi `commit()` khi method kết thúc thành công.
  - Tự động gọi `rollback()` nếu xảy ra `RuntimeException` hoặc `Error`.
  - `readOnly = true`: Tối ưu hóa hiệu năng, hướng dẫn Hibernate tắt cơ chế Dirty Checking (không cần theo dõi snapshot để cập nhật), giúp tiết kiệm đáng kể bộ nhớ RAM và thời gian CPU.
  - Trong Unit/Integration Test: `@Transactional` khiến Spring tự động rollback toàn bộ dữ liệu sau mỗi bài test, giữ cơ sở dữ liệu luôn sạch sẽ và cô lập giữa các test cases.

#### `@EntityGraph`
- **Vị trí**: Trên các phương thức truy vấn của Spring Data JPA Repository.
- **Ý nghĩa & Bản chất**: Can thiệp vào kế hoạch nạp dữ liệu (Fetch Plan) của JPA tại thời điểm runtime. Bằng việc chỉ định `attributePaths = {"group", "group.groupLeader", "employees"}`, Spring Data JPA sẽ sinh câu lệnh SQL `LEFT OUTER JOIN` để nạp sẵn các quan hệ được chỉ định trong cùng một câu query duy nhất, triệt tiêu hoàn toàn vấn đề N+1 query.

#### `@Query` & `@Modifying`
- **Vị trí**: Trên phương thức của Repository.
- **Ý nghĩa & Bản chất**:
  - `@Query`: Khai báo trực tiếp câu truy vấn JPQL hoặc Native SQL khi các method chuẩn của Spring Data không đáp ứng đủ độ phức tạp.
  - `@Modifying`: Bắt buộc đi kèm khi câu `@Query` thực hiện các lệnh thao tác thay đổi dữ liệu (`INSERT`, `UPDATE`, `DELETE`, `DDL`), báo cho Spring Data biết để thực thi qua `executeUpdate()` thay vì `executeQuery()`.

---

### 2.4. Nhóm JPA / Hibernate ORM (Entity Mapping)

#### `@Entity`
- **Vị trí**: Cấp Class (`Project`, `Group`, `Employee`).
- **Ý nghĩa & Bản chất**: Khai báo class là một thực thể ORM (Persistent Domain Object) được quản lý bởi JPA EntityManager và có liên kết với một bảng trong cơ sở dữ liệu.

#### `@Table`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Định nghĩa tên bảng tương ứng trong CSDL (`name = "PROJECT"`) và danh sách các chỉ mục tối ưu hóa (`indexes = { @Index(...) }`).
- **Lưu ý**: Tên bảng trùng với từ khóa CSDL cần được trích dẫn (ví dụ `name = "\"GROUP\""`).

#### `@Id` & `@GeneratedValue`
- **Vị trí**: Thuộc tính định danh khóa chính.
- **Ý nghĩa & Bản chất**:
  - `@Id`: Xác định thuộc tính là Primary Key của bảng.
  - `@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "...")`: Chỉ định chiến lược tự động sinh khóa chính. Sử dụng `SEQUENCE` tối ưu hơn `IDENTITY` vì Hibernate có thể tận dụng cơ chế gom lô (Batch Insert) mà không cần phải thực thi câu `INSERT` ngay lập tức để lấy lại ID từ database.

#### `@Column`
- **Vị trí**: Cấp Field.
- **Ý nghĩa & Bản chất**: Quy định ràng buộc vật lý của cột trong DB: `nullable = false`, `unique = true`, `length = 50`.

#### `@Enumerated(EnumType.STRING)`
- **Vị trí**: Cấp Field enum ([`ProjectStatus`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/ProjectStatus.java)).
- **Ý nghĩa & Bản chất**: Lưu giá trị enum dưới dạng chuỗi văn bản (`'NEW'`, `'PLA'`, `'INP'`, `'FIN'`) thay vì số nguyên thứ tự (`0, 1, 2, 3`). Nếu dùng số (mặc định `ORDINAL`), khi thêm một giá trị mới vào giữa enum, toàn bộ dữ liệu lịch sử trong DB sẽ bị sai lệch hoàn toàn.

#### `@Version`
- **Vị trí**: Cấp Field (`private Long version;`).
- **Ý nghĩa & Bản chất**: Kích hoạt cơ chế Khóa Lạc Quan (Optimistic Locking) của Hibernate. Ngăn chặn triệt để bài toán ghi đè dữ liệu đồng thời (Lost Update Problem).

#### `@ManyToOne` & `@OneToMany` & `@ManyToMany`
- **Vị trí**: Cấp Field quan hệ giữa các Entity.
- **Ý nghĩa & Bản chất**:
  - `@ManyToOne(fetch = FetchType.LAZY)`: Quan hệ nhiều - một. **Luôn phải đặt `fetch = FetchType.LAZY`** vì mặc định của JPA cho `@ManyToOne` là `EAGER`, rất dễ gây bùng nổ truy vấn ngầm định.
  - `@OneToMany(mappedBy = "...", cascade = CascadeType.ALL)`: Quan hệ một - nhiều. Thuộc tính `mappedBy` chỉ định bên không sở hữu quan hệ khóa ngoại (Inverted Side).
  - `@ManyToMany`: Quan hệ nhiều - nhiều giữa `Project` và `Employee`, kết hợp với `@JoinTable` để cấu hình bảng trung gian `PROJECT_EMPLOYEE`.

#### `@JoinColumn` & `@JoinTable`
- **Vị trí**: Cấp Field quan hệ.
- **Ý nghĩa & Bản chất**:
  - `@JoinColumn(name = "GROUP_ID")`: Chỉ định tên cột khóa ngoại (Foreign Key) trực tiếp trên bảng.
  - `@JoinTable`: Chỉ định tên bảng liên kết trung gian cùng các khóa ngoại nối hai bảng với nhau trong quan hệ Many-to-Many.

---

### 2.5. Nhóm Project Lombok

#### `@Getter` & `@Setter`
- **Vị trí**: Cấp Class hoặc Field.
- **Ý nghĩa & Bản chất**: Annotation Processor của Lombok tự động sinh mã bytecode cho các hàm `getXYZ()` và `setXYZ()` trong quá trình biên dịch (Compile-time), giúp giữ mã nguồn sạch sẽ, không có boilerplate code thừa.

#### `@NoArgsConstructor` & `@AllArgsConstructor`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**:
  - `@NoArgsConstructor`: Bắt buộc đối với các JPA Entity vì Hibernate yêu cầu Constructor không đối số (Default Constructor) để khởi tạo đối tượng qua Reflection khi nạp dữ liệu từ DB.
  - `@AllArgsConstructor`: Sinh constructor chứa đầy đủ tất cả các trường, cần thiết cho builder pattern.

#### `@Builder`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Triển khai thiết kế Builder Pattern cho đối tượng. Giúp việc khởi tạo object có nhiều thuộc tính trở nên trực quan, linh hoạt, tránh nhầm lẫn thứ tự tham số.

#### `@ToString` & `@EqualsAndHashCode`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Tự động sinh `toString()`, `equals()` và `hashCode()`.
- **CẠNH BẪY CẦN TRÁNH VỚI `@Data` TRÊN JPA ENTITY**:
  Tuyệt đối không sử dụng `@Data` trên JPA Entity! `@Data` tự động bao hàm `@ToString` và `@EqualsAndHashCode` trên toàn bộ các trường. Khi entity có quan hệ `@ManyToOne` hoặc `@ManyToMany` với `FetchType.LAZY`, việc gọi `hashCode()` hay `toString()` sẽ vô tình kích hoạt tải toàn bộ quan hệ (gây N+1 query), hoặc gây ra lỗi đệ quy vô tận `StackOverflowError` giữa hai chiều của quan hệ.

---

### 2.6. Nhóm Bean Validation (JSR-380 & Custom Annotations)

#### `@Valid` & `@Validated`
- **Vị trí**: Cấp Tham số Controller method hoặc Cấp Class.
- **Ý nghĩa & Bản chất**:
  - `@Valid` (thuộc gói `javax.validation`): Chỉ thị cho validator kiểm tra đệ quy đối tượng DTO truyền vào theo các ràng buộc đã định nghĩa bên trong nó.
  - `@Validated` (thuộc Spring): Mở rộng của `@Valid`, hỗ trợ cơ chế phân nhóm Validation Groups (ví dụ nhóm Create khác nhóm Update).

#### `@NotNull`, `@NotBlank`, `@NotEmpty`
- **Vị trí**: Cấp Field trong DTO.
- **Phân biệt bản chất**:
  - `@NotNull`: Giá trị không được là `null`, nhưng chuỗi rỗng `""` hoặc chuỗi chứa khoảng trắng `"   "` vẫn hợp lệ (dùng cho số, ngày tháng, Object).
  - `@NotEmpty`: Giá trị không được `null` và độ dài phải `> 0` (chuỗi `"   "` vẫn hợp lệ). Dùng cho Collection, List, Map.
  - `@NotBlank`: Chặt chẽ nhất dành cho chuỗi `String`. Giá trị không được `null`, độ dài `> 0` sau khi đã loại bỏ khoảng trắng (`trim().length() > 0`).

#### `@Size`, `@Min`, `@Max`, `@Pattern`
- **Vị trí**: Cấp Field.
- **Ý nghĩa & Bản chất**:
  - `@Size(max = 50)`: Kiểm soát độ dài tối đa của chuỗi hoặc số phần tử của danh sách.
  - `@Min(1)`, `@Max(9999)`: Giới hạn giá trị số học tối thiểu và tối đa.
  - `@Pattern(regexp = "...")`: Kiểm tra chuỗi theo biểu thức chính quy (Regex).

#### Các Custom Validation Annotations Được Xây Dựng Trong Dự Án
1. **[`@Visa`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/Visa.java)**:
   - Ràng buộc mã định danh nhân viên: bắt buộc đúng 3 ký tự viết hoa từ `A-Z`.
   - Validator: [`VisaValidator`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/impl/VisaValidator.java).
2. **[`@Visas`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/Visas.java)**:
   - Ràng buộc danh sách các mã VISA nhân viên tham gia dự án.
   - Validator: [`VisasValidator`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/impl/VisasValidator.java).
3. **[`@StartBeforeEndDate`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/StartBeforeEndDate.java)**:
   - Ràng buộc cấp độ Class: kiểm tra logic nghiệp vụ `startDate <= endDate`.
   - Hỗ trợ tham số cấu hình linh hoạt: `allowEqual` (cho phép ngày bắt đầu trùng ngày kết thúc hay không).
   - Validator: [`StartBeforeEndDateValidator`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/impl/StartBeforeEndDateValidator.java).

---

### 2.7. Nhóm Testing (JUnit, Mockito, SpringBootTest)

#### `@RunWith(SpringRunner.class)`
- **Vị trí**: Cấp Class kiểm thử.
- **Ý nghĩa & Bản chất**: Cung cấp cầu nối giữa môi trường thực thi JUnit 4 và Spring TestContext Framework, cho phép nạp Spring Context vào bài test.

#### `@SpringBootTest`
- **Vị trí**: Cấp Class kiểm thử tích hợp.
- **Ý nghĩa & Bản chất**: Khởi động toàn bộ Spring ApplicationContext (tương đương khi chạy ứng dụng thật). Cho phép kiểm thử tích hợp toàn diện từ tầng Controller, Service đến tầng Database H2.

#### `@AutoConfigureMockMvc`
- **Vị trí**: Cấp Class kiểm thử Web.
- **Ý nghĩa & Bản chất**: Tự động cấu hình và khởi tạo đối tượng `MockMvc`. Cho phép gửi các HTTP Request giả lập (GET, POST, PUT, DELETE) đến Controller mà không cần phải khởi động Web Server thật (Tomcat), giúp test chạy cực nhanh.

#### `@MockBean` vs `@Mock`
- **Vị trí**: Cấp Field trong test class.
- **Phân biệt bản chất**:
  - `@Mock`: Là annotation thuần của Mockito. Tạo một đối tượng mock độc lập mà không can dự gì đến Spring ApplicationContext. Dùng trong Unit Test thuần túy của Service layer (`ProjectServiceTest`).
  - `@MockBean`: Là annotation của Spring Boot Test. Tạo mock object và **thay thế trực tiếp** bean thật tương ứng đang nằm trong Spring ApplicationContext. Dùng khi muốn cô lập tầng Web MVC khỏi tầng Service trong `ProjectControllerTest`.

#### `@InjectMocks`
- **Vị trí**: Cấp Field.
- **Ý nghĩa & Bản chất**: Chỉ thị Mockito tự động khởi tạo đối tượng cần kiểm thử và tiêm toàn bộ các dependency được đánh dấu `@Mock` vào đối tượng này.

#### `@Test` & `@Before`
- **Vị trí**: Cấp Method.
- **Ý nghĩa & Bản chất**:
  - `@Test`: Đánh dấu phương thức là một test case độc lập được JUnit thực thi.
  - `@Before`: Chạy tự động trước mỗi method `@Test` để thiết lập trạng thái giả lập ban đầu (Setup).

---

## 3. TỔNG KẾT & QUY TẮC THIẾT KẾ ĐẠT CHUẨN DOANH NGHIỆP

1. **Clean Code & Không Boilerplate**: Tận dụng tối đa Lombok để giữ codebase tinh gọn, nhưng luôn cảnh giác với các rủi ro của `@Data` trên JPA Entity.
2. **Hiệu năng là ưu tiên hàng đầu**:
   - Sử dụng `Slice<T>` cho dữ liệu tìm kiếm vô hạn / dropdown để giảm tải `COUNT(*)`.
   - Sử dụng `@EntityGraph` để triệt tiêu bài toán N+1 Query.
   - Luôn đặt `FetchType.LAZY` cho các quan hệ `@ManyToOne`.
   - Thấu hiểu cơ chế B-Tree Index để không viết các câu truy vấn gây vô hiệu hóa chỉ mục.
3. **An toàn đồng thời**: Bắt buộc có trường `@Version` trên các bảng nghiệp vụ có khả năng bị nhiều người cùng chỉnh sửa để đảm bảo toàn vẹn dữ liệu.
4. **Kiểm soát chặt chẽ dữ liệu đầu vào**: Ràng buộc qua Bean Validation ngay tại cửa ngõ DTO, ngăn chặn hoàn toàn dữ liệu rác đi vào tầng xử lý nghiệp vụ hay cơ sở dữ liệu.
