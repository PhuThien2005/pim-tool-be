# PIM Tool – Đặc tả Chi tiết Giao diện Lập trình Ứng dụng (RESTful API Specification)

Tài liệu này cung cấp đặc tả kỹ thuật chi tiết toàn bộ các RESTful API của hệ thống **Project Information Management (PIM Tool)** Backend, phục vụ cho quá trình tích hợp giữa Frontend (Angular / React / Vue) và Backend (Spring Boot).

---

## 1. Quy ước Chung (General Conventions)

- **Base URL:** `http://localhost:8080`
- **Định dạng dữ liệu (Content-Type):** `application/json;charset=UTF-8`
- **Định dạng ngày tháng:** Chuẩn ISO-8601 dạng chuỗi `yyyy-MM-dd` (ví dụ: `2025-01-15`).
- **Hỗ trợ Đa ngôn ngữ (i18n):**
  - Gửi header `Accept-Language: en` (Tiếng Anh - mặc định) hoặc `Accept-Language: fr` (Tiếng Pháp).
  - Toàn bộ thông báo lỗi trả về trong trường `message` sẽ tự động dịch tương ứng theo locale được gửi.

---

## 2. Quy ước Phân trang & Sắp xếp (Pagination & Sorting)

### 2.1 Các tham số phân trang chuẩn:
| Tham số | Kiểu dữ liệu | Mặc định | Ý nghĩa |
| :--- | :--- | :--- | :--- |
| `page` | `Integer` | `0` | Chỉ số trang cần lấy (bắt đầu từ 0). |
| `size` | `Integer` | Tuỳ endpoint | Số lượng phần tử trên mỗi trang. |
| `sort` | `String` | Tuỳ endpoint | Tiêu chí sắp xếp, định dạng: `tên_trường,chiều` (`asc` hoặc `desc`). |

### 2.2 Sắp xếp (Sorting):
- **Cơ chế:** Toàn bộ việc sắp xếp được thực thi ở **phía Server trên toàn bộ tập dữ liệu (Server-side sorting)**. Khi người dùng bấm vào Header của bảng trên Frontend, Frontend cần gọi lại API kèm tham số `sort` mới.
- **Các trường hỗ trợ sắp xếp cho bảng Dự án (khớp 100% với Header giao diện):**
  - `number` hoặc `projectNumber`: Số thứ tự dự án (Mặc định: `asc`).
  - `name`: Tên dự án.
  - `status`: Trạng thái dự án (`NEW`, `PLA`, `INP`, `FIN`).
  - `customer`: Tên khách hàng.
  - `startDate`: Ngày bắt đầu dự án.
- **Cú pháp gọi Sort:**
  - *Sort đơn cột:* `?sort=name,desc`
  - *Sort đa cột (nhiều tiêu chí kết hợp):* Truyền lặp lại tham số `sort`:  
    `?sort=status,asc&sort=startDate,desc`  
    *(Ưu tiên sắp theo trạng thái tăng dần; nếu trùng trạng thái thì sắp theo ngày bắt đầu giảm dần)*.

---

## 3. Danh mục API Chi tiết

### Nhóm 1: Quản lý Dự án (`/projects`)

---

