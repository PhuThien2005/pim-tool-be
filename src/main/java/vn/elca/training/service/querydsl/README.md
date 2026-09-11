# TÀI LIỆU HỌC TẬP VÀ ỨNG DỤNG QUERYDSL TRONG DỰ ÁN

Package này được tạo ra nhằm giúp bạn nắm vững bản chất của **QueryDSL**, lý do tại sao các dự án thực tế (đặc biệt là các dự án quản lý thông tin như `pilot-project-back`) bắt buộc phải dùng **Dynamic Query (Query động)**, và cách sử dụng QueryDSL từ mức cơ bản nhất đến nâng cao.

---

## 1. QueryDSL là gì?

**QueryDSL** (Domain Specific Language for Queries) là một framework mã nguồn mở dành cho Java, cho phép bạn viết các câu lệnh truy vấn cơ sở dữ liệu theo phong cách **hướng đối tượng (Object-Oriented)**, **thông suốt (Fluent API)** và đặc biệt là **an toàn kiểu dữ liệu (Type-Safe)**.

### Vấn đề của các cách viết thông thường:
1. **Spring Data JPA Derived Query (`findBy...`)**:
   - Chỉ phù hợp với truy vấn cố định, ít tham số (ví dụ `findByName(String name)`).
   - Khi có 4-5 tiêu chí lọc linh hoạt (keyword, ngày bắt đầu, ngày kết thúc, khách hàng,...), bạn sẽ phải viết hàng chục method cho từng tổ hợp!
2. **JPQL / HQL bằng chuỗi (String Concatenation)**:
   - Ví dụ: `"SELECT p FROM Project p WHERE 1=1 " + (name != null ? "AND p.name = :name" : "")`
   - **Rất nguy hiểm**: Dễ lỗi cú pháp (thiếu khoảng trắng, sai dấu ngoặc), không bắt được lỗi chính tả tên thuộc tính lúc compile (chỉ khi chạy đến dòng đó mới ném lỗi Runtime), và tiềm ẩn nguy cơ **SQL Injection**.

### QueryDSL giải quyết như thế nào?
- Tạo ra các lớp siêu dữ liệu gọi là **Q-Classes** (ví dụ Entity `Project` -> lớp sinh tự động `QProject`).
- Cho phép viết code: `QProject.project.name.containsIgnoreCase("ELCA")`.
- **Compile-time checking**: Nếu entity đổi tên trường `customer` thành `client`, code QueryDSL sẽ báo lỗi đỏ ngay trong IDE, bạn không thể build nếu chưa sửa. Không bao giờ sợ lỗi tên cột khi chạy production!

---

## 2. Tại sao dự án này (`pilot-project-back`) lại cần xây query động?

Hãy nhìn vào đoạn code hiện tại trong file `ProjectServiceImpl.java`:

```java
@Override
public List<ProjectDto> searchByKeyword(String keyword) {
    return projectRepository.findAll().stream()
            .filter(p -> p.getName().contains(keyword))
            .map(mapper::projectToProjectDto)
            .collect(Collectors.toList());
}
```

### Đây là một Anti-Pattern cực kỳ nghiêm trọng trong các hệ thống doanh nghiệp:
1. **Tràn bộ nhớ RAM (OutOfMemoryError)**:
   - `projectRepository.findAll()` sẽ kéo **toàn bộ dữ liệu** của bảng `Project` từ Database nạp vào bộ nhớ RAM của Server.
   - Nếu dự án có 100 dòng thì chạy bình thường. Nhưng khi dự án chạy thực tế có **100.000 dòng hoặc 1.000.000 dòng**, toàn bộ lượng dữ liệu khổng lồ này sẽ làm đầy heap size JVM -> Server bị crash ngay lập tức!
