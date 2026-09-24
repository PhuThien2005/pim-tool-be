# Cẩm Nang Lý Thuyết Chuyên Sâu & Tra Cứu Toàn Bộ Annotation (pilot-project-back)

Tài liệu này tổng hợp toàn bộ các điểm lý thuyết cốt lõi (Core Architectural Points) và bảng tra cứu chuyên sâu các Annotation được sử dụng trong dự án backend `pilot-project-back`.

---

## MỤC LỤC

1. [Các Điểm Lý Thuyết Cốt Lõi Về Kiến Trúc & Thiết Kế Hệ Thống](#1-các-điểm-lý-thuyết-cốt-lõi-về-kiến-trúc--thiết-kế-hệ-thống)
   - [1.1. Kiến Trúc 3 Tầng (3-Tier Layered Architecture) & Phân Tách Trách Nhiệm (SoC)](#11-kiến-trúc-3-tầng-3-tier-layered-architecture--phân-tách-trách-nhiệm-soc)
   - [1.2. Mẫu DTO & Tại Sao Không Được Expose JPA Entity Ra Controller?](#12-mẫu-dto--tại-sao-không-được-expose-jpa-entity-ra-controller)
   - [1.3. Bài Toán N+1 Query & So Sánh Chuyên Sâu `@EntityGraph` vs `@BatchSize`](#13-bài-toán-n1-query--so-sánh-chuyên-sâu-entitygraph-vs-batchsize)
   - [1.4. Kiểm Soát Cạnh Tranh Đồng Thời (Concurrency Control): Khóa Lạc Quan (`@Version`) vs Hiểm Họa TOCTOU & `DataIntegrityViolationException`](#14-kiểm-soát-cạnh-tranh-đồng-thời-concurrency-control-khóa-lạc-quan-version-vs-hiểm-họa-toctou--dataintegrityviolationexception)
   - [1.5. Tối Ưu Phân Trang: `Page<T>` vs `Slice<T>` & Cấu Trúc Chi Tiết Của `Page<T>`](#15-tối-ưu-phân-trang-paget-vs-slicet--cấu-trúc-chi-tiết-của-paget)
   - [1.6. Cơ Chế Sinh Khóa Chính: `SEQUENCE` vs `IDENTITY` & Cơ Chế Write-Behind](#16-cơ-chế-sinh-khóa-chính-sequence-vs-identity--cơ-chế-write-behind)
   - [1.7. Thiết Kế Thực Thể Cơ Sở (`@MappedSuperclass`) & Ràng Buộc Khóa Chính](#17-thiết-kế-thực-thể-cơ-sở-mappedsuperclass--ràng-buộc-khóa-chính)
   - [1.8. Quản Lý Quan Hệ Hai Chiều (Bidirectional) & Tại Sao Cần `@Setter(AccessLevel.NONE)`](#18-quản-lý-quan-hệ-hai-chiều-bidirectional--tại-sao-cần-setteraccesslevelnone)
   - [1.9. Bản Chất Của Hibernate Proxy & `@JsonIgnoreProperties`](#19-bản-chất-của-hibernate-proxy--jsonignoreproperties)
   - [1.10. Mẫu Thiết Kế Builder Pattern (`@Builder`)](#110-mẫu-thiết-kế-builder-pattern-builder)
   - [1.11. Cơ Sở Dữ Liệu: Chỉ Mục B-Tree & Phân Tích Kế Hoạch Thực Thi (EXPLAIN ANALYZE)](#111-cơ-sở-dữ-liệu-chỉ-mục-b-tree--phân-tích-kế-hoạch-thực-thi-explain-analyze)
   - [1.12. Nguyên Tắc Thiết Kế RESTful API Chuẩn Mực](#112-nguyên-tắc-thiết-kế-restful-api-chuẩn-mực)
   - [1.13. Cơ Chế Xử Lý Lỗi Tập Trung (Global Exception Handling)](#113-cơ-chế-xử-lý-lỗi-tập-trung-global-exception-handling)
   - [1.14. Cơ Chế Kiểm Tra Ràng Buộc Dữ Liệu (Bean Validation JSR-380)](#114-cơ-chế-kiểm-tra-ràng-buộc-dữ-liệu-bean-validation-jsr-380)
2. [Bảng Tra Cứu Toàn Diện Các Annotation Sử Dụng Trong Dự Án](#2-bảng-tra-cứu-toàn-diện-các-annotation-sử-dụng-trong-dự-án)
   - [2.1. Nhóm Spring Core & Spring Boot](#21-nhóm-spring-core--spring-boot)
   - [2.2. Nhóm Spring Web MVC (REST Controller)](#22-nhóm-spring-web-mvc-rest-controller)
   - [2.3. Nhóm Spring Data JPA & Transaction Management](#23-nhóm-spring-data-jpa--transaction-management)
   - [2.4. Nhóm JPA / Hibernate ORM (Entity Mapping)](#24-nhóm-jpa--hibernate-orm-entity-mapping)
   - [2.5. Nhóm Project Lombok](#25-nhóm-project-lombok)
   - [2.6. Nhóm Jackson Serialization](#26-nhóm-jackson-serialization)
   - [2.7. Nhóm Bean Validation (JSR-380 & Custom Annotations)](#27-nhóm-bean-validation-jsr-380--custom-annotations)
   - [2.8. Nhóm Testing (JUnit, Mockito, SpringBootTest)](#28-nhóm-testing-junit-mockito-springboottest)

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

### 1.3. Bài Toán N+1 Query, Phân Tích Chuyên Sâu QueryDSL Trong `searchProjects` & So Sánh `@EntityGraph` vs `@BatchSize`

Dưới đây là bản phân tích toàn diện về phương thức `searchProjects` trong [`ProjectRepositoryImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/custom/ProjectRepositoryImpl.java), từ bản chất của QueryDSL, Q-classes, `toPredicate` cho đến lý do tại sao sử dụng `INNER JOIN FETCH` cả 3 bảng:

---

#### 1.3.1. QueryDSL, Q-Class & `JPAQuery` Là Gì?

```
[ Entity: Project.java ] 
       │ (Biên dịch Maven / APT Processor)
       ▼
[ Q-Class: QProject.java (target/generated-sources) ]
       │
       ▼
[ JPAQuery<Project>(em) ] ──► Viết Fluent API Type-safe ──► Sinh SQL Chuẩn 100%
```

##### A. QueryDSL là gì và tại sao lại dùng nó?
- **Vấn đề của JPQL/Native SQL String truyền thống**: Viết câu lệnh bằng chuỗi `"SELECT p FROM Project p WHERE p.name = :name"`. Rất dễ gõ sai chính tả tên cột (ví dụ gõ nhầm `projectNumver`), và trình biên dịch Java hoàn toàn không phát hiện được, chỉ khi chạy ứng dụng (Runtime) mới ném lỗi sập API.
- **Giải pháp của QueryDSL**: Cung cấp cơ chế **Type-safe Query**. Mọi bảng và cột được đại diện bằng các đối tượng và phương thức Java. Nếu bạn gõ sai tên thuộc tính, IDE và Maven sẽ báo lỗi biên dịch ngay lập tức (Compile-time verification).

##### B. Q-Classes (`QProject`, `QGroup`, `QEmployee`) là gì?
- Đây là các lớp Java đặc biệt do công cụ **APT (Annotation Processing Tool)** của QueryDSL tự động sinh ra trong thư mục `target/generated-sources/java` khi bạn chạy `mvn compile`.
- Q-Class đóng vai trò là **Bản đồ siêu dữ liệu (Metadata Path)** của Entity:
    - `p.name` là một `StringPath` $\rightarrow$ Cung cấp các hàm chuỗi: `.containsIgnoreCase()`, `.startsWith()`, `.like()`.
    - `p.projectNumber` là một `NumberPath<Integer>` $\rightarrow$ Cung cấp: `.eq()`, `.gt()`, `.goe()`.
    - `p.startDate` là một `DatePath<LocalDate>` $\rightarrow$ Cung cấp: `.before()`, `.after()`, `.between()`.
    - `p.group` là một `QGroup` $\rightarrow$ Cho phép chấm tiếp sang trường con: `p.group.groupLeader.visa`.

##### C. Tại sao lại viết: `QEmployee gl = new QEmployee("groupLeader")`?
- Mặc định, `QEmployee.employee` tĩnh có Table Alias là `"employee"`.
- Nhưng trong một dự án, bảng `EMPLOYEE` có thể đóng **2 vai trò khác nhau trong cùng 1 câu truy vấn**:
    1. Vai trò là **Trưởng nhóm (Group Leader)**.
    2. Vai trò là **Thành viên dự án (Project Members)**.
- Khởi tạo `new QEmployee("groupLeader")` giúp đặt tên **Table Alias riêng biệt** cho nó trong câu lệnh SQL (`INNER JOIN EMPLOYEE groupLeader ON ...`), tránh xung đột và nhầm lẫn với các alias khác.

##### D. `JPAQuery<T>` là gì?
- Là đối tượng trung tâm của QueryDSL kết nối trực tiếp với `EntityManager` của JPA.
- Cung cấp Fluent Builder để ráp nối câu lệnh: `.from(...)`, `.innerJoin(...)`, `.where(...)`, `.distinct()`, `.fetch()`.

---

#### 1.3.2. Bản Chất Của `criteria.toPredicate()`

`Predicate` trong QueryDSL là đại diện cho **mệnh đề điều kiện `WHERE`** trong SQL.

Trong [`SearchProjectCriteria.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/dto/request/SearchProjectCriteria.java):
```java
public Predicate toPredicate() {
    QProject p = QProject.project;
    String kw = StringUtils.isNotBlank(keyword) ? keyword.trim() : null;
    BooleanExpression keywordExp = (kw == null) ? null : p.name.containsIgnoreCase(kw)
            .or(p.customer.containsIgnoreCase(kw))
            .or(StringUtils.isNumeric(kw) ? p.projectNumber.eq(Integer.parseInt(kw)) : null);

    return new BooleanBuilder()
            .and(keywordExp)
            .and(status != null ? p.status.eq(status) : null)
            .and(StringUtils.isNotBlank(leaderVisa) ? p.group.groupLeader.visa.equalsIgnoreCase(leaderVisa.trim()) : null)
            .and(StringUtils.isNotBlank(memberVisa) ? p.employees.any().visa.equalsIgnoreCase(memberVisa.trim()) : null)
            .and(startDateFrom != null ? p.startDate.goe(startDateFrom) : null)
            .and(startDateTo != null ? p.startDate.loe(startDateTo) : null);
}
```

##### Điểm ma thuật của `BooleanBuilder`:
- Nếu bất kỳ vế nào truyền vào là `null`, `BooleanBuilder` **tự động bỏ qua không sinh vào SQL**!
- Ví dụ:
    - Nếu người dùng chỉ tìm theo `keyword = "EFV"`, SQL sinh ra chỉ có: `WHERE (LOWER(p.name) LIKE '%efv%' OR LOWER(p.customer) LIKE '%efv%')`.
    - Khi người dùng bật Advanced Filter truyền thêm `leaderVisa = "DTH"` và `status = "NEW"`, `BooleanBuilder` tự động nối thêm `AND p.status = 'NEW' AND LOWER(gl.visa) = 'dth'`.
- Giúp code cực kỳ ngắn gọn, không cần viết hàng chục câu lệnh `if (keyword != null) ... else ...`.

---

#### 1.3.3. Có Phải Đang Dùng `fetchJoin()` Không?

**CHÍNH XÁC 100%!** Đoạn code trong [`ProjectRepositoryImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/custom/ProjectRepositoryImpl.java):
```java
JPAQuery<Project> dataQuery = new JPAQuery<Project>(em)
        .from(p)
        .innerJoin(p.group, g).fetchJoin()
        .innerJoin(g.groupLeader, gl).fetchJoin()
        .where(criteria != null ? criteria.toPredicate() : null)
        .distinct();
```
- `.innerJoin(p.group, g).fetchJoin()` $\rightarrow$ Chính là cú pháp `INNER JOIN FETCH p.group g` trong JPQL.
- `.innerJoin(g.groupLeader, gl).fetchJoin()` $\rightarrow$ Chính là `INNER JOIN FETCH g.groupLeader gl`.

---

#### 1.3.4. Tại Sao Lại Cần JOIN Cả 3 Bảng (`PROJECT` $\rightarrow$ `GROUP` $\rightarrow$ `EMPLOYEE` Leader)?

```
[ Bảng PROJECT ] ──(GROUP_ID)──► [ Bảng GROUP ] ──(GROUP_LEADER_ID)──► [ Bảng EMPLOYEE (Leader) ]
```

##### Lý do 1: Bản chất quan hệ dữ liệu là phân cấp gián tiếp
Trong cơ sở dữ liệu:
- Bảng `PROJECT` **KHÔNG HỀ CÓ CỘT NÀO LƯU TRỰC TIẾP LEADER ID**! Nó chỉ có cột `GROUP_ID`.
- Muốn biết ai là Leader của dự án, bắt buộc phải đi theo đường dẫn:
  $$\text{PROJECT} \xrightarrow{\text{GROUP\_ID}} \text{GROUP} \xrightarrow{\text{GROUP\_LEADER\_ID}} \text{EMPLOYEE}$$

##### Lý do 2: Cạm bẫy N+1 Query & `LazyInitializationException` khi Map DTO
Cả 2 quan hệ trong Entity đều đặt `FetchType.LAZY`:
```java
// Trong Project.java:
@ManyToOne(fetch = FetchType.LAZY, optional = false)
private Group group;

// Trong Group.java:
@OneToOne(fetch = FetchType.LAZY, optional = false)
private Employee groupLeader;
```

**Chuyện gì sẽ xảy ra nếu KHÔNG dùng `fetchJoin()` với Leader Group?**
1. **Lúc query Project**: Hibernate chỉ chạy 1 câu `SELECT * FROM PROJECT LIMIT 10`.
2. **Lúc Service map DTO** (hoặc `GroupListResponse`/`ProjectDetailResponse` có chứa `groupLeader`):
    - `ModelMapper` hoặc code getter đọc đến `project.getGroup()`. Vì là Lazy, Hibernate bắn thêm 1 câu query để nạp `Group`.
    - Sau đó đọc tiếp `group.getGroupLeader().getVisa()`. Vì `groupLeader` lại là Lazy Proxy của bảng `EMPLOYEE`, Hibernate **lại bắn thêm 1 câu query nữa cho mỗi Group**!
    - Nếu danh sách có 10 project thuộc 10 group khác nhau $\rightarrow$ **10 câu SQL bổ sung bắn dồn dập xuống DB (Thảm họa N+1 Query)**!
3. **Lỗi sập API `LazyInitializationException`**:
   Nếu việc gọi getter/mapping diễn ra sau khi Transaction đóng, Hibernate sẽ quăng ngay ngoại lệ sập ứng dụng:
   ```text
   org.hibernate.LazyInitializationException: could not initialize proxy [Employee#...] - no Session
   ```

👉 **Khi thêm 2 dòng `.fetchJoin()` trên**:
Hibernate ép Database nối phẳng cả 3 bảng và nạp đầy đủ dữ liệu của `Project`, `Group`, và `groupLeader` **CHỈ TRONG ĐÚNG 1 CÂU LỆNH SQL DUY NHẤT**! Cả 3 đối tượng đều đã có sẵn trong bộ nhớ RAM, loại bỏ hoàn toàn 100% N+1 query và triệt tiêu vĩnh viễn lỗi Lazy Proxy.

##### Lý do 3: Phục vụ Lọc Nâng Cao (`leaderVisa`)
Khi người dùng tìm kiếm theo visa trưởng nhóm (`leaderVisa = 'DTH'`), câu lệnh SQL bắt buộc phải kiểm tra điều kiện trên cột `VISA` của bảng `EMPLOYEE` đại diện cho leader. Nếu không join từ `Project` sang `Group` rồi sang `Employee`, database sẽ không có dữ liệu để thực hiện điều kiện `WHERE gl.VISA = 'DTH'`.

##### Lý do 4: Tại Sao Dùng `INNER JOIN` Mà Không Cần `LEFT JOIN` Khi Có `optional = false`?
- **Ràng buộc dữ liệu chặt chẽ**:
  - `Project.group`: `@ManyToOne(fetch = FetchType.LAZY, optional = false)` và `@JoinColumn(name = "GROUP_ID", nullable = false)`.
  - `Group.groupLeader`: `@OneToOne(fetch = FetchType.LAZY, optional = false)` và `@JoinColumn(name = "GROUP_LEADER_ID", nullable = false)`.
  - Hai ràng buộc `optional = false` và `nullable = false` cam kết 100% rằng:
    - **Không bao giờ có Project nào không có Group**.
    - **Không bao giờ có Group nào không có Leader**.
- **`INNER JOIN` là chuẩn xác tuyệt đối về mặt ngữ nghĩa dữ liệu**:
  Vì hai phía luôn luôn có dữ liệu khớp nhau, việc dùng `INNER JOIN` là hoàn toàn tự nhiên và phản ánh đúng bản chất mô hình quan hệ.
- **Tối ưu hóa vượt trội cho Database Query Optimizer (Join Reordering)**:
  - Với **`LEFT JOIN`**: Database Optimizer **bị ép buộc** phải quét toàn bộ bảng bên trái (`PROJECT`) trước, sau đó mới tìm các dòng tương ứng ở bảng bên phải. Thứ tự duyệt bảng bị cố định cứng.
  - Với **`INNER JOIN`**: Database Cost-Based Optimizer có quyền **Join Reordering (Tự do đảo thứ tự duyệt bảng)**:
    - Ví dụ khi người dùng lọc `leaderVisa = 'DTH'`, DB có thể dùng index trên `EMPLOYEE.VISA` để tìm ra ngay 1 người Leader trong bảng `EMPLOYEE` với độ phức tạp $O(1)$, rồi mới join ngược lại `GROUP` $\rightarrow$ `PROJECT`.
    - Lượng dữ liệu phải đọc giảm đi hàng trăm, hàng ngàn lần so với việc phải quét toàn bộ bảng `PROJECT` của `LEFT JOIN`!
- **Khi nào mới cần `LEFT JOIN`?**:
  Chỉ khi mối quan hệ là tùy chọn (`optional = true`, ví dụ một dự án có thể chưa được phân nhóm `groupId = null`), ta mới dùng `LEFT JOIN` để tránh làm mất các dự án chưa có nhóm khỏi kết quả tìm kiếm. Khi đã có `optional = false`, `INNER JOIN` luôn là sự lựa chọn ưu việt nhất.

#### Bản chất của bài toán N+1 Query
Khi truy vấn danh sách gồm $N$ bản ghi dự án, mỗi dự án có quan hệ `Lazy` với `Group` hoặc `Employees`.
- Truy vấn ban đầu: $1$ câu `SELECT * FROM PROJECT LIMIT 10`.
- Khi duyệt qua danh sách để lấy thông tin nhóm hoặc nhân viên, Hibernate kích hoạt thêm $N$ câu truy vấn con:
  $$\text{Tổng số truy vấn} = 1 + N$$
Nếu $N = 1000$, database sẽ phải chịu tải $1001$ truy vấn mạng riêng biệt, làm tê liệt hiệu năng hệ thống.

#### Tại sao `@EntityGraph` (hay `JOIN FETCH`) THẤT BẠI khi kết hợp với Phân Trang (`LIMIT` / `OFFSET`) trên Collection?
Đây là điểm khác biệt sống còn giữa lý thuyết và thực tế vận hành database:
1. **Hiện tượng bùng nổ dòng (Cartesian Product)**: Khi thực hiện `LEFT JOIN` bảng `PROJECT` với bảng tập hợp con `PROJECT_EMPLOYEE` (quan hệ One-to-Many hoặc Many-to-Many), nếu 1 Project có 5 Employee, câu truy vấn SQL sẽ trả về **5 dòng kết quả** có cùng `PROJECT.ID`.
2. **Sai lệch phân trang ở mức Database**: Nếu câu SQL đó áp dụng `LIMIT 10 OFFSET 0`, database sẽ cắt đúng 10 dòng kết quả SQL đầu tiên. Nhưng 10 dòng đó có thể chỉ đại diện cho 2 hoặc 3 Project (vì mỗi Project chiếm nhiều dòng)! Hậu quả: Trang 1 thay vì hiển thị 10 dự án thì chỉ hiển thị được 2 dự án, phân trang bị sai lệch hoàn toàn.
3. **Hibernate buộc phải In-Memory Pagination (Cực kỳ nguy hiểm)**:
   Khi phát hiện câu truy vấn vừa có `JOIN FETCH` trên Collection vừa có `Pageable` (`setFirstResult/setMaxResults`), Hibernate sẽ in ra dòng cảnh báo nghiêm trọng:
   ```text
   WARN: HHH000104: firstResult/maxResults specified with collection fetch; applying in memory!
   ```
   Để không trả về sai số lượng Entity, Hibernate **bỏ qua hoàn toàn mệnh đề LIMIT/OFFSET dưới SQL**, nạp **TOÀN BỘ** hàng triệu bản ghi từ database vào bộ nhớ RAM của ứng dụng rồi mới tự dùng code Java cắt trang! Điều này dẫn tới nguy cơ sập máy chủ vì tràn bộ nhớ (`java.lang.OutOfMemoryError`).

#### Cơ chế giải quyết hoàn hảo của `@BatchSize` (Under The Hood)
Trong trường hợp bình thường (không có phân trang hoặc chỉ là query danh sách đơn giản), `@BatchSize` giải quyết bài toán kinh điển mang tên **N + 1 Query**.

Hãy so sánh trực tiếp cơ chế khi **KHÔNG DÙNG** và khi **CÓ DÙNG** `@BatchSize` để thấy cách nó hoạt động ngầm bên dưới (Under the hood):

---

### 1. Khi KHÔNG CÓ `@BatchSize` (Bị dính lỗi N + 1)

Giả sử bạn query lấy danh sách 10 dự án:

```java
// 1 câu query lấy danh sách cha
List<Project> projects = projectRepository.findAll(); 

for (Project p : projects) {
    // Mỗi lần chạm vào getEmployees(), Hibernate lại bắn 1 câu query riêng rẽ
    System.out.println(p.getEmployees().size());
}

```

* **Câu 1:** Hibernate lấy 10 dự án:
```sql
SELECT * FROM project; -- Trả về 10 projects (ID từ 1 đến 10)

```


* **N câu tiếp theo:** Đến vòng lặp, khi duyệt tới từng project và gọi `p.getEmployees()`, Hibernate thấy collection này là `LAZY` và chưa có dữ liệu trong RAM, nó lập tức bắn tiếp:
```sql
SELECT * FROM employee WHERE project_id = 1;
SELECT * FROM employee WHERE project_id = 2;
SELECT * FROM employee WHERE project_id = 3;
...
SELECT * FROM employee WHERE project_id = 10;

```



$\rightarrow$ **Tổng cộng:** **1** câu query cha + **10** câu query con = **11 câu SQL** gửi xuống database. Nếu có 1.000 dự án thì sẽ là 1.001 câu SQL (Database nghẽn vì chịu quá nhiều network round-trip).

---

### 2. Khi CÓ `@BatchSize(size = 5)` hoặc cấu hình `default_batch_fetch_size: 5`

Cơ chế của Hibernate sẽ thay đổi hoàn toàn nhờ **First-Level Cache (Hibernate Session)**:

```java
List<Project> projects = projectRepository.findAll(); // Lấy 10 projects (ID: 1 -> 10)

```

1. **Hibernate biết trước các Entity chưa nạp con:**
   Khi câu query đầu tiên chạy xong, cả 10 đối tượng `Project` đều đang nằm trong Session của Hibernate. Hibernate ghi nhận rằng: *Cả 10 ông Project này đều đang có tập hợp `employees` ở trạng thái chưa nạp (uninitialized proxy)*.
2. **Gom nhóm kích hoạt khi đụng vào phần tử đầu tiên:**
   Khi bạn chạy vào vòng for và gọi phần tử đầu tiên:
```java
projects.get(0).getEmployees(); // Project ID = 1 cần lấy nhân viên

```


* Thay vì chỉ đi tìm nhân viên cho mỗi ID = 1, Hibernate nhìn vào Session và thấy: *"À, có cấu hình `batch_size = 5`. Trong Session đang có tận 10 Project chưa nạp nhân viên. Vậy mình sẽ tiện tay gom luôn 5 ID đầu tiên lại để query một thể!"*
* Hibernate lập tức sinh ra **1 câu SQL duy nhất** dùng mệnh đề `IN`:
```sql
SELECT * FROM employee WHERE project_id IN (1, 2, 3, 4, 5);

```




3. **Tự động phân phát vào RAM:**
   Nhận kết quả từ câu `IN` về, Hibernate tự nhét nhân viên vào đúng `Set<Employee>` của cả 5 project (từ 1 đến 5).
4. **Tận dụng dữ liệu có sẵn cho các vòng lặp tiếp theo:**
* Khi vòng for lặp tiếp đến `projects.get(1).getEmployees()` (ID = 2): Dữ liệu **đã có sẵn trong RAM** từ câu `IN` trước đó $\rightarrow$ Không bắn thêm câu SQL nào.
* Đến ID = 3, 4, 5: Vẫn có sẵn trong RAM $\rightarrow$ Không bắn SQL.
* Đến `projects.get(5).getEmployees()` (ID = 6): Dữ liệu chưa có, Hibernate lại gom tiếp 5 ID còn lại (6, 7, 8, 9, 10) và bắn câu query thứ hai:
```sql
SELECT * FROM employee WHERE project_id IN (6, 7, 8, 9, 10);

```





---

### Kết quả so sánh

* **Không có `@BatchSize`:** Chạy mất **11 câu SQL** ($1 + 10$).
* **Có `@BatchSize(size = 5)`:** Chỉ mất đúng **3 câu SQL**:
* 1 câu lấy danh sách Project.
* 1 câu `IN (1, 2, 3, 4, 5)` cho 5 project đầu.
* 1 câu `IN (6, 7, 8, 9, 10)` cho 5 project sau.



Nếu đặt `batch_size = 50` hoặc `100`, thì 10 bản ghi đó sẽ được gom hết vào **đúng 1 câu `IN` duy nhất**, biến bài toán từ $1 + N$ câu lệnh thành vỏn vẹn **2 câu lệnh SQL**.
Để vừa phân trang chuẩn xác dưới Database bằng `LIMIT / OFFSET`, vừa triệt tiêu bài toán N+1 Query mà không bị OOM, `@BatchSize` là giải pháp tối ưu:
1. **Bước 1 (Query gốc)**: Hibernate thực hiện câu query chính lấy dữ liệu bảng cha **KHÔNG JOIN VỚI COLLECTION CON**:
   ```sql
   SELECT p.* FROM PROJECT p ORDER BY p.PROJECT_NUMBER ASC LIMIT 10 OFFSET 0;
   ```
   Database áp dụng `LIMIT 10` trực tiếp dưới tầng lưu trữ, trả về đúng chuẩn xác 10 Entity `Project` trong vài mili-giây.
2. **Bước 2 (Batch Fetching ngầm)**:
   Khi mã nguồn hoặc ObjectMapper duyệt qua 10 Project này và gọi `project.getEmployees()`, Hibernate không phát sinh 10 câu lệnh riêng lẻ. Nhờ cấu hình `default_batch_fetch_size: 50` hoặc `@BatchSize(size = 50)`, Hibernate sẽ gom toàn bộ 10 ID của các Project trên trang hiện tại thành **đúng 1 câu SQL duy nhất** sử dụng mệnh đề `IN`:
   ```sql
   SELECT pe.PROJECT_ID, e.* 
   FROM PROJECT_EMPLOYEE pe 
   JOIN EMPLOYEE e ON pe.EMPLOYEE_ID = e.ID 
   WHERE pe.PROJECT_ID IN (1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010);
   ```
3. **Kết luận**:
   - **Dùng `@EntityGraph` / `JOIN FETCH`**: Tuyệt vời cho các quan hệ To-One (`@ManyToOne`, `@OneToOne`) hoặc khi truy vấn chi tiết 1 bản ghi duy nhất ([`findDetailById`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/ProjectRepository.java#L24-L26)).
   - **Dùng `@BatchSize`**: Bắt buộc áp dụng cho các quan hệ To-Many (`@OneToMany`, `@ManyToMany`) khi có phân trang danh sách.

---

### 1.4. Kiểm Soát Cạnh Tranh Đồng Thời (Concurrency Control): Khóa Lạc Quan (`@Version`) vs Hiểm Họa TOCTOU & Vi Phạm Toàn Vẹn Dữ Liệu (`DataIntegrityViolationException`)

Trong các hệ thống phần mềm doanh nghiệp đa người dùng (Multi-tenant, Multi-threaded Enterprise Systems), các luồng xử lý (threads) liên tục đọc và ghi dữ liệu đồng thời vào cùng một bảng database. Nếu không có cơ chế kiểm soát cạnh tranh chặt chẽ, hệ thống sẽ đối mặt với 2 vấn đề toàn vẹn dữ liệu nghiêm trọng:
1. **Bài toán Cập nhật Bị mất (Lost Update Problem)** khi nhiều người dùng cùng chỉnh sửa một bản ghi hiện có (`UPDATE`/`DELETE`).
2. **Bài toán Xung đột Khóa Duy nhất Đồng thời (Concurrent Unique Violation / TOCTOU)** khi nhiều người dùng cùng tạo mới bản ghi với cùng một mã định danh (`INSERT`).

Dưới đây là bản phân tích kiến trúc chuyên sâu về hai cơ chế phòng vệ tương ứng:

---

#### 1.4.1. Cạnh Tranh Khi Cập Nhật (UPDATE): Khóa Lạc Quan (Optimistic Locking) Với `@Version`

##### A. Bài toán Cập nhật Bị mất (Lost Update Problem)
- **Tình huống thực tế**:
  - Lúc 10:00:00, User A mở chi tiết dự án ID `1` (tên: "EFV", khách hàng: "Customer A", version = `0`).
  - Lúc 10:00:02, User B cũng mở chi tiết dự án ID `1` (cùng nhận version = `0`).
  - Lúc 10:00:05, User A sửa khách hàng thành "Canton de Vaud" và bấm Lưu. Hệ thống ghi nhận thành công, version trong DB nhảy lên `1`.
  - Lúc 10:00:10, User B (vẫn đang nhìn thấy màn hình cũ) sửa tên dự án thành "EFV Tax Portal" và bấm Lưu.
- **Nếu không có khóa**: Lệnh lưu của User B sẽ ghi đè đè bẹp thay đổi "Canton de Vaud" của User A mà không hề hay biết. Thay đổi của User A bị mất vĩnh viễn (Lost Update).

##### B. Tại sao chọn Khóa Lạc Quan (Optimistic Locking) thay vì Khóa Bi Quan (Pessimistic Locking)?
- **Khóa Bi Quan (`Pessimistic Locking` - `SELECT ... FOR UPDATE`)**: 
  - Đặt cờ khóa cứng hàng dữ liệu dưới Database ngay khi User A mở xem. Bất kỳ ai khác (User B) muốn đọc/sửa đều phải chờ (blocking) cho tới khi User A bấm Lưu hoặc tắt trình duyệt.
  - **Nhược điểm nghiêm trọng**: Gây nghẽn kết nối (Connection Pool Starvation), dễ dẫn tới Deadlock, làm giảm sút thảm hại khả năng mở rộng (Scalability) của hệ thống web.
- **Khóa Lạc Quan (`Optimistic Locking`)**:
  - Không hề khóa bất kỳ dòng nào dưới database trong suốt thời gian người dùng thao tác trên màn hình. Cho phép hàng ngàn người dùng cùng đọc đồng thời với tốc độ tối đa.
  - Chỉ kiểm tra phiên bản tại tích tắc duy nhất khi câu lệnh `UPDATE` thực sự được gửi xuống Database.

##### C. Cơ chế hoạt động ngầm dưới Database (Under The Hood)
Trong lớp cơ sở [`AbstractBaseEntity.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/AbstractBaseEntity.java):
```java
@Version
@Column(name = "VERSION", nullable = false)
private Long version;
```
1. Khi câu lệnh cập nhật được thực thi, Hibernate tự động chèn thêm điều kiện so khớp phiên bản vào mệnh đề `WHERE` của câu SQL:
   ```sql
   UPDATE PROJECT 
   SET NAME = 'EFV Tax Portal', CUSTOMER = 'Canton de Vaud', VERSION = 1 
   WHERE ID = 1 AND VERSION = 0;
   ```
2. **Kiểm tra số dòng ảnh hưởng (`affected rows`)**:
   - Nếu User A cập nhật trước: Số dòng tìm thấy là `1` $\rightarrow$ Thành công! Giá trị `VERSION` trong DB nhảy lên `1`.
   - Khi User B gửi yêu cầu cập nhật kèm `VERSION = 0`: Do `VERSION` dưới DB lúc này đã là `1`, câu lệnh `WHERE ID = 1 AND VERSION = 0` không tìm thấy dòng nào $\rightarrow$ **`affected rows = 0`**.
3. **Phản ứng của Hibernate & Spring**:
   - Hibernate phát hiện `affected rows = 0` và lập tức ném ngoại lệ `org.hibernate.StaleObjectStateException`.
   - Spring Framework bắt lấy ngoại lệ này và chuyển đổi (translate) thành `org.springframework.orm.ObjectOptimisticLockingFailureException`.
   - Bộ xử lý lỗi [`GlobalExceptionHandler.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/controller/GlobalExceptionHandler.java) bắt được ngoại lệ này và trả về mã HTTP chuẩn RESTful: **`409 Conflict`** với mã lỗi `OPTIMISTIC_LOCK_ERROR`.

##### D. Tại sao bắt buộc dùng kiểu `Long` cho trường `@Version`?
- Kiểu `Long` (64-bit signed integer) có giá trị cực đại là $2^{63}-1 \approx 9.22 \times 10^{18}$ (hơn 9 tỷ tỷ).
- Nếu sử dụng kiểu `Integer` (32-bit), giá trị tối đa là khoảng $2.14$ tỷ. Trong các hệ thống giao dịch lớn xử lý hàng triệu transaction mỗi ngày, trường version có nguy cơ bị tràn số (integer overflow) quay về số âm sau vài năm vận hành. Kiểu `Long` loại bỏ vĩnh viễn rủi ro này.

##### E. Phân tích sống còn: Tại sao BẮT BUỘC dùng `saveAndFlush()` thay vì `save()` trong `updateProject`?
Trong [`ProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java#L109-L110):
```java
Project updated = projectRepository.saveAndFlush(project);
return modelMapper.map(updated, ProjectDetailResponse.class);
```

Đây là điểm kỹ thuật cực kỳ tinh tế liên quan đến cơ chế **Write-Behind (Trì hoãn ghi)** của Hibernate:
1. **Nếu chỉ gọi `save(project)` thông thường**:
   - Hibernate chỉ lưu trạng thái bẩn (dirty) vào bộ nhớ đệm (Persistence Context / ActionQueue) và **CHƯA HỀ BẮN CÂU SQL `UPDATE` XUỐNG DB**.
   - Vì câu lệnh `UPDATE` chưa chạy, **giá trị thuộc tính `project.version` trong RAM vẫn là `0`**!
   - Đến dòng kế tiếp `modelMapper.map(updated, ProjectDetailResponse.class)`, đối tượng DTO trả về cho client sẽ mang giá trị **`version: 0`**.
   - Khi phương thức kết thúc và Transaction commit, câu lệnh `UPDATE` mới thực sự chạy xuống DB và nâng version trong DB lên **`1`**.
   - 💥 **Hậu quả**: Database lưu `version = 1`, nhưng Frontend lại nhận được `version = 0`. Khi người dùng tiếp tục bấm nút Lưu lần 2 trên giao diện, Frontend gửi lên `version = 0` $\rightarrow$ Backend lập tức báo lỗi **Optimistic Locking giả/ảo** dù không hề có ai khác sửa dự án!
2. **Khi gọi `saveAndFlush(project)`**:
   - Lệnh `flush()` ép Hibernate **bắn ngay lập tức câu SQL `UPDATE` xuống Database** trong thời gian thực thi phương thức.
   - Khi câu SQL `UPDATE` thành công, Hibernate tự động cập nhật thuộc tính trong bộ nhớ Java: **`project.version` nhảy ngay từ `0` lên `1`**.
   - Nhờ đó, hàm `modelMapper.map(...)` lấy được chuẩn xác giá trị **`version: 1`** để trả về cho Frontend trong response.
3. **Kích hoạt lỗi sớm (Fail-Fast)**:
   - Nếu có ai đó đã sửa trước, lệnh `flush()` sẽ làm văng ngoại lệ `OptimisticLockException` **ngay tại dòng code đó**, nằm trọn vẹn trong khối quản lý của Service, thay vì bị trì hoãn tới tận lúc transaction commit ngoài tầm kiểm soát.

---

#### 1.4.2. Cạnh Tranh Khi Tạo Mới (INSERT): Hiểm Họa TOCTOU & `DataIntegrityViolationException`

##### A. Lỗ hổng TOCTOU (Time-Of-Check to Time-Of-Use) là gì?
TOCTOU là một dạng lỗi cạnh tranh đồng thời (Race Condition) kinh điển trong an ninh phần mềm và hệ thống cơ sở dữ liệu. Nó xảy ra khi có một khoảng trễ thời gian giữa:
- **Thời điểm kiểm tra điều kiện (Time of Check)**: Mã nguồn kiểm tra xem tài nguyên có hợp lệ/tồn tại không.
- **Thời điểm sử dụng/ghi dữ liệu (Time of Use)**: Mã nguồn thực hiện ghi dữ liệu dựa trên giả định rằng kết quả kiểm tra trước đó vẫn còn đúng.

##### B. Phân tích 2 kịch bản trong `createProject`:
Trong [`ProjectServiceImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/service/impl/ProjectServiceImpl.java#L61-L85):
```java
// Bước 1: Time of Check
if (projectRepository.existsByProjectNumber(request.getProjectNumber())) {
    throw new ProjectNumberAlreadyExistsException(request.getProjectNumber());
}

// ... chuẩn bị dữ liệu ...

// Bước 2: Time of Use
Project saved = projectRepository.save(project);
```

###### Kịch bản 1: Luồng tuần tự bình thường (99.9% trường hợp)
1. Người dùng A tạo dự án với `projectNumber = 1001` (số này đã có sẵn trong DB).
2. Lệnh `projectRepository.existsByProjectNumber(1001)` truy vấn DB và trả về `true`.
3. Khối lệnh `if` lập tức ném ra ngoại lệ nghiệp vụ:
   ```java
   throw new ProjectNumberAlreadyExistsException(1001);
   ```
4. **Luồng xử lý dừng lại ngay lập tức tại đây**. Mã nguồn hoàn toàn chưa bao giờ chạm tới câu lệnh `save()` hay câu SQL `INSERT`.
5. Database không hề nhận lệnh ghi nào, nên **không bao giờ xảy ra lỗi vi phạm toàn vẹn dữ liệu**.
6. Hệ thống trả về mã lỗi HTTP `400 Bad Request` với message rõ ràng: *"The project number already existed: 1001"*.

###### Kịch bản 2: Luồng cạnh tranh đồng thời (Race Condition - Lọt qua cửa kiểm tra)
Đây là kịch bản duy nhất khiến `DataIntegrityViolationException` phát sinh trên `PROJECT_NUMBER`:

```
   [Luồng A (Thread 1)]                                      [Luồng B (Thread 2)]
            │                                                         │
1. Gửi request tạo: 9999                                   1. Gửi request tạo: 9999
            │                                                         │
2. Hỏi DB: existsByProjectNumber(9999)?                    2. Hỏi DB: existsByProjectNumber(9999)?
   ──► DB trả về FALSE (Chưa có!)                             ──► DB trả về FALSE (Chưa có!)
            │                                                         │
   VƯỢT QUA CỔNG CHECK! ✅                                     VƯỢT QUA CỔNG CHECK! ✅
            │                                                         │
3. Bắn SQL: INSERT INTO PROJECT(..., 9999)                            │
   ──► Ghi thành công vào Database!                                   │
            │                                                         │
            │                                              3. Bắn SQL: INSERT INTO PROJECT(..., 9999)
            │                                                 ──► 💥 DATABASE TỪ CHỐI!
            │                                                     Vi phạm UNIQUE CONSTRAINT
            │                                                     trên cột PROJECT_NUMBER!
            ▼                                                         ▼
       HTTP 201 Created                                    Ném DataIntegrityViolationException
```

Vì Luồng B kiểm tra DB tại thời điểm Luồng A **chưa kịp ghi xong**, cả hai luồng đều nhận kết quả kiểm tra là `false` (hợp lệ). Cổng kiểm tra `existsBy...` bị vô hiệu hóa hoàn toàn bởi khoảng trễ mili-giây.

Khi Luồng B thực sự thực hiện lệnh `INSERT`, cơ chế bảo vệ cứng của Database mới kích hoạt:
1. Engine cơ sở dữ liệu kiểm tra cây chỉ mục B-Tree của ràng buộc Unique trên cột `PROJECT_NUMBER`.
2. Phát hiện khóa `9999` đã được Luồng A ghi trước đó.
3. Database hủy câu lệnh và ném mã lỗi vi phạm toàn vẹn (H2/PostgreSQL mã `23505`, Oracle mã `ORA-00001`).
4. JDBC Driver đẩy ngoại lệ lên Hibernate `ConstraintViolationException`.
5. Spring Framework bọc thành:
   ```java
   org.springframework.dao.DataIntegrityViolationException
   ```

##### C. Tại sao `@Column(unique = true)` ở Entity không tự kiểm tra trước?
- **Bản chất của JPA Annotation**: Thuộc tính `unique = true` của `@Column` chỉ đóng vai trò là **siêu dữ liệu phát sinh DDL (Data Definition Language)**. Nó hướng dẫn Hibernate sinh câu lệnh:
  ```sql
  ALTER TABLE PROJECT ADD CONSTRAINT UK_PROJECT_NUMBER UNIQUE (PROJECT_NUMBER);
  ```
- **Không có In-Memory Validation**: JPA/Hibernate hoàn toàn **không có cơ chế tự động truy vấn kiểm tra trùng lặp trong bộ nhớ Java trước khi ghi**. Việc phát hiện trùng lặp là trách nhiệm duy nhất của Database Engine khi nhận lệnh `INSERT`.

##### D. Vai trò: Cổng Soát Vé Mềm vs Bức Tường Lửa Cứng
| Thành phần | Cấp độ | Vai trò kiến trúc |
| :--- | :---: | :--- |
| **`existsByProjectNumber`** | **Tầng Ứng Dụng (Application Layer)** | **Cổng soát vé mềm (Fail-Fast)**: Ngăn chặn 99.9% trường hợp trùng lặp trong luồng thông thường để trả về thông báo lỗi thân thiện, tiết kiệm tài nguyên kết nối DB. Bị vô hiệu hóa khi có Race Condition. |
| **`UNIQUE Constraint` dưới DB** | **Tầng Cơ Sở Dữ Liệu (Database Layer)** | **Bức tường lửa cứng (Single Source of Truth)**: Chốt chặn an ninh tối hậu không thể xuyên thủng. Đảm bảo 100% dữ liệu không bao giờ bị trùng lặp ngay cả khi có hàng trăm luồng đồng thời vượt qua cổng soát vé mềm. |

##### E. Các nguồn gốc khác gây ra `DataIntegrityViolationException`
Không chỉ có `projectNumber`, ngoại lệ `DataIntegrityViolationException` đại diện cho toàn bộ các vi phạm quy tắc toàn vẹn dữ liệu trong cơ sở dữ liệu quan hệ:
1. **Vi phạm Khóa Ngoại (`FOREIGN KEY Constraint Violation`)**: Khi xóa một `Group` mà vẫn còn các `Project` đang tham chiếu tới `GROUP_ID` đó, hoặc chèn `Project` với `GROUP_ID` không tồn tại.
2. **Vi phạm Ràng Buộc Khác Rỗng (`NOT NULL Constraint Violation`)**: Khi một trường được cấu hình `nullable = false` dưới DB nhưng câu lệnh SQL truyền giá trị `NULL`.
3. **Cắt Cụt Dữ Liệu (`Data Truncation / Value Too Large`)**: Khi dữ liệu chuỗi gửi xuống vượt quá kích thước tối đa của cột DB (ví dụ cột `CUSTOMER VARCHAR(50)` nhưng truyền chuỗi 100 ký tự).

---

#### 1.4.3. So Sánh Bản Chất: `OptimisticLockingFailureException` vs `DataIntegrityViolationException`

Rất nhiều kỹ sư thường nhầm lẫn giữa hai loại ngoại lệ này. Bảng dưới đây đối chiếu chi tiết:

| Đặc tính so sánh | Khóa Lạc Quan (`OptimisticLockingFailureException`) | Vi Phạm Toàn Vẹn Dữ Liệu (`DataIntegrityViolationException`) |
| :--- | :--- | :--- |
| **Bản chất nghiệp vụ** | Tranh chấp phiên bản sửa đổi đồng thời trên **cùng một bản ghi đã tồn tại**. | Vi phạm tính duy nhất hoặc quy tắc quan hệ khi ghi dữ liệu (thường gặp khi **tạo mới đồng thời**). |
| **Câu lệnh SQL phát sinh** | `UPDATE ... WHERE ID = ? AND VERSION = ?` | `INSERT INTO ...` hoặc `DELETE FROM ...` (dính khóa ngoại) |
| **Thành phần phát hiện lỗi** | **Hibernate ORM**: Nhận kết quả từ JDBC Driver thấy `affected rows = 0`. | **Database Engine**: Phát hiện vi phạm Unique B-Tree Index hoặc Foreign Key Table. |
| **Mã lỗi Database** | Không có mã lỗi DB (câu lệnh SQL chạy hoàn toàn hợp lệ nhưng tìm thấy 0 dòng). | Có mã lỗi DB rõ ràng (H2/Postgres: `23505`, Oracle: `ORA-00001`, MySQL: `1062`). |
| **Vai trò của `@Version`** | Là thành phần cốt lõi để so khớp phiên bản. | Hoàn toàn không liên quan (không kiểm tra version trong `INSERT`). |
| **Mã HTTP Status Code** | **`409 Conflict`** (Chuẩn RESTful cho xung đột phiên bản). | **`409 Conflict`** (nếu trùng Unique do race condition) hoặc **`400 Bad Request`**. |

---

#### 1.4.4. Thiết Kế Xử Lý Lỗi Tập Trung Chuẩn Mực Trong `GlobalExceptionHandler`

Để tránh việc lỗi `DataIntegrityViolationException` trôi xuống hàm bắt ngoại lệ chung `Exception.class` và trả về mã `500 Internal Server Error`, [`GlobalExceptionHandler.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/controller/GlobalExceptionHandler.java) cần xử lý riêng biệt cả 2 trường hợp:

```java
// 1. Xử lý xung đột phiên bản khi Cập nhật (Khóa Lạc Quan)
@ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
public ResponseEntity<ErrorResponse> handleOptimisticLock(Exception ex, Locale locale) {
    log.warn("Optimistic lock conflict: {}", ex.getMessage());
    String message = getLocalizedMessage("error.optimistic.lock", 
            "The project has been modified by another user. Please refresh and try again.", locale);

    ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value()) // HTTP 409
            .errorCode("OPTIMISTIC_LOCK_ERROR")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
}

// 2. Xử lý vi phạm toàn vẹn dữ liệu khi Tạo mới đồng thời (Race Condition TOCTOU)
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, Locale locale) {
    log.warn("Data integrity violation (concurrent race condition): {}", ex.getMessage());

    String message = getLocalizedMessage("error.project.number.exist", 
            "The project number already existed", locale);

    ErrorResponse errorResponse = ErrorResponse.builder()
            .status(HttpStatus.CONFLICT.value()) // HTTP 409 Conflict chuẩn RESTful
            .errorCode("PROJECT_NUMBER_ALREADY_EXISTS")
            .message(message)
            .timestamp(LocalDateTime.now())
            .build();

    return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
}
```

---

#### 1.4.5. Phương Pháp Kiểm Thử Tái Hiện (Simulation / Reproduction Guide)
Chi tiết các bước thực hành mô phỏng chính xác lỗi `DataIntegrityViolationException` bằng phương pháp **IntelliJ Debugger + H2 Web Console + Bruno API Client** được hướng dẫn đầy đủ tại:
👉 [`HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/HUONG_DAN_REPRODUCE_DATA_INTEGRITY_TOCTOU.md).

---

### 1.5. Tối Ưu Phân Trang: Bản Chất `Pageable`, `Page<T>` vs `Slice<T>` & Cơ Chế Thực Thi SQL

#### 1.5.1. Bản chất của Interface `Pageable` & Cấu Trúc Các Trường
`Pageable` là một interface trừu tượng trong Spring Data (`org.springframework.data.domain.Pageable`) đóng gói toàn bộ yêu cầu về phân trang và sắp xếp. Lớp triển khai phổ biến nhất là `PageRequest`.

Một đối tượng `Pageable` bao gồm các trường và phương thức cốt lõi:

| Thành phần trong `Pageable` | Kiểu dữ liệu | Ý nghĩa & Cách tính toán |
|---|---|---|
| `pageNumber` | `int` | Chỉ số trang được yêu cầu (**0-indexed**: trang 1 gửi lên là `0`, trang 2 là `1`). |
| `pageSize` | `int` | Số lượng bản ghi tối đa mong muốn lấy về trong một trang (ví dụ `10`, `20`). |
| `offset` | `long` | Vị trí bắt đầu đọc dữ liệu (số dòng cần bỏ qua). Tính theo công thức: <br>$$\text{offset} = (\text{long}) \text{pageNumber} \times \text{pageSize}$$ |
| `sort` | `Sort` | Đối tượng mô tả danh sách các trường cần sắp xếp và chiều sắp xếp (`ASC`/`DESC`). |
| `isPaged()` / `isUnpaged()` | `boolean` | Xác định xem request này có yêu cầu phân trang hay lấy toàn bộ tập dữ liệu. |
| `hasPrevious()` | `boolean` | Kiểm tra xem có trang trước đó hay không (`pageNumber > 0`). |
| `next()` | `Pageable` | Trả về đối tượng `Pageable` mới cho trang kế tiếp (`pageNumber + 1`). |
| `previousOrFirst()` | `Pageable` | Trả về đối tượng `Pageable` cho trang trước, hoặc trang đầu tiên nếu đang ở trang 0. |

*Cách khởi tạo trong mã Java*:
```java
Pageable pageable = PageRequest.of(0, 10, Sort.by("projectNumber").ascending());
Pageable unpaged = Pageable.unpaged();
```

#### 1.5.2. Vòng Đời Chuyển Hóa: Từ HTTP Request Đến Controller
Khi người dùng duyệt bảng ở Frontend, trình duyệt gửi HTTP Request kèm các query parameters:
```http
GET /projects?page=1&size=10&sort=name,asc&sort=projectNumber,desc
```

Quá trình chuyển hóa diễn ra hoàn toàn tự động:
1. **`PageableHandlerMethodArgumentResolver`**:
   Spring MVC kích hoạt bộ giải quyết tham số (Argument Resolver) để phân tích URL:
   - Đọc tham số `page=1` (nếu không truyền, mặc định là `0`).
   - Đọc tham số `size=10` (nếu không truyền, mặc định là `20`).
   - Đọc tham số `sort=name,asc` và `sort=projectNumber,desc`.
2. **Khởi tạo `PageRequest`**:
   Spring tự động đóng gói các giá trị này thành một instance `PageRequest` hợp lệ và tiêm (inject) trực tiếp vào tham số của Controller method:
   ```java
   @GetMapping
   public ResponseEntity<Page<ProjectListResponse>> searchProjects(
           SearchProjectCriteria criteria,
           Pageable pageable) { ... }
   ```
   Lập trình viên không cần viết bất kỳ dòng mã thủ công nào để bóc tách chuỗi URL hay tính toán `offset`.

#### 1.5.3. Cơ Chế Dưới Database: `Pageable` Biến Thành Câu Query Như Thế Nào?
Khi Repository trả về một đối tượng `Page<T>`, Spring Data JPA và Hibernate phối hợp thực thi **2 CÂU LỆNH SQL ĐỘC LẬP**:

```
                       Pageable Được Truyền Xuống Database
                                       │
                 ┌─────────────────────┴─────────────────────┐
                 ▼                                           ▼
       [ CÂU SQL 1: LẤY DATA ]                    [ CÂU SQL 2: ĐẾM TỔNG SỐ ]
  SELECT p.* FROM PROJECT p                  SELECT COUNT(p.ID) FROM PROJECT p
  WHERE ...                                  WHERE ...
  ORDER BY p.NAME ASC, p.PROJECT_NUMBER DESC (TỰ ĐỘNG BỎ ORDER BY ĐỂ TỐI ƯU HÓA)
  LIMIT 10 OFFSET 10;
                 │                                           │
                 └─────────────────────┬─────────────────────┘
                                       ▼
                       Đóng gói thành Page<T> hoàn chỉnh:
                       - content: 10 phần tử từ Query 1
                       - totalElements: kết quả đếm từ Query 2
                       - totalPages: ceil(totalElements / pageSize)
```

1. **Câu SQL 1: Lấy dữ liệu của trang (Data Query)**:
   Hibernate dịch `pageable` thành mệnh đề phân trang tương ứng với chuẩn của từng hệ quản trị CSDL (SQL Dialect):
   - **H2 / PostgreSQL / MySQL**:
     ```sql
     SELECT p.* FROM PROJECT p 
     WHERE p.STATUS = 'NEW' 
     ORDER BY p.NAME ASC, p.PROJECT_NUMBER ASC 
     LIMIT 10 OFFSET 10;
     ```
     - `LIMIT` = `pageable.getPageSize()` (10)
     - `OFFSET` = `pageable.getOffset()` (Trang 1: $1 \times 10 = 10$).
   - **Oracle 12c+ / Microsoft SQL Server 2012+**:
     ```sql
     SELECT p.* FROM PROJECT p 
     ORDER BY p.NAME ASC, p.PROJECT_NUMBER ASC 
     OFFSET 10 ROWS FETCH NEXT 10 ROWS ONLY;
     ```
   - *Hiệu năng*: Câu lệnh này chỉ đọc đúng 10 bản ghi từ đĩa/bộ đệm, chạy cực kỳ nhanh khi có Index hỗ trợ sắp xếp.

2. **Câu SQL 2: Đếm tổng số bản ghi (Count Query)**:
   ```sql
   SELECT COUNT(p.ID) FROM PROJECT p WHERE p.STATUS = 'NEW';
   ```
   - **Tại sao câu đếm lại TỰ ĐỘNG BỎ HOÀN TOÀN mệnh đề `ORDER BY`?**:
     Việc đếm tổng số dòng thỏa mãn điều kiện không hề phụ thuộc vào thứ tự các dòng. Loại bỏ `ORDER BY` giúp Database Optimizer không phải tốn tài nguyên sắp xếp dữ liệu (Sort Buffer / Disk Temp Table), giúp câu đếm chạy nhanh hơn nhiều lần.
   - Kết quả đếm được dùng để tính:
     $$\text{totalElements} = \text{kết quả COUNT}$$
     $$\text{totalPages} = \left\lceil \frac{\text{totalElements}}{\text{pageSize}} \right\rceil$$

#### 1.5.4. Cơ Chế Tối Ưu Thông Minh Của `PageableExecutionUtils.getPage(...)`
Trong dự án tại [`ProjectRepositoryImpl.java`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/repository/custom/ProjectRepositoryImpl.java), chúng ta sử dụng `PageableExecutionUtils`:
```java
return PageableExecutionUtils.getPage(
        content,
        pageable != null ? pageable : Pageable.unpaged(),
        countQuery::fetchOne
);
```

`PageableExecutionUtils` có cơ chế tối ưu thông minh giúp tiết kiệm **50% số câu query**:
- **Trường hợp 1 (Trang đầu tiên và không đầy trang)**:
  Nếu đang ở trang đầu tiên (`offset == 0`) và số lượng phần tử lấy về `content.size() < pageSize` (ví dụ `pageSize = 10` nhưng chỉ tìm thấy 6 bản ghi):
  $\rightarrow$ `PageableExecutionUtils` **HOÀN TOÀN KHÔNG CHẠY CÂU COUNT QUERY**! Nó suy ra luôn `totalElements = content.size() = 6`. Tiết kiệm được một truy vấn `COUNT(*)` tốn kém xuống database.
- **Trường hợp 2 (Trang cuối cùng)**:
  Nếu `offset > 0` và số phần tử lấy về `content.size() < pageSize`:
  $\rightarrow$ Nó tự tính `totalElements = offset + content.size()` mà không cần gọi `countQuery::fetchOne`.
- **Trường hợp 3 (`Pageable.unpaged()`)**:
  Bỏ qua câu count query vì toàn bộ dữ liệu đã nằm trong `content`.

#### 1.5.5. So Sánh Khi Dùng `Slice<T>` Thay Vì `Page<T>`
Khi chuyển kiểu trả về từ `Page<T>` sang `Slice<T>` (áp dụng cho Autocomplete dropdown tại `GroupController` và `EmployeeController`):
- Spring Data thực thi **DUY NHẤT 1 CÂU SQL**:
  ```sql
  SELECT g.* FROM "GROUP" g LIMIT 11 OFFSET 0;
  ```
- **Kỹ thuật `LIMIT size + 1`**: Khi bạn yêu cầu `size = 10`, Spring Data sẽ ngầm yêu cầu DB lấy **11 bản ghi**:
  - Nếu DB trả về đủ 11 bản ghi: Spring Data biết chắc chắn vẫn còn dữ liệu phía sau $\rightarrow$ gán `hasNext() = true`, gỡ bỏ bản ghi thứ 11 và trả về đúng 10 bản ghi cho client.
  - Nếu DB trả về $\le 10$ bản ghi: Spring Data biết đã hết dữ liệu $\rightarrow$ gán `hasNext() = false`.
- **Lợi ích vượt trội**: Hoàn toàn **KHÔNG BAO GIỜ chạy câu lệnh `SELECT COUNT(*)`**, giải phóng tải triệt để cho database.

#### 1.5.6. Cấu Trúc Toàn Bộ Các Trường Trong Đối Tượng `Page<T>`
Khi backend trả về một đối tượng `Page<ProjectListResponse>` ra ngoài REST Controller, Jackson sẽ chuyển đổi thành cấu trúc JSON với đầy đủ 11 trường:

| Tên trường (Field) | Kiểu dữ liệu | Ý nghĩa chi tiết |
|---|---|---|
| `content` | `List<T>` | Danh sách các phần tử dữ liệu thực tế thuộc trang hiện tại. |
| `pageable` | `Pageable` | Chứa thông tin cấu hình phân trang được yêu cầu (`pageNumber`, `pageSize`, `offset`, `sort`). |
| `totalElements` | `long` | Tổng số lượng bản ghi thỏa mãn điều kiện lọc trên toàn bộ cơ sở dữ liệu (được tính từ câu `COUNT(*)`). |
| `totalPages` | `int` | Tổng số trang phân chia được: $\lceil \text{totalElements} / \text{pageSize} \rceil$. |
| `size` | `int` | Số lượng phần tử tối đa cho phép trên 1 trang (chính là `pageSize`). |
| `number` | `int` | Chỉ số trang hiện tại (**0-indexed**: trang đầu tiên là `0`, trang thứ hai là `1`). |
| `numberOfElements` | `int` | Số lượng phần tử thực tế hiện diện trên trang này (trang cuối cùng có thể có ít phần tử hơn `size`). |
| `first` | `boolean` | Cờ báo hiệu đây có phải là trang đầu tiên hay không (`number == 0`). |
| `last` | `boolean` | Cờ báo hiệu đây có phải là trang cuối cùng hay không (`number == totalPages - 1`). |
| `empty` | `boolean` | Cờ báo hiệu trang này có dữ liệu hay không (`content.isEmpty()`). |
| `sort` | `Sort` | Đối tượng mô tả chi tiết thông tin sắp xếp (`sorted`, `unsorted`, `orders`). |

---

### 1.6. Cơ Chế Sinh Khóa Chính: `SEQUENCE` vs `IDENTITY` & Cơ Chế Write-Behind

#### Nhận định chính xác về `SEQUENCE` vs `IDENTITY`
Khác biệt cốt tử giữa `SEQUENCE` và `IDENTITY` nằm ở cách chúng tương tác với **Persistence Context (Bộ nhớ đệm cấp 1)** và cơ chế **Write-Behind (Trì hoãn ghi)** của Hibernate:

```
[ Chiến lược IDENTITY ]
  persist(entity) ──► BẮT BUỘC PHẢI THỰC THI NGAY INSERT VÀO DB ──► DB sinh ID & trả về
                       (VÔ HIỆU HÓA HOÀN TOÀN BATCH INSERT & WRITE-BEHIND)

[ Chiến lược SEQUENCE ]
  persist(entity) ──► Gọi SELECT NEXT VALUE FOR SEQ ──► Nhận ID trước
                      Entity chuyển thành MANAGED trong RAM
                      Lệnh INSERT được trì hoãn gom vào lô (Batch Insert) khi Commit Transaction!
```

1. **Với `GenerationType.IDENTITY`**:
   - ID được sinh ra bởi chính cột tự tăng của bảng (`AUTO_INCREMENT` / `IDENTITY`).
   - Để biết được ID vừa sinh là bao nhiêu (nhằm gán vào trường `id` của đối tượng Java và đưa vào quản lý trong Persistence Context), Hibernate **bắt buộc phải bắn lệnh SQL INSERT xuống Database ngay tại thời điểm gọi `entityManager.persist(entity)`**.
   - Hậu quả: Cơ chế trì hoãn ghi (Write-Behind) bị phá vỡ hoàn toàn. Hibernate **không thể gom nhiều lệnh INSERT thành một đợt (Batch Insert)**, khiến hiệu năng ghi hàng loạt bị giảm sút nghiêm trọng.
2. **Với `GenerationType.SEQUENCE`**:
   - Sequence là một đối tượng cơ sở dữ liệu độc lập (`CREATE SEQUENCE hibernate_sequence ...`), tồn tại tách biệt với các bảng.
   - Khi gọi `persist(entity)`, Hibernate chỉ cần gọi lệnh lấy giá trị tiếp theo từ sequence (`SELECT NEXT VALUE FOR ...`). Entity ngay lập tức nhận được ID duy nhất và chuyển sang trạng thái `MANAGED` hoàn toàn trong bộ nhớ RAM mà **chưa cần insert bất kỳ dòng nào vào bảng**.
   - Đến thời điểm transaction commit, Hibernate sẽ gom toàn bộ các đối tượng cần insert lại và thực thi qua một kết nối JDBC batch duy nhất.
   - Kết hợp với thuật toán `pooled-lo` optimizer: Hibernate có thể lấy trước 50 ID chỉ trong 1 lần gọi sequence, giảm thiểu tối đa độ trễ mạng.
3. **Cơ sở dữ liệu H2 có hỗ trợ `SEQUENCE` không?**:
   - **Chắc chắn 100% là CÓ**. H2 hỗ trợ native Sequence từ các phiên bản sơ khai đến phiên bản 2.x mới nhất. Câu lệnh `CREATE SEQUENCE hibernate_sequence START WITH 1000 INCREMENT BY 1;` là câu lệnh chuẩn ANSI SQL được H2 thực thi hoàn hảo. Toàn bộ 64 bài test tự động của dự án đã chạy kiểm chứng thành công trên H2 In-Memory.

---

### 1.7. Thiết Kế Thực Thể Cơ Sở (`@MappedSuperclass`) & Ràng Buộc Khóa Chính

Trong dự án, các entity [`Project`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/Project.java), [`Group`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/Group.java), [`Employee`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/Employee.java) đều kế thừa từ [`AbstractEntity`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/AbstractEntity.java):

```java
@MappedSuperclass
public abstract class AbstractEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_generator")
    @SequenceGenerator(name = "seq_generator", sequenceName = "hibernate_sequence", allocationSize = 1)
    @Column(updatable = false, precision = 19, scale = 0)
    private Long id;

    @Version
    private Long version;
}
```

#### `@MappedSuperclass` là gì và có tác dụng như thế nào?
- Đánh dấu một lớp cha chứa các thuộc tính dùng chung (như `id`, `version`, `createdAt`, `updatedAt`).
- **Khác với `@Entity` thông thường**: Lớp cha có `@MappedSuperclass` **không có bảng riêng trong cơ sở dữ liệu** và không thể thực hiện truy vấn đa hình `SELECT e FROM AbstractEntity e`.
- **Tác dụng**: Toàn bộ các trường của lớp cha cùng các annotation ánh xạ JPA (`@Id`, `@Version`, `@Column`) sẽ được "nhúng thẳng" (inherit & map) vào từng bảng của các thực thể con kế thừa nó (`PROJECT`, `\"GROUP\"`, `EMPLOYEE`). Giúp tái sử dụng mã nguồn và tuân thủ triệt để nguyên lý DRY (Don't Repeat Yourself).

#### Tại sao lại cần `serialVersionUID = 1L;`?
- Interface `Serializable` cho phép chuyển đổi một đối tượng Java thành luồng byte (Serialization) để lưu trữ (cache Redis, HTTP Session) hoặc truyền qua mạng.
- `serialVersionUID` đóng vai trò là "mã định danh phiên bản" của cấu trúc lớp:
  - Nếu lập trình viên **không khai báo tường minh**, JVM sẽ tự động tính toán một giá trị băm (hash) dựa trên toàn bộ tên trường, kiểu dữ liệu, phương thức của class tại thời điểm biên dịch.
  - Rủi ro chí tử: Chỉ cần thêm hoặc sửa một method nhỏ, mã hash tự động sẽ thay đổi. Khi giải tuần tự hóa (Deserialization) dữ liệu đã lưu trước đó, JVM sẽ ném ra ngoại lệ `java.io.InvalidClassException: local class incompatible` làm sập ứng dụng.
  - Khai báo cứng `private static final long serialVersionUID = 1L;` đảm bảo khả năng tương thích ngược (Backward Compatibility) và kiểm soát an toàn tuyệt đối tiến trình serialize.

#### Thuộc tính `updatable = false` trên `@Column`: Ràng buộc cụ thể kiểu gì?
- Đây là chỉ thị cấp tầng ORM (Hibernate Metadata Instruction).
- **Cơ chế hoạt động**: Khi Entity bị chỉnh sửa và Hibernate sinh câu lệnh SQL `UPDATE`, cột được đánh dấu `updatable = false` sẽ **BỊ LOẠI BỎ HOÀN TOÀN KHỎI MỆNH ĐỀ `SET`**:
  ```sql
  UPDATE PROJECT SET NAME = ?, CUSTOMER = ?, VERSION = ? WHERE ID = ?;
  ```
- **Ý nghĩa bảo vệ**: Ngay cả khi lập trình viên vô tình gọi `project.setId(999L)` trong mã Java, Hibernate cũng không bao giờ đưa `ID = 999` vào lệnh `UPDATE`. Khóa chính của dòng trong cơ sở dữ liệu được bảo vệ bất biến 100%.

#### Thuộc tính `precision = 19, scale = 0`: Ý nghĩa số học
- `precision = 19`: Tổng số lượng chữ số tối đa (Total number of digits bao gồm cả phần nguyên và phần thập phân) là 19 chữ số.
- `scale = 0`: Số lượng chữ số nằm sau dấu phẩy (phần thập phân) là 0 chữ số.
- **Bản chất**: Đây là định nghĩa một số nguyên thuần túy có độ dài tối đa 19 chữ số, khớp hoàn hảo với kiểu `BIGINT` trong chuẩn SQL và kiểu `Long` trong Java (khoảng giá trị của Long: $-9,223,372,036,854,775,808$ đến $9,223,372,036,854,775,807$, đúng 19 chữ số).

---

### 1.8. Quản Lý Quan Hệ Hai Chiều (Bidirectional) & Tại Sao Cần `@Setter(AccessLevel.NONE)`

Trong các thực thể có quan hệ hai chiều như `Project` và `Employee`:
```java
public class Project extends AbstractEntity {
    @ManyToMany
    @JoinTable(...)
    @Setter(AccessLevel.NONE)
    private Set<Employee> employees = new HashSet<>();
}
```

#### Tại sao phải vô hiệu hóa setter mặc định bằng `@Setter(AccessLevel.NONE)`?
1. **Rủi ro phá vỡ tính toàn vẹn bộ nhớ**: Nếu dùng `@Setter` công khai mặc định của Lombok, người khác có thể gọi `project.setEmployees(newEmployees)`. Lệnh này gán đè một Set mới vào `Project`, nhưng các đối tượng `Employee` bên trong lại không hề hay biết rằng mình đã bị thêm vào hoặc gỡ bỏ khỏi Project đó.
2. **Cơ chế đồng bộ thủ công hai chiều (Convenience Methods)**:
   Để đảm bảo cả 2 đầu của mối quan hệ trong Persistence Context luôn nhất quán trước khi flush xuống DB, chúng ta chặn setter và tự viết các hàm nghiệp vụ:
   ```java
   public void addEmployee(Employee employee) {
       this.employees.add(employee);
       employee.getProjects().add(this);
   }

   public void removeEmployee(Employee employee) {
       this.employees.remove(employee);
       employee.getProjects().remove(this);
   }
   ```

---

### 1.9. Bản Chất Của Hibernate Proxy & `@JsonIgnoreProperties`

Khi một Entity con được cấu hình `FetchType.LAZY` (ví dụ `group` trong `Project`):
```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "GROUP_ID")
private Group group;
```

#### Hibernate Proxy là gì?
- Khi bạn nạp `Project` từ database, Hibernate **không** truy vấn bảng `\"GROUP\"` ngay.
- Thay vào đó, Hibernate sử dụng thư viện Bytecode (ByteBuddy / CGLIB) để tạo ra một **lớp con giả lập chạy trong RAM** kế thừa từ `Group`, gọi là **Hibernate Proxy Object**.
- Đối tượng Proxy này chỉ chứa ID của Group và giữ 2 trường kỹ thuật nội bộ của framework:
  1. `hibernateLazyInitializer`: Lưu trạng thái phiên session, entity name, identifier để sẵn sàng truy vấn khi cần.
  2. `handler`: Bộ chặn (MethodInterceptor) để khi ai đó gọi `group.getName()`, nó sẽ kích hoạt câu lệnh SQL nạp dữ liệu thật từ DB.

#### Tại sao lại cần `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`?
- Jackson ObjectMapper khi serialize đối tượng Java sang chuỗi JSON sẽ dùng Reflection quét **tất cả các getter**.
- Khi quét trúng đối tượng Proxy, Jackson sẽ cố đọc `getHibernateLazyInitializer()` và `getHandler()`.
- Hậu quả nghiêm trọng: Jackson không biết cách biến 2 đối tượng kỹ thuật phức tạp này thành JSON, dẫn đến ném ngoại lệ:
  ```text
  com.fasterxml.jackson.databind.exc.InvalidDefinitionException: 
  No serializer found for class org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor
  ```
- Thêm `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})` là chỉ thị trực tiếp cho Jackson: *"Hãy bỏ qua 2 thuộc tính kỹ thuật nội bộ của Hibernate Proxy này, không được đưa chúng vào chuỗi JSON!"*.

---

### 1.10. Mẫu Thiết Kế Builder Pattern (`@Builder`)

#### Builder Pattern là gì?
Builder Pattern là một mẫu thiết kế thuộc nhóm Creational (khởi tạo đối tượng). Nó giải quyết triệt để vấn nạn **Telescoping Constructor Anti-pattern** (Constructor có quá nhiều tham số).

#### Ví dụ minh họa:
Giả sử lớp `Project` có 8 thuộc tính. Nếu dùng constructor truyền thống:
```java
Project p = new Project(1001, "EFV", "Customer A", ProjectStatus.NEW, date1, date2, group, employees);
```
- Nhược điểm: Lập trình viên rất dễ đặt nhầm thứ tự giữa 2 chuỗi String (nhầm tên dự án với tên khách hàng) hoặc giữa 2 ngày tháng mà trình biên dịch không hề báo lỗi.
- Khi dùng `@Builder` của Lombok (Fluent API style):
  ```java
  Project p = Project.builder()
          .projectNumber(1001)
          .name("EFV")
          .customer("Customer A")
          .status(ProjectStatus.NEW)
          .startDate(LocalDate.now())
          .build();
  ```
- **Lợi ích**:
  1. **Rõ ràng, tường minh**: Mỗi giá trị được gán đều đi kèm với tên hàm tương ứng, loại bỏ 100% khả năng nhầm lẫn vị trí tham số.
  2. **Linh hoạt**: Chỉ cần gán những thuộc tính cần thiết, các thuộc tính còn lại tự nhận giá trị mặc định mà không cần viết hàng chục constructor nạp chồng (overload).
  3. **Bất biến (Immutability)**: Cho phép tạo các đối tượng DTO bất biến chỉ có getter mà không cần setter.

---

### 1.11. Cơ Sở Dữ Liệu: Chỉ Mục B-Tree & Phân Tích Kế Hoạch Thực Thi (EXPLAIN ANALYZE)

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

### 1.12. Nguyên Tắc Thiết Kế RESTful API Chuẩn Mực

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

### 1.13. Cơ Chế Xử Lý Lỗi Tập Trung (Global Exception Handling)

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

### 1.14. Cơ Chế Kiểm Tra Ràng Buộc Dữ Liệu (Bean Validation JSR-380)

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
- **Ý nghĩa & Bản chất**: Meta-annotation tổng hợp của 3 annotation cốt lõi:
  - `@SpringBootConfiguration`: Khai báo class là Spring Configuration class.
  - `@EnableAutoConfiguration`: Kích hoạt cơ chế tự động cấu hình bean dựa trên thư viện trong classpath.
  - `@ComponentScan`: Quét toàn bộ package hiện tại và các package con để đăng ký các Spring Bean.

#### `@Configuration`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Đánh dấu class chứa các định nghĩa bean (thông qua các method `@Bean`). Spring CGLIB proxy sẽ chặn các lời gọi method `@Bean` bên trong class này để đảm bảo nguyên lý Singleton.

#### `@Bean`
- **Vị trí**: Cấp Method trong class `@Configuration`.
- **Ý nghĩa & Bản chất**: Chỉ định đối tượng trả về từ method sẽ được quản lý bởi Spring IoC Container. Ví dụ: Cấu hình bean `ModelMapper`.

#### `@Component`, `@Service`, `@Repository`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**:
  - `@Component`: Đánh dấu class thông thường là Spring Bean.
  - `@Service`: Đặc tả hóa của `@Component`, đại diện cho tầng Business Service.
  - `@Repository`: Đặc tả hóa của `@Component`, đại diện cho tầng Data Access. Tự động dịch chuyển các ngoại lệ JDBC/Hibernate thành hệ thống `DataAccessException` của Spring.

#### `@Autowired`
- **Vị trí**: Cấp Field, Constructor hoặc Setter.
- **Ý nghĩa & Bản chất**: Yêu cầu Spring IoC Container tự động tiêm dependency theo cơ chế DI. Ưu tiên Constructor Injection (kết hợp `@RequiredArgsConstructor` của Lombok).

---

### 2.2. Nhóm Spring Web MVC (REST Controller)

#### `@RestController`
- **Vị trí**: Cấp Class (`ProjectController`, `GroupController`, `EmployeeController`).
- **Ý nghĩa & Bản chất**: Kết hợp giữa `@Controller` và `@ResponseBody`. Mọi dữ liệu trả về từ method được tự động serialize thành JSON/XML và ghi trực tiếp vào HTTP Response Body.

#### `@RequestMapping`
- **Vị trí**: Cấp Class hoặc Method.
- **Ý nghĩa & Bản chất**: Định tuyến URL pattern cho controller hoặc method.

#### `@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`
- **Vị trí**: Cấp Method.
- **Ý nghĩa & Bản chất**: Shortcut tương ứng cho từng HTTP method, chuẩn hóa hành vi RESTful.

#### `@PathVariable`
- **Vị trí**: Tham số Method.
- **Ý nghĩa & Bản chất**: Trích xuất giá trị từ biến định tuyến trên URI (`/projects/{id}`).

#### `@RequestParam`
- **Vị trí**: Tham số Method.
- **Ý nghĩa & Bản chất**: Bắt các tham số query string từ URL (`/projects?keyword=EFV`).

#### `@RequestBody`
- **Vị trí**: Tham số Method.
- **Ý nghĩa & Bản chất**: Yêu cầu Jackson deserialize nội dung của HTTP Request Body (JSON) thành đối tượng Java DTO.

#### `@ResponseStatus`
- **Vị trí**: Cấp Method hoặc Exception Class.
- **Ý nghĩa & Bản chất**: Định nghĩa trước HTTP Status Code trả về (ví dụ `@ResponseStatus(HttpStatus.CREATED)`).

#### `@RestControllerAdvice` & `@ExceptionHandler`
- **Vị trí**: `@RestControllerAdvice` ở cấp Class, `@ExceptionHandler` ở cấp Method.
- **Ý nghĩa & Bản chất**: Bắt tập trung các ngoại lệ phát sinh từ bất kỳ Controller nào trong toàn bộ ứng dụng để trả về JSON lỗi thống nhất.

---

### 2.3. Nhóm Spring Data JPA & Transaction Management

#### `@Transactional`
- **Vị trí**: Cấp Class hoặc Method ở Service/Repository layer.
- **Ý nghĩa & Bản chất**: Quản lý ranh giới giao dịch ACID:
  - Tự động mở transaction, commit khi thành công, rollback khi xảy ra `RuntimeException`.
  - `readOnly = true`: Tắt cơ chế Dirty Checking của Hibernate, tiết kiệm đáng kể CPU và RAM.
  - Trong Unit/Integration Test: Tự động rollback dữ liệu sau mỗi bài test để giữ DB luôn sạch sẽ.

#### `@EntityGraph`
- **Vị trí**: Phương thức của Spring Data JPA Repository.
- **Ý nghĩa & Bản chất**: Can thiệp vào kế hoạch nạp dữ liệu tại runtime. Tự động sinh `LEFT OUTER JOIN` để nạp các quan hệ được chỉ định trong `attributePaths` trong đúng 1 câu query, triệt tiêu N+1 query.

#### `@Query` & `@Modifying`
- **Vị trí**: Phương thức Repository.
- **Ý nghĩa & Bản chất**:
  - `@Query`: Khai báo trực tiếp JPQL hoặc Native SQL.
  - `@Modifying`: Đi kèm khi thực thi các lệnh thay đổi dữ liệu (`INSERT`, `UPDATE`, `DELETE`), báo cho Spring Data biết để gọi `executeUpdate()`.

---

### 2.4. Nhóm JPA / Hibernate ORM (Entity Mapping)

#### `@MappedSuperclass`
- **Vị trí**: Cấp Class cha trừu tượng ([`AbstractEntity`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/AbstractEntity.java)).
- **Ý nghĩa & Bản chất**: Khai báo class cha chứa các thuộc tính và annotation ánh xạ dùng chung (`id`, `version`). Không có bảng riêng trong DB, các thuộc tính được nhúng thẳng vào các bảng của các thực thể con kế thừa nó.

#### `@Entity`
- **Vị trí**: Cấp Class (`Project`, `Group`, `Employee`).
- **Ý nghĩa & Bản chất**: Đánh dấu class là một thực thể ORM được quản lý bởi JPA EntityManager và liên kết với một bảng trong cơ sở dữ liệu.

#### `@Table`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Định nghĩa tên bảng (`name = "PROJECT"`) và danh sách chỉ mục (`indexes = { @Index(...) }`).

#### `@Id` & `@GeneratedValue` & `@SequenceGenerator`
- **Vị trí**: Cấp Field khóa chính.
- **Ý nghĩa & Bản chất**:
  - `@Id`: Xác định thuộc tính là Primary Key.
  - `@GeneratedValue(strategy = GenerationType.SEQUENCE)`: Sử dụng cơ chế Sequence độc lập của DB, cho phép Hibernate lấy ID trước và kích hoạt cơ chế Write-Behind gom lô (Batch Insert).
  - `@SequenceGenerator`: Cấu hình tên sequence vật lý trong database (`hibernate_sequence`).

#### `@Column`
- **Vị trí**: Cấp Field.
- **Ý nghĩa & Bản chất**:
  - `nullable = false`: Bắt buộc không được null.
  - `unique = true`: Ràng buộc giá trị duy nhất.
  - `updatable = false`: Loại bỏ hoàn toàn cột này khỏi mệnh đề `SET` trong câu lệnh SQL `UPDATE`, bảo vệ khóa chính bất biến.
  - `precision = 19, scale = 0`: Số nguyên có tối đa 19 chữ số (khớp với kiểu `BIGINT` và Java `Long`).

#### `@Version`
- **Vị trí**: Cấp Field (`private Long version;`).
- **Ý nghĩa & Bản chất**: Kích hoạt cơ chế Khóa Lạc Quan (Optimistic Locking). Tự động kiểm tra và tăng phiên bản khi update, ngăn chặn ghi đè dữ liệu đồng thời.

#### `@Enumerated(EnumType.STRING)`
- **Vị trí**: Cấp Field enum ([`ProjectStatus`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/model/entity/ProjectStatus.java)).
- **Ý nghĩa & Bản chất**: Lưu giá trị enum dưới dạng chuỗi văn bản (`'NEW'`, `'INP'`) thay vì số nguyên thứ tự (`ORDINAL`), bảo toàn dữ liệu lịch sử khi enum thay đổi.

#### `@ManyToOne` & `@OneToMany` & `@ManyToMany`
- **Vị trí**: Cấp Field quan hệ.
- **Ý nghĩa & Bản chất**:
  - `@ManyToOne(fetch = FetchType.LAZY)`: Bắt buộc đặt `LAZY` để tránh kích hoạt câu query nạp ngầm ngoài ý muốn.
  - `@OneToMany(mappedBy = "...")`: Chỉ định bên không sở hữu quan hệ khóa ngoại (Inverted Side).
  - `@ManyToMany`: Quan hệ nhiều - nhiều giữa `Project` và `Employee`, kết hợp với `@JoinTable` để cấu hình bảng trung gian `PROJECT_EMPLOYEE`.

#### `@BatchSize`
- **Vị trí**: Cấp Class hoặc Cấp Field Collection.
- **Ý nghĩa & Bản chất**: Hướng dẫn Hibernate gom các ID của thực thể cha thành mệnh đề `WHERE id IN (?, ?, ...)` khi nạp dữ liệu quan hệ Lazy. Là giải pháp số 1 để giải quyết bài toán N+1 Query khi có phân trang `Pageable`.

---

### 2.5. Nhóm Project Lombok

#### `@Getter` & `@Setter`
- **Vị trí**: Cấp Class hoặc Field.
- **Ý nghĩa & Bản chất**: Sinh getter/setter tại thời điểm biên dịch.
- **Đặc biệt: `@Setter(AccessLevel.NONE)`**: Chặn Lombok sinh hàm setter mặc định. Bắt buộc áp dụng trên các trường Collection của quan hệ hai chiều để lập trình viên sử dụng các hàm helper tự viết, bảo vệ tính toàn vẹn 2 chiều trong Persistence Context.

#### `@NoArgsConstructor` & `@AllArgsConstructor`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**:
  - `@NoArgsConstructor`: Bắt buộc đối với các JPA Entity để Hibernate khởi tạo đối tượng qua Reflection.
  - `@AllArgsConstructor`: Sinh constructor đầy đủ tham số, cần thiết cho `@Builder`.

#### `@Builder`
- **Vị trí**: Cấp Class.
- **Ý nghĩa & Bản chất**: Triển khai thiết kế Builder Pattern (Fluent API). Giúp khởi tạo đối tượng có nhiều thuộc tính một cách rõ ràng, không bao giờ bị nhầm lẫn thứ tự các tham số cùng kiểu dữ liệu.

#### `@ToString` & `@EqualsAndHashCode`
- **CẠNH BẪY CẦN TRÁNH VỚI `@Data` TRÊN JPA ENTITY**:
  Tuyệt đối không dùng `@Data` trên JPA Entity! `@Data` bao hàm `@ToString` và `@EqualsAndHashCode` trên toàn bộ các trường, sẽ vô tình kích hoạt tải các quan hệ Lazy hoặc gây lỗi đệ quy vô tận `StackOverflowError` trong quan hệ hai chiều.

---

### 2.6. Nhóm Jackson Serialization

#### `@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})`
- **Vị trí**: Cấp Class Entity hoặc DTO.
- **Ý nghĩa & Bản chất**: Chỉ thị cho Jackson ObjectMapper bỏ qua 2 trường kỹ thuật nội bộ của Hibernate ByteBuddy Proxy khi chuyển đổi đối tượng sang JSON, ngăn chặn lỗi `InvalidDefinitionException: No serializer found for class ... ByteBuddyInterceptor`.

#### `@JsonProperty`
- **Vị trí**: Cấp Field hoặc Getter trong DTO.
- **Ý nghĩa & Bản chất**: Tùy chỉnh tên khóa JSON tương ứng khi serialize/deserialize.

---

### 2.7. Nhóm Bean Validation (JSR-380 & Custom Annotations)

#### `@Valid` & `@Validated`
- **Vị trí**: Tham số Controller method hoặc Cấp Class.
- **Ý nghĩa & Bản chất**: Kích hoạt tiến trình kiểm tra tính hợp lệ của DTO.

#### `@NotNull`, `@NotBlank`, `@NotEmpty`
- **Vị trí**: Cấp Field trong DTO.
- **Phân biệt**:
  - `@NotNull`: Không được null (dùng cho số, ngày tháng, đối tượng).
  - `@NotEmpty`: Không được null và size > 0 (dùng cho Collection, List).
  - `@NotBlank`: Không được null và độ dài sau khi trim() phải > 0 (dành riêng cho chuỗi String).

#### `@Size`, `@Min`, `@Max`, `@Pattern`
- **Vị trí**: Cấp Field.
- **Ý nghĩa**: Ràng buộc độ dài chuỗi, giá trị số tối thiểu/tối đa, hoặc so khớp Regex.

#### Các Custom Validation Annotations Trong Dự Án
1. **[`@Visa`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/Visa.java)**: Ràng buộc mã VISA nhân viên đúng 3 chữ cái in hoa (`[A-Z]{3}`).
2. **[`@Visas`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/Visas.java)**: Ràng buộc danh sách tập hợp các mã VISA nhân viên tham gia dự án.
3. **[`@StartBeforeEndDate`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/java/vn/elca/training/validator/annotation/StartBeforeEndDate.java)**: Ràng buộc cấp Class, kiểm tra ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc.

---

### 2.8. Nhóm Testing (JUnit, Mockito, SpringBootTest)

#### `@RunWith(SpringRunner.class)`
- **Vị trí**: Cấp Class kiểm thử.
- **Ý nghĩa**: Cầu nối giữa JUnit 4 và Spring TestContext Framework.

#### `@SpringBootTest`
- **Vị trí**: Cấp Class kiểm thử tích hợp.
- **Ý nghĩa**: Nạp toàn bộ Spring ApplicationContext thật để kiểm thử luồng tích hợp hoàn chỉnh.

#### `@AutoConfigureMockMvc`
- **Vị trí**: Cấp Class kiểm thử Web.
- **Ý nghĩa**: Khởi tạo `MockMvc` giả lập gửi nhận HTTP Request/Response mà không cần khởi động Tomcat thật.

#### `@MockBean` vs `@Mock`
- **Phân biệt**:
  - `@Mock`: Mockito thuần túy, không can dự vào Spring Context (dùng cho Service Unit Test).
  - `@MockBean`: Thay thế trực tiếp bean thật trong Spring ApplicationContext (dùng cho Web Controller Test).

#### `@InjectMocks`
- **Vị trí**: Cấp Field trong Unit Test.
- **Ý nghĩa**: Tự động tiêm các đối tượng `@Mock` vào instance cần kiểm thử.

#### `@Test` & `@Before`
- **Vị trí**: Cấp Method.
- **Ý nghĩa**: `@Test` đánh dấu test case độc lập; `@Before` chạy thiết lập môi trường trước mỗi bài test.

---

## 3. TỔNG KẾT & QUY TẮC THIẾT KẾ ĐẠT CHUẨN DOANH NGHIỆP

1. **Clean Code & Không Boilerplate**: Tận dụng tối đa Lombok để giữ codebase tinh gọn, nhưng luôn dùng `@Setter(AccessLevel.NONE)` cho quan hệ hai chiều và tránh xa `@Data` trên JPA Entity.
2. **Hiệu năng là ưu tiên hàng đầu**:
   - Sử dụng `Slice<T>` cho dữ liệu tìm kiếm vô hạn / dropdown autocomplete để loại bỏ chi phí `SELECT COUNT(*)`.
   - Kết hợp `@BatchSize` cho quan hệ Collection khi có phân trang để tránh bẫy In-Memory Pagination và OOM của `@EntityGraph`.
   - Sử dụng `GenerationType.SEQUENCE` thay cho `IDENTITY` để kích hoạt cơ chế Write-Behind và Batch Insert.
   - Luôn đặt `FetchType.LAZY` cho các quan hệ `@ManyToOne`.
   - Thấu hiểu cơ chế B-Tree Index để không viết các câu truy vấn gây vô hiệu hóa chỉ mục.
3. **An toàn đồng thời**: Bắt buộc có trường `@Version` (kiểu `Long`) trên các bảng nghiệp vụ có khả năng bị nhiều người cùng chỉnh sửa để đảm bảo toàn vẹn dữ liệu.
4. **Kiểm soát chặt chẽ dữ liệu đầu vào**: Ràng buộc qua Bean Validation ngay tại cửa ngõ DTO, ngăn chặn hoàn toàn dữ liệu rác đi vào tầng xử lý nghiệp vụ hay cơ sở dữ liệu.