#### 1.1 Tìm kiếm & Phân trang Dự án
- **Endpoint:** `GET /projects`
- **Mô tả:** Tra cứu danh sách dự án với bộ lọc linh hoạt và phân trang `Page<T>`.
- **Query Parameters:**
  
  **a) Bộ lọc cơ bản (Basic Filter - Luôn hiển thị trên giao diện):**
  | Tham số | Kiểu dữ liệu | Bắt buộc | Mô tả |
  | :--- | :--- | :---: | :--- |
  | `keyword` | `String` | Không | Từ khóa tìm kiếm chung trên 3 cột (tìm tương đối theo `name`, `customer` hoặc khớp chính xác số `projectNumber`). |
  | `status` | `String` | Không | Dropdown lọc chính xác theo trạng thái dự án: `NEW`, `PLA`, `INP`, `FIN`. |

  **b) Bộ lọc nâng cao (Advanced Filter - Mở rộng khi click nút "Show/Hide Advanced Filter"):**
  | Tham số | Kiểu dữ liệu | Bắt buộc | Mô tả |
  | :--- | :--- | :---: | :--- |
  | `leaderVisa` | `String` | Không | Lọc theo mã VISA của trưởng nhóm (Group Leader). |
  | `memberVisa` | `String` | Không | Lọc dự án có chứa thành viên mang mã VISA này. |
  | `startDateFrom`| `LocalDate` | Không | Ngày bắt đầu từ (`yyyy-MM-dd`). |
  | `startDateTo` | `LocalDate` | Không | Ngày bắt đầu đến (`yyyy-MM-dd`). |
  | `endDateFrom` | `LocalDate` | Không | Ngày kết thúc từ (`yyyy-MM-dd`). |
  | `endDateTo` | `LocalDate` | Không | Ngày kết thúc đến (`yyyy-MM-dd`). |

  **c) Tham số phân trang & sắp xếp (Pagination & Sorting):**
  | Tham số | Kiểu dữ liệu | Bắt buộc | Mô tả |
  | :--- | :--- | :---: | :--- |
  | `page` | `Integer` | Không | Chỉ số trang (mặc định: `0`). |
  | `size` | `Integer` | Không | Số phần tử/trang (mặc định: `10` hoặc theo cấu hình client). |
  | `sort` | `String` | Không | Sắp xếp (mặc định: `projectNumber,asc`). Hỗ trợ: `number`, `name`, `status`, `customer`, `startDate`. |

