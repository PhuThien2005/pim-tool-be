# KẾT QUẢ KỲ VỌNG PHẢN HỒI BACKEND (BACKEND EXPECTED RESPONSES)
## ĐỐI SOÁT VỚI BỘ MANUAL TEST CASES CỦA PIM TOOL

> **Mục đích tài liệu:** Quy định chi tiết và chuẩn hóa phản ứng kỳ vọng của Backend (Spring Boot) tương ứng với từng Test Case trong tài liệu `MANUAL_TEST_CASES.md`.  
> Bao gồm: **HTTP Method & URL, Request Payload / Query Params, Logic xử lý nội bộ, Câu lệnh SQL sinh ra (Hibernate/JPA/QueryDSL), HTTP Status Code và Response Body JSON.**

---

## 📋 MỤC LỤC
1. [Bảng Ma Trận Tổng Quan (Summary Matrix)](#1-bảng-ma-trận-tổng-quan)
2. [Nhóm 1: Tạo mới Dự án (US01 - New Project)](#nhóm-1-tạo-mới-dự-án-us01---new-project)
3. [Nhóm 2: Chỉnh sửa Dự án (US01 - Edit Project)](#nhóm-2-chỉnh-sửa-dự-án-us01---edit-project)
4. [Nhóm 3: Hiển thị Bảng, Phân trang & Sắp xếp (US02 - List, Pagination, Sort)](#nhóm-3-hiển-thị-bảng-phân-trang--sắp-xếp-us02)
5. [Nhóm 4: Tìm kiếm, Lọc nâng cao & Đồng bộ URL (US02 - Search & Filter)](#nhóm-4-tìm-kiếm-lọc-nâng-cao--đồng-bộ-url-us02)
6. [Nhóm 5: Quy tắc Xóa Dự án Đơn lẻ & Hàng loạt (US02 - Delete Rules)](#nhóm-5-quy-tắc-xóa-dự-án-đơn-lẻ--hàng-loạt-us02)
7. [Nhóm 6: Đa ngôn ngữ (i18n - English / French)](#nhóm-6-đa-ngôn-ngữ-i18n)

---

## 1. BẢNG MA TRẬN TỔNG QUAN

| Mã Test Case | Endpoint Backend | HTTP Method | HTTP Status | SQL Query phát sinh (Chính) | Phản ứng nổi bật của Backend |
|---|---|:---:|:---:|---|---|
| **TC-NEW-01** | `/groups` | GET | `200 OK` | `SELECT ... FROM "GROUP" LEFT JOIN EMPLOYEE` | Trả về danh sách Group kèm Leader Visa cho dropdown |
| **TC-NEW-02** | *Không gọi API* | - | - | *Không phát sinh* | Frontend hủy bỏ, điều hướng về `/` |
| **TC-NEW-03** | `/projects` | POST | `400 Bad Request` | *Không phát sinh* (Chặn tại Bean Validation) | Trả về map các trường `@NotNull`, `@NotBlank` bị thiếu |
| **TC-NEW-04** | `/projects` | POST | `400 Bad Request` | *Không phát sinh* (Chặn tại Bean Validation) | Trả về lỗi vi phạm `@Size(max = 50)` |
| **TC-NEW-05** | `/projects` | POST | `400 Bad Request` | *Không phát sinh* (Chặn tại `@StartBeforeEndDate`) | Trả về `"endDate": "End date must be later than start date."` |
| **TC-NEW-06** | `/employees` | GET | `200 OK` | `SELECT ... FROM EMPLOYEE WHERE lower(VISA) LIKE ? ...` | Trả về Slice nhân viên khớp từ khóa gợi ý |
| **TC-NEW-07** | `/projects` | POST | `400 Bad Request` | `SELECT COUNT(ID) FROM PROJECT WHERE PROJECT_NUMBER = ?` | Ném `PROJECT_NUMBER_ALREADY_EXISTS` |
| **TC-NEW-08** | `/projects` | POST | `400 Bad Request` | `SELECT ... FROM EMPLOYEE WHERE VISA IN (?)` | Ném `VISA_NOT_FOUND` kèm danh sách visa lỗi |
| **TC-NEW-09** | `/projects` | POST | `201 Created` | `INSERT INTO PROJECT` & `INSERT INTO PROJECT_EMPLOYEE` | Lưu thành công dự án mới, gán `version = 1` |
| **TC-EDIT-01** | `/projects/{id}` | GET | `200 OK` | `SELECT ... FROM PROJECT LEFT JOIN ... WHERE ID = ?` | Trả về chi tiết dự án (kèm danh sách members & version) |
| **TC-EDIT-02** | *Không gọi API* | - | - | *Không phát sinh* | Ô Project Number bị disabled/readonly ở frontend |
| **TC-EDIT-03** | `/projects/{id}` | GET | `200 OK` | Dùng kết quả từ TC-EDIT-01 | Frontend bind dữ liệu nhận được vào form |
| **TC-EDIT-04** | `/projects/{id}` | PUT | `200 OK` | `UPDATE PROJECT SET ... VERSION = 2 WHERE ID = ? AND VERSION = 1` | Cập nhật thành công, tự động tăng `version` lên 2 |
| **TC-EDIT-05** | `/projects/{id}` | PUT | `409 Conflict` | `UPDATE ... WHERE ID = ? AND VERSION = 1` (0 rows) | Bắt lỗi `OptimisticLockException`, trả về thông điệp xung đột |
| **TC-EDIT-06** | `/projects/{id}` | GET | `404 Not Found` | `SELECT ... WHERE ID = 99999999` (Không thấy) | Ném `PROJECT_NOT_FOUND` với ID tương ứng |
| **TC-LIST-01** | `/projects` | GET | `200 OK` | `SELECT ... FROM PROJECT ORDER BY PROJECT_NUMBER ASC LIMIT 5` | Trả về trang 0 gồm 5 dự án mặc định |
| **TC-LIST-02** | `/projects` | GET | `200 OK` | Dùng kết quả từ TC-LIST-01 | Cung cấp trường `status` để UI ẩn/hiện nút xóa |
| **TC-LIST-03** | `/projects` | GET | `200 OK` | `... ORDER BY project0_.<COL> <ASC/DESC> LIMIT 5` | Sắp xếp động theo header cột được click |
| **TC-LIST-04** | `/projects` | GET | `200 OK` | `... LIMIT 5 OFFSET 5` | Phân trang Server-side chính xác theo `page` và `size` |
| **TC-SEARCH-01**| `/projects` | GET | `200 OK` | `... WHERE lower(NAME) LIKE ? OR lower(CUSTOMER) LIKE ?` | Lọc theo `keyword` (kết hợp name, customer, number) |
| **TC-SEARCH-02**| `/projects` | GET | `200 OK` | `... WHERE STATUS = ?` | Lọc chính xác theo `status` |
| **TC-SEARCH-03**| `/projects` | GET | `200 OK` | `... WHERE (NAME LIKE ? ...) AND STATUS = ?` | Kết hợp đồng thời điều kiện keyword và status |
| **TC-SEARCH-04**| `/projects` | GET | `200 OK` | `SELECT ... FROM PROJECT ORDER BY PROJECT_NUMBER ASC` | Bỏ hết mệnh đề WHERE, nạp lại toàn bộ danh sách |
| **TC-ADV-01** | *Không gọi API* | - | - | *Không phát sinh* | Đóng/mở panel lọc tại UI |
| **TC-ADV-02** | `/projects` | GET | `200 OK` | `... JOIN "GROUP" ... JOIN EMPLOYEE ... VISA = ?` | Lọc dự án theo trưởng nhóm (`leaderVisa`) |
| **TC-ADV-03** | `/projects` | GET | `200 OK` | `... WHERE EXISTS (SELECT 1 FROM PROJECT_EMPLOYEE ...)` | Lọc dự án có thành viên mang `memberVisas` |
| **TC-ADV-04** | `/projects` | GET | `200 OK` | Query sạch, không có điều kiện rác | Bỏ qua các param rỗng/null nhờ `BooleanBuilder` |
| **TC-NAV-01** | `/projects` | GET | `200 OK` | Query tương ứng URL params phục hồi | Đồng bộ mượt mà khi người dùng bấm Back/Forward |
| **TC-DEL-01** | *Không gọi API* | - | - | *Không phát sinh* | Bấm Cancel popup xóa đơn lẻ |
| **TC-DEL-02** | `/projects/{id}` | DELETE | `204 No Content` | `DELETE FROM PROJECT_EMPLOYEE` & `DELETE FROM PROJECT` | Xóa thành công 1 dự án có status `NEW` |
| **TC-DEL-03** | *Không gọi API* | - | - | *Không phát sinh* | Tích chọn checkbox hiển thị thanh selection |
| **TC-DEL-04** | `/projects` | DELETE | `204 No Content` | `DELETE FROM PROJECT_EMPLOYEE WHERE PROJECT_ID IN (...)` | Xóa hàng loạt danh sách dự án `NEW` |
| **TC-DEL-05** | `/projects` | DELETE | `400 Bad Request` | `SELECT COUNT(ID) ... WHERE STATUS <> 'NEW'` | Chặn xóa nếu có dự án khác trạng thái `NEW` |
| **TC-I18N-01** | Toàn bộ API | Đa dạng | Theo kết quả | Phản hồi thông điệp bằng tiếng Pháp (`fr`) | `Accept-Language: fr` $\rightarrow$ Đọc `messages_fr.properties` |
| **TC-I18N-02** | Toàn bộ API | Đa dạng | Theo kết quả | Phản hồi thông điệp bằng tiếng Anh (`en`) | `Accept-Language: en` $\rightarrow$ Đọc `messages.properties` |

---

## NHÓM 1: TẠO MỚI DỰ ÁN (US01 - NEW PROJECT)

### TC-NEW-01: Giao diện mặc định màn hình New Project
- **API Request:**  
  `GET http://localhost:8080/groups?page=0&size=100`
- **Mục đích:** Lấy danh sách các nhóm và mã VISA trưởng nhóm phục vụ dropdown `Group*`.
- **Câu lệnh SQL sinh ra (Hibernate/JPA):**
  ```sql
  select
      group0_.ID as id1_0_0_,
      employee1_.ID as id1_1_1_,
      group0_.VERSION as version2_0_0_,
      group0_.GROUP_LEADER_ID as group_le3_0_0_,
      employee1_.VERSION as version2_1_1_,
      employee1_.BIRTH_DATE as birth_da3_1_1_,
      employee1_.FIRST_NAME as first_na4_1_1_,
      employee1_.LAST_NAME as last_nam5_1_1_,
      employee1_.VISA as visa6_1_1_
  from
      "GROUP" group0_
  left outer join
      EMPLOYEE employee1_
          on group0_.GROUP_LEADER_ID=employee1_.ID
  order by
      group0_.ID asc limit 100;
  ```
- **HTTP Status:** `200 OK`
- **Response Body:**
  ```json
  {
    "content": [
      { "id": 1, "leaderVisa": "DTH", "version": 1 },
      { "id": 2, "leaderVisa": "BHU", "version": 1 },
      { "id": 3, "leaderVisa": "JHV", "version": 1 }
    ],
    "pageable": { "sort": { "sorted": true, "unsorted": false, "empty": false }, "pageNumber": 0, "pageSize": 100, "offset": 0 },
    "last": true,
    "totalPages": 1,
    "totalElements": 3,
    "size": 100,
    "number": 0,
    "first": true,
    "numberOfElements": 3,
    "empty": false
  }
  ```

---

### TC-NEW-02: Nút Cancel hủy bỏ tạo mới
- **Phản ứng Backend:** Không có bất kỳ request API tạo mới (`POST /projects`) nào được gửi xuống backend. Database không bị thay đổi.
- Khi frontend điều hướng về trang chủ `/`, backend tiếp nhận request nạp bảng mặc định (xem chi tiết ở `TC-LIST-01`).

---

### TC-NEW-03: Validate để trống tất cả trường bắt buộc
- **Tình huống kích hoạt Backend (Nếu request được gửi):**  
  `POST http://localhost:8080/projects`  
  Payload: `{}`
- **Xử lý nội bộ:** `LocalValidatorFactoryBean` chặn request trước khi vào tầng Service do vi phạm `@NotNull` (`projectNumber`, `groupId`, `startDate`) và `@NotBlank` (`name`, `customer`).
- **HTTP Status:** `400 Bad Request`
- **Response Body (xử lý bởi `GlobalExceptionHandler`):**
  ```json
  {
    "status": 400,
    "errorCode": "VALIDATION_ERROR",
    "message": "Validation failed",
    "errors": {
      "projectNumber": "must not be null",
      "name": "must not be blank",
      "customer": "must not be blank",
      "groupId": "must not be null",
      "startDate": "must not be null"
    },
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-NEW-04: Validate độ dài tối đa (Max Length 50 ký tự)
- **Tình huống kích hoạt Backend:**  
  `POST http://localhost:8080/projects` với trường `name` hoặc `customer` dài > 50 ký tự.
- **Xử lý nội bộ:** Ràng buộc `@Size(max = 50)` trên `CreateProjectRequest` bị vi phạm.
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
  ```json
  {
    "status": 400,
    "errorCode": "VALIDATION_ERROR",
    "message": "Validation failed",
    "errors": {
      "name": "size must be between 0 and 50"
    },
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-NEW-05: Validate logic ngày tháng (End Date <= Start Date)
- **API Request:**  
  `POST http://localhost:8080/projects`
- **Request Payload:**
  ```json
  {
    "projectNumber": 9101,
    "name": "Project Date Test",
    "customer": "Customer Test",
    "groupId": 1,
    "status": "NEW",
    "startDate": "2026-10-15",
    "endDate": "2026-10-10",
    "visas": []
  }
  ```
- **Xử lý nội bộ:** Validator `@StartBeforeEndDate` (với cấu hình mặc định `allowEqual = false`) kiểm tra thấy `endDate <= startDate`, trả về vi phạm trên thuộc tính `endDate`.
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
  ```json
  {
    "status": 400,
    "errorCode": "VALIDATION_ERROR",
    "message": "Validation failed",
    "errors": {
      "endDate": "End date must be later than start date."
    },
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-NEW-06: Tính năng gợi ý thành viên (MemberSuggest Autocomplete)
- **API Request:**  
  `GET http://localhost:8080/employees?keyword=d&page=0&size=10`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select
      employee0_.ID as id1_1_,
      employee0_.VERSION as version2_1_,
      employee0_.BIRTH_DATE as birth_da3_1_,
      employee0_.FIRST_NAME as first_na4_1_,
      employee0_.LAST_NAME as last_nam5_1_,
      employee0_.VISA as visa6_1_
  from
      EMPLOYEE employee0_
  where
      lower(employee0_.VISA) like '%d%'
      or lower(employee0_.FIRST_NAME) like '%d%'
      or lower(employee0_.LAST_NAME) like '%d%'
  order by
      employee0_.VISA asc limit 10;
  ```
- **HTTP Status:** `200 OK`
- **Response Body:**
  ```json
  {
    "content": [
      {
        "id": 1,
        "visa": "DTH",
        "firstName": "Thien",
        "lastName": "Do",
        "birthDate": "1990-01-01",
        "version": 1
      }
    ],
    "pageable": { "sort": { "sorted": true }, "pageNumber": 0, "pageSize": 10 },
    "last": true,
    "totalPages": 1,
    "totalElements": 1,
    "size": 10,
    "number": 0,
    "first": true,
    "numberOfElements": 1,
    "empty": false
  }
  ```

---

### TC-NEW-07: Xử lý lỗi Backend - Trùng số dự án (Duplicate Number)
- **API Request:**  
  `POST http://localhost:8080/projects`
- **Request Payload:**
  ```json
  {
    "projectNumber": 3116,
    "name": "Duplicate Test Project",
    "customer": "ELCA",
    "groupId": 1,
    "status": "NEW",
    "startDate": "2026-01-01",
    "visas": []
  }
  ```
- **Câu lệnh SQL kiểm tra sự tồn tại (Fast-check):**
  ```sql
  select
      count(project0_.ID) as col_0_0_
  from
      PROJECT project0_
  where
      project0_.PROJECT_NUMBER=3116 limit 1;
  ```
- **Xử lý nội bộ:** Kết quả `count > 0` $\rightarrow$ ném `ProjectNumberAlreadyExistsException(3116)`. `GlobalExceptionHandler` bắt và đọc template `error.project.number.exist`.
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
  ```json
  {
    "status": 400,
    "errorCode": "PROJECT_NUMBER_ALREADY_EXISTS",
    "message": "The project number already existed. Please select a different project number",
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-NEW-08: Xử lý lỗi Backend - VISA không tồn tại (Invalid VISA)
- **API Request:**  
  `POST http://localhost:8080/projects`
- **Request Payload:**
  ```json
  {
    "projectNumber": 9888,
    "name": "Invalid Visa Test",
    "customer": "ELCA",
    "groupId": 1,
    "status": "NEW",
    "startDate": "2026-01-01",
    "visas": ["DTH", "XYZ_FAKE", "ABC_FAKE"]
  }
  ```
- **Câu lệnh SQL kiểm tra các Visa trong cơ sở dữ liệu:**
  ```sql
  select
      employee0_.ID as id1_1_,
      employee0_.VERSION as version2_1_,
      employee0_.BIRTH_DATE as birth_da3_1_,
      employee0_.FIRST_NAME as first_na4_1_,
      employee0_.LAST_NAME as last_nam5_1_,
      employee0_.VISA as visa6_1_
  from
      EMPLOYEE employee0_
  where
      employee0_.VISA in ('DTH', 'XYZ_FAKE', 'ABC_FAKE');
  ```
- **Xử lý nội bộ:** Chỉ tìm thấy employee `DTH`. Tập hợp visa còn thiếu là `["XYZ_FAKE", "ABC_FAKE"]` $\rightarrow$ ném `VisaNotFoundException`. `GlobalExceptionHandler` ghép các visa thiếu vào template `error.visa.not.exist`.
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
  ```json
  {
    "status": 400,
    "errorCode": "VISA_NOT_FOUND",
    "message": "The following visas do not exist: XYZ_FAKE, ABC_FAKE",
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-NEW-09: Tạo mới dự án thành công hoàn chỉnh
- **API Request:**  
  `POST http://localhost:8080/projects`
- **Request Payload:**
  ```json
  {
    "projectNumber": 8899,
    "name": "PIM Tool Automation System",
    "customer": "Securitas AG",
    "groupId": 1,
    "status": "NEW",
    "startDate": "2026-05-01",
    "endDate": "2026-12-31",
    "visas": ["DTH", "BHU"]
  }
  ```
- **Chuỗi câu lệnh SQL phát sinh:**
  1. Kiểm tra trùng số dự án:
     ```sql
     select count(project0_.ID) as col_0_0_ from PROJECT project0_ where project0_.PROJECT_NUMBER=8899 limit 1;
     ```
  2. Nạp thực thể Group:
     ```sql
     select group0_.ID as id1_0_0_, ... from "GROUP" group0_ left outer join EMPLOYEE employee1_ on group0_.GROUP_LEADER_ID=employee1_.ID where group0_.ID=1;
     ```
  3. Nạp danh sách Nhân viên theo Visa:
     ```sql
     select employee0_.ID as id1_1_, ... from EMPLOYEE employee0_ where employee0_.VISA in ('DTH', 'BHU');
     ```
  4. Lấy giá trị khóa chính mới từ Sequence:
     ```sql
     call next value for hibernate_sequence;
     ```
  5. Thêm bản ghi mới vào bảng `PROJECT`:
     ```sql
     insert into PROJECT (CUSTOMER, END_DATE, GROUP_ID, NAME, PROJECT_NUMBER, START_DATE, STATUS, VERSION, ID)
     values ('Securitas AG', '2026-12-31', 1, 'PIM Tool Automation System', 8899, '2026-05-01', 'NEW', 1, 100);
     ```
  6. Thêm quan hệ thành viên vào bảng liên kết `PROJECT_EMPLOYEE`:
     ```sql
     insert into PROJECT_EMPLOYEE (PROJECT_ID, EMPLOYEE_ID) values (100, 1);
     insert into PROJECT_EMPLOYEE (PROJECT_ID, EMPLOYEE_ID) values (100, 2);
     ```
- **HTTP Status:** `201 Created`
- **Response Body:**
  ```json
  {
    "id": 100,
    "version": 1,
    "projectNumber": 8899,
    "name": "PIM Tool Automation System",
    "customer": "Securitas AG",
    "group": {
      "id": 1,
      "leaderVisa": "DTH",
      "version": 1
    },
    "status": "NEW",
    "startDate": "2026-05-01",
    "endDate": "2026-12-31",
    "members": [
      { "id": 1, "visa": "DTH", "firstName": "Thien", "lastName": "Do", "birthDate": "1990-01-01", "version": 1 },
      { "id": 2, "visa": "BHU", "firstName": "Huu", "lastName": "Bui", "birthDate": "1992-05-15", "version": 1 }
    ]
  }
  ```

---

## NHÓM 2: CHỈNH SỬA DỰ ÁN (US01 - EDIT PROJECT)

### TC-EDIT-01 & TC-EDIT-03: Truy cập trang Edit & Form điền sẵn dữ liệu cũ
- **API Request:**  
  `GET http://localhost:8080/projects/1`
- **Câu lệnh SQL sinh ra (Sử dụng EntityGraph nạp 1 lần không bị N+1 query):**
  ```sql
  select
      project0_.ID as id1_2_0_,
      group1_.ID as id1_0_1_,
      employee2_.ID as id1_1_2_,
      employees3_.PROJECT_ID as project_1_3_0__,
      employee4_.ID as employee2_3_0__,
      employee4_.ID as id1_1_3_,
      project0_.VERSION as version2_2_0_,
      project0_.CUSTOMER as customer3_2_0_,
      project0_.END_DATE as end_date4_2_0_,
      project0_.GROUP_ID as group_id9_2_0_,
      project0_.NAME as name5_2_0_,
      project0_.PROJECT_NUMBER as project_6_2_0_,
      project0_.START_DATE as start_da7_2_0_,
      project0_.STATUS as status8_2_0_,
      group1_.VERSION as version2_0_1_,
      group1_.GROUP_LEADER_ID as group_le3_0_1_,
      employee2_.VERSION as version2_1_2_,
      employee2_.VISA as visa6_1_2_,
      employee4_.VERSION as version2_1_3_,
      employee4_.FIRST_NAME as first_na4_1_3_,
      employee4_.LAST_NAME as last_nam5_1_3_,
      employee4_.VISA as visa6_1_3_
  from
      PROJECT project0_
  left outer join
      "GROUP" group1_
          on project0_.GROUP_ID=group1_.ID
  left outer join
      EMPLOYEE employee2_
          on group1_.GROUP_LEADER_ID=employee2_.ID
  left outer join
      PROJECT_EMPLOYEE employees3_
          on project0_.ID=employees3_.PROJECT_ID
  left outer join
      EMPLOYEE employee4_
          on employees3_.EMPLOYEE_ID=employee4_.ID
  where
      project0_.ID=1;
  ```
- **HTTP Status:** `200 OK`
- **Response Body:**
  ```json
  {
    "id": 1,
    "version": 1,
    "projectNumber": 3116,
    "name": "Facturation / Encaissements",
    "customer": "Les Retaites Populaires",
    "group": { "id": 1, "leaderVisa": "DTH", "version": 1 },
    "status": "NEW",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "members": [
      { "id": 1, "visa": "DTH", "firstName": "Thien", "lastName": "Do", "birthDate": "1990-01-01", "version": 1 },
      { "id": 2, "visa": "BHU", "firstName": "Huu", "lastName": "Bui", "birthDate": "1992-05-15", "version": 1 }
    ]
  }
  ```

---

### TC-EDIT-02: Quy tắc Khóa ô Project Number
- **Phản ứng Backend:** Ô `projectNumber` ở `UpdateProjectRequest` là optional (không validate bắt buộc) hoặc nếu frontend gửi lên thì Backend sẽ không cập nhật giá trị trường `projectNumber` này vào database (bảo đảm tính toàn vẹn số dự án cố định).

---

### TC-EDIT-04: Cập nhật thông tin dự án thành công
- **API Request:**  
  `PUT http://localhost:8080/projects/1`
- **Request Payload:**
  ```json
  {
    "version": 1,
    "projectNumber": 3116,
    "name": "Facturation / Encaissements - Updated 2026",
    "customer": "Les Retaites Populaires",
    "groupId": 1,
    "status": "INP",
    "startDate": "2025-01-01",
    "endDate": "2025-12-31",
    "visas": ["DTH", "BHU", "JHV"]
  }
  ```
- **Câu lệnh SQL sinh ra:**
  1. Nạp entity gốc và nạp visa mới:
     ```sql
     select ... from PROJECT where ID=1;
     select ... from EMPLOYEE where VISA in ('DTH', 'BHU', 'JHV');
     ```
  2. Cập nhật bảng liên kết `PROJECT_EMPLOYEE`:
     ```sql
     insert into PROJECT_EMPLOYEE (PROJECT_ID, EMPLOYEE_ID) values (1, 3);
     ```
  3. Cập nhật bảng `PROJECT` và tăng `VERSION` lên 2:
     ```sql
     update
         PROJECT
     set
         CUSTOMER='Les Retaites Populaires',
         END_DATE='2025-12-31',
         GROUP_ID=1,
         NAME='Facturation / Encaissements - Updated 2026',
         START_DATE='2025-01-01',
         STATUS='INP',
         VERSION=2
     where
         ID=1
         and VERSION=1;
     ```
- **HTTP Status:** `200 OK`
- **Response Body:** `ProjectDetailResponse` với tên mới, trạng thái `INP` và `version: 2`.

---

### TC-EDIT-05: Xử lý xung đột phiên bản (Optimistic Locking)
- **API Request:**  
  `PUT http://localhost:8080/projects/1` với `"version": 1` (khi dữ liệu trong DB đã ở `VERSION = 2`).
- **Câu lệnh SQL sinh ra:**
  ```sql
  update PROJECT set ... VERSION=2 where ID=1 and VERSION=1;
  ```
  *(Số dòng bị tác động: 0 row)*
- **Xử lý nội bộ:** Hibernate phát hiện 0 dòng được update $\rightarrow$ ném `OptimisticLockException` (hoặc Spring ném `ObjectOptimisticLockingFailureException`). `GlobalExceptionHandler` bắt và đọc template `error.optimistic.lock`.
- **HTTP Status:** `409 Conflict`
- **Response Body:**
  ```json
  {
    "status": 409,
    "errorCode": "OPTIMISTIC_LOCK_ERROR",
    "message": "The project has been modified by another user. Please refresh and try again",
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

### TC-EDIT-06: Truy cập ID dự án không tồn tại
- **API Request:**  
  `GET http://localhost:8080/projects/99999999`
- **Câu lệnh SQL:**
  ```sql
  select project0_.ID as id1_2_0_, ... from PROJECT project0_ where project0_.ID=99999999;
  ```
- **Xử lý nội bộ:** Kết quả rỗng $\rightarrow$ ném `ProjectNotFoundException(99999999)`.
- **HTTP Status:** `404 Not Found`
- **Response Body:**
  ```json
  {
    "status": 404,
    "errorCode": "PROJECT_NOT_FOUND",
    "message": "Project not found with id: 99999999",
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

## NHÓM 3: HIỂN THỊ BẢNG, PHÂN TRANG & SẮP XẾP (US02)

### TC-LIST-01: Cấu trúc bảng danh sách dự án đủ 7 cột
- **API Request:**  
  `GET http://localhost:8080/projects?page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra (QueryDSL kết hợp Paging):**
  ```sql
  -- Lấy dữ liệu 5 dòng trang đầu tiên:
  select
      project0_.ID as id1_2_,
      project0_.VERSION as version2_2_,
      project0_.CUSTOMER as customer3_2_,
      project0_.END_DATE as end_date4_2_,
      project0_.GROUP_ID as group_id9_2_,
      project0_.NAME as name5_2_,
      project0_.PROJECT_NUMBER as project_6_2_,
      project0_.START_DATE as start_da7_2_,
      project0_.STATUS as status8_2_
  from
      PROJECT project0_
  order by
      project0_.PROJECT_NUMBER asc limit 5 offset 0;

  -- Đếm tổng số bản ghi:
  select count(project0_.ID) as col_0_0_ from PROJECT project0_;
  ```
- **HTTP Status:** `200 OK`
- **Response Body (`Page<ProjectListResponse>`):**
  ```json
  {
    "content": [
      {
        "id": 1,
        "version": 1,
        "projectNumber": 1001,
        "name": "Facturation / Encaissements",
        "status": "NEW",
        "customer": "Les Retaites Populaires",
        "startDate": "2025-01-01"
      }
    ],
    "totalPages": 3,
    "totalElements": 12,
    "size": 5,
    "number": 0,
    "first": true,
    "last": false,
    "numberOfElements": 5
  }
  ```

---

### TC-LIST-02: Quy tắc hiển thị nút Delete (Chỉ hiển thị cho status NEW)
- **Phản ứng Backend:** Backend đã nạp sẵn trường `"status": "NEW"` / `"INP"` / `"PLA"` / `"FIN"` trong `ProjectListResponse` ở `TC-LIST-01`. Frontend đọc trường `status` này để quyết định có render icon thùng rác hay để trống. Không cần thêm query.

---

### TC-LIST-03: Sắp xếp động theo từng cột (Column Sorting)
- **API Request mẫu (Ví dụ click cột Name giảm dần):**  
  `GET http://localhost:8080/projects?page=0&size=5&sort=name,desc`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select project0_.ID as id1_2_, ...
  from PROJECT project0_
  order by project0_.NAME desc, project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
  *(Lưu ý: Tầng Repository tự động thêm `projectNumber asc` làm secondary sort để đảm bảo thứ tự phân trang ổn định).*
- **HTTP Status:** `200 OK`

---

### TC-LIST-04: Phân trang chuẩn Server-side (Pagination)
- **API Request (Khi click trang 2):**  
  `GET http://localhost:8080/projects?page=1&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select project0_.ID as id1_2_, ...
  from PROJECT project0_
  order by project0_.PROJECT_NUMBER asc limit 5 offset 5;
  ```
- **HTTP Status:** `200 OK`
- **Response Body:** `number: 1`, `size: 5`, danh sách gồm các dự án từ vị trí số 6 đến 10.

---

## NHÓM 4: TÌM KIẾM, LỌC NÂNG CAO & ĐỒNG BỘ URL (US02)

### TC-SEARCH-01: Tìm kiếm nhanh bằng từ khóa (Keyword - Debounce)
- **API Request:**  
  `GET http://localhost:8080/projects?keyword=Facturation&page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select project0_.ID as id1_2_, ...
  from PROJECT project0_
  where
      lower(project0_.NAME) like '%facturation%'
      or lower(project0_.CUSTOMER) like '%facturation%'
  order by project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
  *(Nếu keyword là chuỗi số ví dụ `3116`, điều kiện SQL tự động bổ sung `or project0_.PROJECT_NUMBER = 3116`).*
- **HTTP Status:** `200 OK`

---

### TC-SEARCH-02: Lọc theo trạng thái (Status Filter)
- **API Request:**  
  `GET http://localhost:8080/projects?status=NEW&page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select project0_.ID as id1_2_, ...
  from PROJECT project0_
  where project0_.STATUS='NEW'
  order by project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
- **HTTP Status:** `200 OK`

---

### TC-SEARCH-03: Kết hợp Từ khóa + Trạng thái
- **API Request:**  
  `GET http://localhost:8080/projects?keyword=IOC&status=INP&page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra:**
  ```sql
  select project0_.ID as id1_2_, ...
  from PROJECT project0_
  where
      (lower(project0_.NAME) like '%ioc%' or lower(project0_.CUSTOMER) like '%ioc%')
      and project0_.STATUS='INP'
  order by project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
- **HTTP Status:** `200 OK`

---

### TC-SEARCH-04: Nút Reset Search khôi phục toàn bộ
- **API Request:**  
  `GET http://localhost:8080/projects?page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra:** Không có điều kiện lọc trong mệnh đề WHERE, nạp lại dữ liệu đầy đủ.
- **HTTP Status:** `200 OK`

---

### TC-ADV-01: Đóng/Mở bảng Lọc nâng cao
- **Phản ứng Backend:** Hoàn toàn xử lý tại Frontend UI (State toggle), không gửi request xuống backend.

---

### TC-ADV-02: Lọc nâng cao theo Group (Leader Visa)
- **API Request:**  
  `GET http://localhost:8080/projects?leaderVisa=DTH&page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra (QueryDSL Cross Join & Foreign Key Resolution):**
  ```sql
  select
      project0_.ID as id1_2_,
      project0_.NAME as name5_2_,
      project0_.PROJECT_NUMBER as project_6_2_, ...
  from
      PROJECT project0_
  cross join
      "GROUP" group1_
  cross join
      EMPLOYEE employee2_
  where
      project0_.GROUP_ID=group1_.ID
      and group1_.GROUP_LEADER_ID=employee2_.ID
      and lower(employee2_.VISA)='dth'
  order by
      project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
- **HTTP Status:** `200 OK`

---

### TC-ADV-03: Lọc nâng cao theo Member Visa (Dùng MemberSuggest)
- **API Request:**  
  `GET http://localhost:8080/projects?memberVisas=BHU&page=0&size=5&sort=projectNumber,asc`
- **Câu lệnh SQL sinh ra (Subquery Exists tối ưu hiệu năng):**
  ```sql
  select
      project0_.ID as id1_2_,
      project0_.NAME as name5_2_,
      project0_.PROJECT_NUMBER as project_6_2_, ...
  from
      PROJECT project0_
  where
      exists (
          select 1
          from PROJECT_EMPLOYEE employees3_, EMPLOYEE employee4_
          where
              project0_.ID=employees3_.PROJECT_ID
              and employees3_.EMPLOYEE_ID=employee4_.ID
              and lower(employee4_.VISA)='bhu'
      )
  order by
      project0_.PROJECT_NUMBER asc limit 5 offset 0;
  ```
- **HTTP Status:** `200 OK`

---

### TC-ADV-04: Lọc sạch Param rỗng (Clean Params Rule)
- **API Request:**  
  `GET http://localhost:8080/projects?page=0&size=5&sort=projectNumber,asc`
- **Phản ứng Backend:** Backend không bị lỗi `MethodArgumentTypeMismatchException` hay `InvalidFormatException` vì Frontend đã loại bỏ hoàn toàn các chuỗi rỗng (`status=&startDateFrom=&...`). Tầng `SearchProjectCriteria.toPredicate()` chỉ build các biểu thức `BooleanExpression` khi thuộc tính thực sự khác null và không blank.

---

### TC-NAV-01: Đồng bộ 2 chiều với nút Back/Forward của trình duyệt
- **API Request khi ấn nút Back:**  
  `GET http://localhost:8080/projects?keyword=Facturation&page=0&size=5&sort=projectNumber,asc`
- **Phản ứng Backend:** Thực thi query và trả về kết quả tìm kiếm tương ứng chính xác như `TC-SEARCH-01`.

---

## NHÓM 5: QUY TẮC XÓA DỰ ÁN (US02 - DELETE RULES)

### TC-DEL-01: Popup xác nhận khi Xóa đơn lẻ
- **Phản ứng Backend:** Khi người dùng click `Cancel` trên Popup, không có request HTTP DELETE nào được kích hoạt. Không có bản ghi nào bị xóa.

---

### TC-DEL-02: Xác nhận xóa đơn lẻ thành công
- **API Request:**  
  `DELETE http://localhost:8080/projects/1`
- **Câu lệnh SQL sinh ra:**
  1. Nạp entity kiểm tra trạng thái:
     ```sql
     select project0_.ID as id1_2_0_, project0_.STATUS as status8_2_0_, ...
     from PROJECT project0_ where project0_.ID=1;
     ```
  2. Kiểm tra `project.getStatus() == ProjectStatus.NEW` $\rightarrow$ Hợp lệ.
  3. Xóa liên kết trong bảng trung gian `PROJECT_EMPLOYEE`:
     ```sql
     delete from PROJECT_EMPLOYEE where PROJECT_ID=1;
     ```
  4. Xóa bản ghi trong bảng `PROJECT`:
     ```sql
     delete from PROJECT where ID=1 and VERSION=1;
     ```
- **HTTP Status:** `204 No Content`
- **Response Body:** Trống (Empty).

---

### TC-DEL-03: Chọn nhiều dòng hiển thị thanh đếm (Multi-selection Bar)
- **Phản ứng Backend:** Xử lý hoàn toàn tại React Component State, không gọi API backend.

---

### TC-DEL-04: Xóa hàng loạt nhiều dự án NEW thành công
- **API Request:**  
  `DELETE http://localhost:8080/projects`
- **Request Payload:**
  ```json
  [1, 2]
  ```
- **Câu lệnh SQL sinh ra (Thực thi batch an toàn):**
  1. Kiểm tra điều kiện nghiệp vụ: Đảm bảo TẤT CẢ dự án trong danh sách đều có trạng thái `NEW`:
     ```sql
     select
         count(project0_.ID) as col_0_0_
     from
         PROJECT project0_
     where
         project0_.ID in (1, 2)
         and project0_.STATUS<>'NEW';
     ```
     *(Kết quả trả về: 0 bản ghi vi phạm).*
  2. Xóa hàng loạt trong bảng liên kết `PROJECT_EMPLOYEE`:
     ```sql
     delete from PROJECT_EMPLOYEE where PROJECT_ID in (1, 2);
     ```
  3. Xóa hàng loạt trong bảng `PROJECT`:
     ```sql
     delete from PROJECT where ID in (1, 2);
     ```
- **HTTP Status:** `204 No Content`
- **Response Body:** Trống (Empty).

---

### TC-DEL-05: Quy tắc CHẶN XÓA khi có dự án khác trạng thái NEW
- **Tình huống kích hoạt Backend (Bypass Frontend validation):**  
  `DELETE http://localhost:8080/projects` với payload: `[1, 3]` (trong đó dự án ID 3 có status là `INP`).
- **Câu lệnh SQL kiểm tra:**
  ```sql
  select
      count(project0_.ID) as col_0_0_
  from
      PROJECT project0_
  where
      project0_.ID in (1, 3)
      and project0_.STATUS<>'NEW';
  ```
  *(Kết quả trả về: 1 bản ghi vi phạm $\rightarrow$ `count > 0`).*
- **Xử lý nội bộ:** Ném `InvalidProjectStatusException()`. Toàn bộ thao tác xóa bị hủy bỏ (Transaction Rollback).
- **HTTP Status:** `400 Bad Request`
- **Response Body:**
  ```json
  {
    "status": 400,
    "errorCode": "INVALID_PROJECT_STATUS",
    "message": "Only projects with status NEW can be deleted",
    "timestamp": "2026-09-26T17:55:00.000"
  }
  ```

---

## NHÓM 6: ĐA NGÔN NGỮ (I18N - ENGLISH / FRENCH)

### TC-I18N-01: Chuyển đổi ngôn ngữ sang Tiếng Pháp (FR)
- **Header gửi kèm mọi request:**  
  `Accept-Language: fr`
- **Phản ứng Backend:**  
  Spring Boot `LocaleResolver` nhận diện locale là `Locale.FRENCH` và nạp thông điệp từ [`messages_fr.properties`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/resources/messages_fr.properties).
- **Ví dụ các phản hồi JSON khi có lỗi:**
  1. Trùng số dự án:
     ```json
     {
       "status": 400,
       "errorCode": "PROJECT_NUMBER_ALREADY_EXISTS",
       "message": "Le numéro de projet existe déjà. Veuillez sélectionner un autre numéro"
     }
     ```
  2. Xóa sai trạng thái:
     ```json
     {
       "status": 400,
       "errorCode": "INVALID_PROJECT_STATUS",
       "message": "Seuls les projets avec le statut NEW peuvent être supprimés"
     }
     ```
  3. Lỗi khoảng ngày tìm kiếm (`endDateFrom > endDateTo`):
     ```json
     {
       "status": 400,
       "errorCode": "VALIDATION_ERROR",
       "message": "Validation failed",
       "errors": {
         "endDateTo": "La date de fin 'À' doit être postérieure ou égale à la date 'De'"
       }
     }
     ```
  4. Visa không tồn tại:
     ```json
     {
       "status": 400,
       "errorCode": "VISA_NOT_FOUND",
       "message": "Les visas suivants n'existent pas: XYZ"
     }
     ```
  5. Xung đột phiên bản:
     ```json
     {
       "status": 409,
       "errorCode": "OPTIMISTIC_LOCK_ERROR",
       "message": "Le projet a été modifié par un autre utilisateur. Veuillez actualiser et réessayer"
     }
     ```

---

### TC-I18N-02: Chuyển đổi ngôn ngữ sang Tiếng Anh (EN)
- **Header gửi kèm mọi request:**  
  `Accept-Language: en`
- **Phản ứng Backend:**  
  Spring Boot nhận diện `Locale.ENGLISH` và nạp thông điệp từ [`messages.properties`](file:///C:/Users/dptn/IdeaProjects/pilot-project-back/src/main/resources/messages.properties) (trả về các câu tiếng Anh chuẩn tương ứng).