2. **Lãng phí tài nguyên CPU & Network**:
   - Truyền tải hàng triệu bản ghi qua mạng giữa Database và Application chỉ để lấy về vài bản ghi khớp điều kiện.
   - Database được sinh ra và tối ưu hoá (Index, B-Tree, Caching) để thực hiện việc lọc dữ liệu. Lọc dữ liệu **phải luôn luôn được thực hiện ở tầng Database** thông qua mệnh đề `WHERE`!

### Tại sao lại cần "Query Động" (Dynamic Query)?
Trong bài toán quản lý dự án (Project Management) của bài Onboarding này, người dùng trên giao diện Web thường có một **Form tìm kiếm nâng cao** với nhiều tiêu chí:
- `keyword`: Tìm theo tên dự án HOẶC tên khách hàng.
- `customer`: Chọn khách hàng trong dropdown.
- `finishingDateFrom` & `finishingDateTo`: Khoảng ngày kết thúc.
- `status`: Trạng thái dự án.
- `taskName`: Tên task thuộc dự án.

> **Đặc điểm**: Mỗi ô tìm kiếm người dùng **có thể nhập hoặc để trống (null)**.
> - Nếu người dùng chỉ nhập `keyword` -> Query chỉ thêm `WHERE name LIKE %...% OR customer LIKE %...%`.
> - Nếu người dùng nhập thêm `finishingDateFrom` -> Query tự động nối thêm `AND finishing_date >= ...`.
> - Nếu người dùng không nhập gì -> Query `SELECT *` bình thường.

Nếu có 5 tiêu chí lọc tuỳ chọn, bạn sẽ có tới $2^5 = 32$ tổ hợp câu lệnh SQL khác nhau. Bạn không thể viết 32 method JPA repository được!
**Query động (Dynamic Query) bằng QueryDSL** sinh ra chính là để giải quyết bài toán này: xây dựng mệnh đề `WHERE` một cách linh hoạt theo đúng những gì người dùng truyền vào.

---

## 3. Cách sử dụng QueryDSL đơn giản nhất (3 Bước)

### Bước 1: Khai báo trong `pom.xml` (Dự án đã cấu hình sẵn)
1. Thêm dependency `querydsl-jpa` và `querydsl-apt`:
   ```xml
   <dependency>
       <groupId>com.querydsl</groupId>
       <artifactId>querydsl-jpa</artifactId>
       <version>4.2.2</version>
   </dependency>
   <dependency>
       <groupId>com.querydsl</groupId>
       <artifactId>querydsl-apt</artifactId>
       <version>4.2.2</version>
   </dependency>
   ```
2. Cấu hình Maven APT Plugin để tự động sinh các lớp `Q...` khi compile:
   ```xml
   <plugin>
       <groupId>com.mysema.maven</groupId>
       <artifactId>apt-maven-plugin</artifactId>
       <version>1.1.3</version>
       <executions>
           <execution>
               <goals><goal>process</goal></goals>
               <configuration>
                   <outputDirectory>target/generated-sources</outputDirectory>
                   <processor>com.querydsl.apt.jpa.JPAAnnotationProcessor</processor>
               </configuration>
           </execution>
       </executions>
   </plugin>
   ```
   > Khi chạy `mvn compile`, các lớp `QProject`, `QTask`, `QUser` sẽ tự động sinh ra trong `target/generated-sources`.

---

### Bước 2: Cho Repository kế thừa `QuerydslPredicateExecutor<T>`
Dự án của bạn trong `ProjectRepository.java` đã có sẵn:
```java
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long>, QuerydslPredicateExecutor<Project> {
}
```
Interface này cung cấp ngay các method nhận vào `Predicate` của QueryDSL:
- `findAll(Predicate predicate)`
- `findOne(Predicate predicate)`
- `count(Predicate predicate)`
- `findAll(Predicate predicate, Pageable pageable)` (hỗ trợ phân trang)

---

### Bước 3: Sử dụng `BooleanBuilder` để xây dựng Query động
Xem minh họa trong `ProjectQuerydslDemoService.java`:

```java
public List<Project> searchProjectsDynamic(ProjectSearchCriteria criteria) {
    QProject qProject = QProject.project;
    BooleanBuilder builder = new BooleanBuilder();

    // 1. Chỉ thêm điều kiện khi người dùng thực sự nhập keyword
    if (StringUtils.isNotBlank(criteria.getKeyword())) {
        builder.and(qProject.name.containsIgnoreCase(criteria.getKeyword())
                .or(qProject.customer.containsIgnoreCase(criteria.getKeyword())));
    }

    // 2. Chỉ thêm điều kiện khi có ngày từ ngày
    if (criteria.getFinishingDateFrom() != null) {
        builder.and(qProject.finishingDate.goe(criteria.getFinishingDateFrom())); // goe: >=
    }

    // 3. Chỉ thêm điều kiện khi có ngày đến ngày
    if (criteria.getFinishingDateTo() != null) {
        builder.and(qProject.finishingDate.loe(criteria.getFinishingDateTo())); // loe: <=
    }

    // Thực thi query trực tiếp xuống Database
    Iterable<Project> results = projectRepository.findAll(builder);
    
    List<Project> list = new ArrayList<>();
    results.forEach(list::add);
    return list;
}
```

---

## 4. Các toán tử phổ biến trong QueryDSL

| Toán tử QueryDSL | Ý nghĩa SQL tương đương | Ví dụ |
|---|---|---|
| `.eq(val)` | `= val` | `qProject.name.eq("KSTA")` |
| `.ne(val)` | `<> val` hoặc `!= val` | `qProject.name.ne("TEST")` |
| `.contains(val)` | `LIKE '%val%'` (phân biệt hoa thường) | `qProject.name.contains("abc")` |
| `.containsIgnoreCase(val)` | `LIKE '%val%'` (không phân biệt hoa thường) | `qProject.name.containsIgnoreCase("ksta")` |
| `.startsWith(val)` | `LIKE 'val%'` | `qProject.name.startsWith("PRJ_")` |
| `.endsWith(val)` | `LIKE '%val'` | `qProject.name.endsWith("_DEV")` |
| `.isNull()` / `.isNotNull()` | `IS NULL` / `IS NOT NULL` | `qProject.customer.isNotNull()` |
| `.in(list)` | `IN (val1, val2, ...)` | `qProject.id.in(1L, 2L, 3L)` |
| `.between(from, to)` | `BETWEEN from AND to` | `qProject.finishingDate.between(from, to)` |
| `.goe(val)` | `>= val` (Greater Than or Equal) | `qProject.finishingDate.goe(date)` |
| `.gt(val)` | `> val` (Greater Than) | `qProject.id.gt(10L)` |
| `.loe(val)` | `<= val` (Less Than or Equal) | `qProject.finishingDate.loe(date)` |
| `.lt(val)` | `< val` (Less Than) | `qProject.id.lt(100L)` |

---

## 5. Nâng cao: JPAQuery / DTO Projection & Joins

Khi cần JOIN nhiều bảng hoặc chỉ muốn lấy một vài cột vào DTO để tối ưu RAM (tránh nạp cả Entity vào Hibernate L1 Cache), ta sử dụng `JPAQuery`:

```java
// DTO Projection: SELECT thẳng ra ProjectDto
List<ProjectDto> dtos = new JPAQuery<ProjectDto>(em)
        .select(Projections.constructor(
                ProjectDto.class,
                qProject.id,
                qProject.name,
                qProject.customer,
                qProject.finishingDate
        ))
        .from(qProject)
        .where(qProject.customer.eq("ELCA"))
        .fetch();
```

---

## 6. Cấu trúc package demo

- `ProjectSearchCriteria.java`: DTO tiêu chí tìm kiếm.
- `ProjectQuerydslDemoService.java`: Service chứa đầy đủ 6 ví dụ thực tế.
- `ProjectQuerydslDemoTest.java` (trong thư mục test): Bộ unit test tự động kiểm tra toàn bộ các trường hợp tìm kiếm và in kết quả.