- **Phản hồi thành công (HTTP 200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "version": 1,
      "projectNumber": 1001,
      "name": "EFV",
      "customer": "Customer A",
      "group": {
        "id": 1,
        "version": 1,
        "groupLeader": {
          "id": 1,
          "version": 1,
          "visa": "LIN",
          "firstName": "Linh",
          "lastName": "Nguyen",
          "birthDate": "1990-05-12"
        }
      },
      "status": "NEW",
      "startDate": "2025-01-01",
      "endDate": null
    }
  ],
  "pageable": {
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "totalElements": 1,
  "totalPages": 1,
  "last": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 1,
  "first": true,
  "empty": false
}
```

---

#### 1.2 Lấy Chi tiết một Dự án
- **Endpoint:** `GET /projects/{projectId}`
- **Mô tả:** Lấy thông tin chi tiết đầy đủ của một dự án (bao gồm phiên bản `version`, nhóm thực hiện và toàn bộ danh sách thành viên) để điền vào form Edit Project.
- **Path Variables:**
  - `projectId` (`Long`, bắt buộc): ID của dự án cần xem.
- **Phản hồi thành công (HTTP 200 OK):**
```json
{
  "id": 1,
  "version": 1,
  "projectNumber": 1001,
  "name": "EFV",
  "customer": "Customer A",
  "status": "NEW",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",
  "group": {
    "id": 1,
    "version": 1,
    "groupLeader": {
      "id": 1,
      "version": 1,
      "visa": "LIN",
      "firstName": "Linh",
      "lastName": "Nguyen",
      "birthDate": "1990-05-12"
    }
  },
  "employees": [
    {
      "id": 2,
      "version": 1,
      "visa": "HOA",
      "firstName": "Hoa",
      "lastName": "Tran",
      "birthDate": "1992-08-20"
    }
  ]
}
```
- **Lỗi có thể trả về:**
  - `HTTP 404 Not Found` (Mã lỗi: `PROJECT_NOT_FOUND`): Dự án không tồn tại.

---

#### 1.3 Tạo mới Dự án
- **Endpoint:** `POST /projects`
- **Mô tả:** Tạo mới một dự án vào hệ thống.
- **Request Body (`CreateProjectRequest`):**
```json
{
  "projectNumber": 1002,
  "name": "CXTRANET",
  "customer": "Customer B",
  "groupId": 1,
  "status": "NEW",
  "startDate": "2025-02-01",
  "endDate": "2025-11-30",
  "visas": ["LIN", "HOA"]
}
```
- **Ràng buộc Validation từng trường:**
  - `projectNumber`: Bắt buộc (`@NotNull`), kiểu số nguyên, từ 1 đến 9999 (`@Min(1) @Max(9999)`), duy nhất trong toàn hệ thống.
  - `name`: Bắt buộc (`@NotBlank`), tối đa 50 ký tự (`@Size(max = 50)`).
  - `customer`: Bắt buộc (`@NotBlank`), tối đa 50 ký tự (`@Size(max = 50)`).
  - `groupId`: Bắt buộc (`@NotNull`), ID nhóm thực hiện dự án.
  - `status`: Không bắt buộc (nếu không truyền sẽ mặc định là `NEW`). Giá trị hợp lệ: `NEW`, `PLA`, `INP`, `FIN`.
  - `startDate`: Bắt buộc (`@NotNull`), định dạng `yyyy-MM-dd`.
  - `endDate`: Không bắt buộc. Nếu có thì bắt buộc phải sau `startDate` (`@StartBeforeEndDate`).
  - `visas`: Tập hợp mã VISA của các thành viên. Mỗi mã VISA bắt buộc đúng định dạng 3 chữ cái in hoa (`@ValidVisas`, regex `^[A-Z]{3}$`).
- **Phản hồi thành công (HTTP 201 Created):**
  - Trả về đối tượng `ProjectDetailResponse` vừa tạo thành công.
- **Lỗi có thể trả về:**
  - `HTTP 400 Bad Request` (`VALIDATION_ERROR`): Thiếu trường bắt buộc hoặc sai định dạng.
  - `HTTP 400 Bad Request` (`PROJECT_NUMBER_ALREADY_EXISTS`): Số dự án đã tồn tại trong database.
  - `HTTP 400 Bad Request` (`VISA_NOT_FOUND`): Có một hoặc nhiều mã VISA không tồn tại trong hệ thống.
  - `HTTP 404 Not Found` (`GROUP_NOT_FOUND`): `groupId` chỉ định không tồn tại.

---

#### 1.4 Cập nhật Dự án (Edit Mode & Optimistic Locking)
- **Endpoint:** `PUT /projects/{projectId}`
- **Mô tả:** Cập nhật thông tin của dự án đã có. Áp dụng cơ chế **Optimistic Locking** để ngăn chặn tình trạng ghi đè đồng thời.
- **Path Variables:**
  - `projectId` (`Long`, bắt buộc): ID của dự án cần cập nhật.
- **Request Body (`UpdateProjectRequest`):**
```json
{
  "version": 1,
  "projectNumber": 1001,
  "name": "EFV Enhanced",
  "customer": "Customer A Prime",
  "groupId": 1,
  "status": "INP",
  "startDate": "2025-01-01",
  "endDate": "2026-06-30",
  "visas": ["LIN", "DTH"]
}
```
- **Ràng buộc quan trọng:**
  - `version`: **BẮT BUỘC (`@NotNull`)**. Giá trị `version` nhận được lúc gọi `GET /projects/{projectId}`.
  - `projectNumber`: Trường Read-only (không được phép thay đổi theo quy tắc nghiệp vụ US01). Backend sẽ bỏ qua trường này và giữ nguyên số dự án ban đầu.
- **Phản hồi thành công (HTTP 200 OK):**
  - Trả về `ProjectDetailResponse` với `version` mới (ví dụ tăng lên 2).
- **Lỗi có thể trả về:**
  - `HTTP 404 Not Found` (`PROJECT_NOT_FOUND`): Dự án không tồn tại.
  - `HTTP 409 Conflict` (`OPTIMISTIC_LOCK_ERROR`): **Xung đột sửa đổi đồng thời**. Xuất hiện khi `version` gửi lên không khớp với `version` hiện tại trong DB (nghĩa là đã có người khác sửa dự án này trước đó). Thông báo: *"The project has been modified by another user. Please refresh and try again."*
  - `HTTP 400 Bad Request` (`VISA_NOT_FOUND` / `VALIDATION_ERROR`): Dữ liệu không hợp lệ hoặc visa không tồn tại.

---

#### 1.5 Xóa Đơn lẻ một Dự án
- **Endpoint:** `DELETE /projects/{projectId}`
- **Mô tả:** Xóa một dự án theo ID.
- **Ràng buộc nghiệp vụ:** **Chỉ cho phép xóa dự án có trạng thái là `NEW`**.
- **Path Variables:**
  - `projectId` (`Long`, bắt buộc): ID của dự án.
- **Phản hồi thành công (HTTP 204 No Content):** Không có nội dung trả về.
- **Lỗi có thể trả về:**
  - `HTTP 400 Bad Request` (`INVALID_PROJECT_STATUS`): Dự án có trạng thái khác `NEW` (ví dụ: `PLA`, `INP`, `FIN`). Thông báo: *"Only projects with status NEW can be deleted"*.
  - `HTTP 404 Not Found` (`PROJECT_NOT_FOUND`): Dự án không tồn tại.

---

#### 1.6 Xóa Hàng loạt Dự án (Bulk Delete)
- **Endpoint:** `DELETE /projects`
- **Mô tả:** Xóa nhiều dự án cùng một lúc theo danh sách ID được chọn qua checkbox.
- **Ràng buộc nghiệp vụ:** **Toàn bộ** các dự án trong danh sách yêu cầu xóa bắt buộc phải có trạng thái `NEW`. Nếu tồn tại bất kỳ dự án nào khác `NEW`, hệ thống sẽ hủy toàn bộ thao tác xóa.
- **Request Body (`List<Long>`):**
```json
[1, 2, 4]
```
- **Phản hồi thành công (HTTP 204 No Content):** Không có nội dung trả về.
- **Lỗi có thể trả về:**
  - `HTTP 400 Bad Request` (`INVALID_PROJECT_STATUS`): Có chứa dự án không phải trạng thái `NEW`.
  - `HTTP 404 Not Found` (`PROJECT_NOT_FOUND`): Có chứa ID dự án không tồn tại trong DB.

---

### Nhóm 2: Nhóm Dự án (`/groups`)

---

#### 2.1 Lấy Danh sách Nhóm
- **Endpoint:** `GET /groups`
- **Mô tả:** Lấy danh sách các nhóm phục vụ cho dropdown `<select>` chọn Group trong màn hình New/Edit Project. Dữ liệu trả về dưới dạng `Slice<GroupListResponse>` hỗ trợ cuộn vô tận (Infinite Scroll) với hiệu năng cao, không tốn query `count(*)`, đã nạp sẵn thông tin trưởng nhóm (`groupLeader`).
- **Query Parameters:**
  | Tham số | Kiểu dữ liệu | Mặc định | Mô tả |
  | :--- | :--- | :--- | :--- |
  | `page` | `Integer` | `0` | Chỉ số trang. |
  | `size` | `Integer` | `20` | Số lượng nhóm mỗi lần tải. |
  | `sort` | `String` | `id,asc` | Sắp xếp theo ID nhóm. |

- **Phản hồi thành công (HTTP 200 OK):**
```json
{
  "content": [
    {
      "id": 1,
      "version": 1,
      "groupLeader": {
        "id": 1,
        "version": 1,
        "visa": "LIN",
        "firstName": "Linh",
        "lastName": "Nguyen",
        "birthDate": "1990-05-12"
      }
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 20,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 1,
  "empty": false
}
```

---

### Nhóm 3: Nhân viên & Thành viên (`/employees`)

---

#### 3.1 Tìm kiếm Gợi ý Thành viên (Autocomplete Suggestion Box)
- **Endpoint:** `GET /employees`
- **Mô tả:** Tìm kiếm gợi ý nhân viên khi người dùng gõ vào ô nhập thành viên (`Members`). Hỗ trợ tìm kiếm theo tiền tố/chứa trong mã `VISA`, `firstName` hoặc `lastName`. Dữ liệu trả về dưới dạng `Slice<EmployeeListResponse>` (không đếm tổng số bản ghi để tăng tốc độ phản hồi gợi ý).
- **Query Parameters:**
  | Tham số | Kiểu dữ liệu | Mặc định | Mô tả |
  | :--- | :--- | :--- | :--- |
  | `keyword` | `String` | `""` | Từ khóa cần tìm (nếu để trống hoặc khoảng trắng, backend trả về danh sách rỗng). |
  | `page` | `Integer` | `0` | Chỉ số trang gợi ý. |
  | `size` | `Integer` | `10` | Số lượng kết quả gợi ý tối đa mỗi lần. |
  | `sort` | `String` | `visa,asc` | Sắp xếp theo mã VISA tăng dần. |

- **Phản hồi thành công (HTTP 200 OK):**
```json
{
  "content": [
    {
      "id": 2,
      "version": 1,
      "visa": "HOA",
      "firstName": "Hoa",
      "lastName": "Tran",
      "birthDate": "1992-08-20"
    },
    {
      "id": 1,
      "version": 1,
      "visa": "LIN",
      "firstName": "Linh",
      "lastName": "Nguyen",
      "birthDate": "1990-05-12"
    }
  ],
  "pageable": {
    "pageNumber": 0,
    "pageSize": 10,
    "sort": {
      "sorted": true,
      "unsorted": false,
      "empty": false
    },
    "offset": 0,
    "paged": true,
    "unpaged": false
  },
  "size": 10,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 2,
  "empty": false
}
```

---

## 4. Định dạng Lỗi Chuẩn (Standard Error Response)

Khi xảy ra lỗi (mã HTTP `4xx` hoặc `5xx`), toàn bộ API trả về cấu trúc đối tượng JSON đồng nhất như sau:

```json
{
  "status": 400,
  "errorCode": "VALIDATION_ERROR",
  "message": "Please enter all the mandatory fields (*)",
  "errors": {
    "name": "must not be blank",
    "projectNumber": "must not be null"
  },
  "timestamp": "2026-09-22T16:20:00"
}
```

### Bảng tra cứu Mã Lỗi Hệ thống:
| HTTP Status | `errorCode` | Ý nghĩa |
| :---: | :--- | :--- |
| `400` | `VALIDATION_ERROR` | Thiếu hoặc sai định dạng các trường bắt buộc (`*`). |
| `400` | `PROJECT_NUMBER_ALREADY_EXISTS` | Số dự án đã tồn tại trong hệ thống. |
| `400` | `VISA_NOT_FOUND` | Một hoặc nhiều mã VISA không tồn tại trong danh mục nhân viên. |
| `400` | `INVALID_PROJECT_STATUS` | Vi phạm ràng buộc: chỉ dự án trạng thái `NEW` mới được phép xóa. |
| `400` | `MISSING_PARAMETER` | Thiếu tham số request bắt buộc. |
| `400` | `MALFORMED_JSON_REQUEST` | Định dạng JSON gửi lên bị sai cú pháp. |
| `404` | `PROJECT_NOT_FOUND` | Không tìm thấy dự án theo ID cung cấp. |
| `404` | `GROUP_NOT_FOUND` | Không tìm thấy nhóm theo ID cung cấp. |
| `409` | `OPTIMISTIC_LOCK_ERROR` | Xung đột phiên bản dữ liệu khi hai người cùng sửa một dự án. |
| `500` | `INTERNAL_SERVER_ERROR` | Lỗi kỹ thuật ngoài ý muốn (DB, mạng, exception chưa bắt). |
