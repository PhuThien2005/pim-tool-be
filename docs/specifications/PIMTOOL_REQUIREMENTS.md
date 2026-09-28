# PIM Tool – On-boarding Exercise Requirements Specification (v4.0)

> **Tài liệu gốc:** `On-boarding-PIMTool-Requirements-v4-Exercise.docx`  
> **Phiên bản:** 4.0 | **Tác giả:** GTN | **Người duyệt:** HMT, NQN  
> **Mục đích:** Hướng dẫn chi tiết bài tập On-boarding PIM Tool cho Developer (Java / Spring Boot).

---

## 1. Giới thiệu & Bối cảnh Nghiệp vụ (Business Context)

### 1.1 Thuật ngữ (Glossary)
| Thuật ngữ | Tên đầy đủ | Ý nghĩa |
| :--- | :--- | :--- |
| **PIM** | **Project Information Management** | Hệ thống phần mềm quản lý hồ sơ, tài liệu hành chính của các dự án. |

### 1.2 Phạm vi chức năng bài tập

Hệ thống PIM thực tế rất lớn, nhưng trong bài tập on-boarding này bạn chỉ cần tập trung triển khai **2 chức năng chính**:

| User Story | Mã | Mô tả |
| :--- | :--- | :--- |
| **Tạo mới & Cập nhật Dự án** | **US01** | Tạo dự án mới hoặc chỉnh sửa thông tin dự án hiện có. |
| **Danh sách & Tìm kiếm Dự án** | **US02** | Xem danh sách dự án, lọc/tìm kiếm, xóa dự án (đơn lẻ hoặc hàng loạt). |

#### Các phần nằm ngoài phạm vi (Out of Scope):
- Quản lý phân quyền người dùng (Role management).
- Đăng nhập, đăng xuất (Login, Logout).
- Trợ giúp (Help), Quản lý Khách hàng/Nhà cung cấp mới (New Customer/Supplier).

---

## 2. Chi tiết User Story 01: Tạo mới & Cập nhật Dự án (US01)

> **Mục tiêu:** Là một *Project Manager*, tôi muốn tạo mới một dự án trên PIM tool để tra cứu sau này, đồng thời có thể cập nhật thông tin của dự án đã có.

### 2.1 Thuộc tính User Story
- **Độ phức tạp (Complexity point):** 1.5
- **Mức độ ưu tiên (Priority/BV):** High
- **Sprint:** 1

### 2.2 Đặc tả nghiệp vụ (Specifications)
1. **Truy cập:**
   - Người dùng click menu **New $\rightarrow$ Project** để tạo mới.
   - Hoặc click vào số dự án (hyperlink `Project Number`) trong danh sách US02 để vào màn hình cập nhật.
2. **Tiêu đề màn hình (Screen Title):**
   - Chế độ Tạo mới: Tiêu đề là **`New Project`**.
   - Chế độ Cập nhật: Tiêu đề đổi thành **`Edit Project information`**.
3. **Các trường thông tin của Dự án:**
   - `Project Number` (Số dự án)
   - `Project Name` (Tên dự án)
   - `Customer` (Tên khách hàng)
   - `Group` (Nhóm thực hiện dự án)
   - `Members` (Danh sách VISA các thành viên)
   - `Status` (Trạng thái dự án)
   - `Start Date` (Ngày bắt đầu)
   - `End Date` (Ngày kết thúc)
4. **Quy tắc khi Cập nhật (Edit Mode):**
   - **`Project Number` KHÔNG ĐƯỢC PHÉP THAY ĐỔI** (Disabled / Read-only).
   - Form phải được tự động điền sẵn dữ liệu hiện tại của dự án.
5. **Quy tắc khi Tạo mới (New Mode):**
   - Trạng thái mặc định được chọn là **`NEW`** (`New`).
6. **Định dạng trường Members (Thành viên):**
   - Nhập một hoặc nhiều VISA, phân tách bởi dấu phẩy (`,`).
   - Ví dụ: `DTH, BHU, JHV`.
7. **Điều hướng sau khi lưu:**
   - Nếu lưu thành công (không có lỗi), hệ thống điều hướng quay về màn hình danh sách dự án (**Projects list page**).

### 2.3 Quy tắc Kiểm tra Hợp lệ & Thông báo Lỗi (Validation & Messages)
- **Đánh dấu trường lỗi:** Bất kỳ trường nào có lỗi validation phải được làm nổi bật (highlight đỏ trên giao diện).
- **Thiếu trường bắt buộc:**
  - *Thông báo:* `Please enter all the mandatory fields (*).`
- **Trùng số dự án (Duplicate Project Number):**
  - *Thông báo:* `The project number already existed. Please select a different project number`
- **VISA không hợp lệ / không tồn tại:**
  - *Thông báo:* `The following visas do not exist: {visa list}.`
  - *(Ví dụ: `The following visas do not exist: ABC, XYZ.`)*
- **Kiểm tra ngày tháng (Date Range):**
  - Nếu `End Date` được nhập, `End Date` phải lớn hơn `Start Date`.
- **Lỗi hệ thống ngoài dự kiến (Unexpected Technical Error):**
  - Gặp lỗi mạng, lỗi database,... chuyển hướng người dùng đến trang lỗi riêng với thông điệp:  
    `Unexpected error occurred [<detail of the error>]. Please contact your administrator`

### 2.4 Tiêu chí hoàn thành (Done Tests / Acceptance Criteria)
| Kịch bản kiểm thử | Kết quả mong đợi |
| :--- | :--- |
| **TC 1.1** Tạo dự án mới nhưng để trống toàn bộ trường. | Nhận thông báo lỗi về các trường bắt buộc (`Please enter all the mandatory fields (*)`). |
| **TC 1.2** Tạo dự án mới và điền đầy đủ, hợp lệ tất cả các trường. | Dự án lưu thành công, hệ thống chuyển hướng về màn hình **Projects list**. |
| **TC 1.3** Cập nhật dự án hiện có với thông tin mới. | Dữ liệu cập nhật được lưu thành công vào cơ sở dữ liệu. |

### 2.5 Tính năng Nâng cao (Advanced Task - US01)
- **Suggestion box cho trường Members:**
  - Khi gõ phím vào ô nhập thành viên, hiển thị danh sách gợi ý gồm mã VISA và Họ tên nhân viên tương ứng.
  - Người dùng click chọn một nhân viên thì mã VISA tự động được thêm vào ô nhập.

---

## 3. Chi tiết User Story 02: Danh sách & Tìm kiếm Dự án (US02)

> **Mục tiêu:** Là một *Project Manager*, tôi muốn xem danh sách các dự án và tìm kiếm dự án theo tiêu chí để tra cứu thông tin hoặc cập nhật/xóa khi cần.

### 3.1 Thuộc tính User Story
- **Độ phức tạp (Complexity point):** 2.0
- **Mức độ ưu tiên (Priority/BV):** High
- **Sprint:** 1

### 3.2 Đặc tả nghiệp vụ (Specifications)
1. **Hiển thị mặc định:**
   - Khi mở trang danh sách, toàn bộ dự án được hiển thị.
   - Sắp xếp mặc định: **Theo `Project Number` tăng dần (ASC)**.
   - *Lưu ý cơ bản:* Chưa bắt buộc phân trang ở mức cơ bản (Pagination is not required in basic level).
2. **Bộ lọc tìm kiếm (Search Criteria):**
   - Tìm kiếm kết hợp theo 4 tiêu chí: **Name**, **Number**, **Customer**, và **Status**.
   - Nếu không nhập tiêu chí nào $\rightarrow$ Hiển thị tất cả dự án.
3. **Nút "Reset Search":**
   - Xóa toàn bộ tiêu chí tìm kiếm đang nhập và tải lại toàn bộ danh sách dự án ban đầu.
4. **Liên kết điều hướng:**
   - Trên bảng kết quả, `Project Number` hiển thị dưới dạng **hyperlink**. Click vào link sẽ điều hướng sang màn hình **Edit Project** (US01) của dự án đó.
5. **Chức năng Xóa dự án (Deletion):**
   - **Xóa đơn lẻ (Single delete):** Click vào icon xóa trên từng dòng.
   - **Xóa hàng loạt (Multiple delete):** Tích chọn checkbox nhiều dự án và bấm nút **`Delete selected item`**.
   - **RÀNG BUỘC NGHIỆP VỤ BẮT BUỘC:**  
     **CHỈ CHO PHÉP XÓA DỰ ÁN CÓ TRẠNG THÁI LÀ `NEW`** (`A project can be deleted only its status is 'New'`).
6. **Lưu trạng thái bộ lọc (Preserve Search Criteria):**
   - Khi người dùng điều hướng từ trang Project List sang trang khác (ví dụ: sang Edit Project hoặc bấm Cancel quay lại), **tiêu chí tìm kiếm trước đó phải được giữ nguyên**.
   - Khi quay lại trang Project List, cả form tìm kiếm và bảng kết quả đều phải hiển thị theo tiêu chí đã lưu.

### 3.3 Tiêu chí hoàn thành (Done Tests / Acceptance Criteria)
| Kịch bản kiểm thử | Kết quả mong đợi |
| :--- | :--- |
| **TC 2.1** Mở trang danh sách dự án. | Danh sách dự án hiển thị và được sắp xếp tăng dần theo `Project Number`. |
| **TC 2.2** Nhập từ khóa tìm kiếm (Name, Number, Customer). | Bảng kết quả chỉ hiển thị các dự án có tên hoặc số khớp với từ khóa. |
| **TC 2.3** Chọn lọc theo Status. | Bảng kết quả chỉ hiển thị các dự án có Status khớp với giá trị chọn. |
| **TC 2.4** Chọn dự án và bấm "Delete". | Hệ thống chỉ cho phép xóa nếu dự án có status là `NEW`. |
| **TC 2.5** Vào màn hình New/Edit, sau đó bấm nút "Cancel". | Hệ thống quay về màn hình Project List và giữ nguyên các tiêu chí tìm kiếm trước đó. |

### 3.4 Tính năng Nâng cao (Advanced Tasks - US02)
- **Phân trang (Pagination)**.
- **Bảng dữ liệu hỗ trợ sắp xếp theo Header cột (Sorting headers)**.
- **Mở rộng bộ lọc nâng cao (Show / Hide Advanced Filter):**
  - Bổ sung tiêu chí: *Project Leader*, *Containing Member (chứa thành viên)*, *Start date từ/đến (`startDateFrom`, `startDateTo`)*, *End date từ/đến (`endDateFrom`, `endDateTo`)*.

---

## 4. Thiết kế Cơ sở Dữ liệu (Database Specifications)

### 4.1 Bảng `PROJECT`
| Tên cột | Kiểu dữ liệu | Bắt buộc (Mandatory) | Mô tả chi tiết |
| :--- | :--- | :---: | :--- |
| `ID` | `NUMBER(19,0)` | Yes | Khóa chính (Surrogate key). |
| `GROUP_ID` | `NUMBER(19,0)` | Yes | Khóa ngoại trỏ đến bảng `GROUP`. |
| `PROJECT_NUMBER` | `NUMBER(4,0)` | Yes | Số thứ tự dự án. **Unique (duy nhất)**. |
| `NAME` | `VARCHAR(50)` | Yes | Tên dự án. |
| `CUSTOMER` | `VARCHAR(50)` | Yes | Tên khách hàng. |
| `STATUS` | `CHAR(3)` | Yes | Trạng thái: `NEW` (New), `PLA` (Planned), `INP` (In progress), `FIN` (Finished). |
| `START_DATE` | `DATE` | Yes | Ngày bắt đầu dự án. |
| `END_DATE` | `DATE` | No | Ngày kết thúc. Nếu có giá trị thì phải sau `START_DATE`. |
| `VERSION` | `NUMBER(10,0)` | Yes | Cột kiểm soát đồng thời (Optimistic locking version). |

### 4.2 Bảng `PROJECT_EMPLOYEE` (Bảng trung gian n-n)
| Tên cột | Kiểu dữ liệu | Bắt buộc (Mandatory) | Mô tả chi tiết |
| :--- | :--- | :---: | :--- |
| `PROJECT_ID` | `NUMBER(19,0)` | Yes | Khóa ngoại trỏ đến bảng `PROJECT`. |
| `EMPLOYEE_ID` | `NUMBER(19,0)` | Yes | Khóa ngoại trỏ đến bảng `EMPLOYEE`. |

### 4.3 Bảng `EMPLOYEE`
| Tên cột | Kiểu dữ liệu | Bắt buộc (Mandatory) | Mô tả chi tiết |
| :--- | :--- | :---: | :--- |
| `ID` | `NUMBER(19,0)` | Yes | Khóa chính (Surrogate key). |
| `VISA` | `CHAR(3)` | Yes | Mã VISA nhân viên (3 chữ cái in hoa, unique). |
| `FIRST_NAME` | `VARCHAR(50)` | Yes | Tên nhân viên. |
| `LAST_NAME` | `VARCHAR(50)` | Yes | Họ nhân viên. |
| `BIRTH_DATE` | `DATE` | Yes | Ngày sinh nhân viên. |
| `VERSION` | `NUMBER(10,0)` | Yes | Cột kiểm soát đồng thời (Optimistic locking version). |

### 4.4 Bảng `GROUP`
| Tên cột | Kiểu dữ liệu | Bắt buộc (Mandatory) | Mô tả chi tiết |
| :--- | :--- | :---: | :--- |
| `ID` | `NUMBER(19,0)` | Yes | Khóa chính (Surrogate key). |
| `GROUP_LEADER_ID` | `NUMBER(19,0)` | Yes | Khóa ngoại trỏ đến bảng `EMPLOYEE` (Trưởng nhóm). |
| `VERSION` | `NUMBER(10,0)` | Yes | Cột kiểm soát đồng thời (Optimistic locking version). |

---

## 5. Yêu cầu Kỹ thuật dành cho Java Developer (Technical Notes)

1. **Kiến trúc 3 tầng (3-tier Architecture):**
   - `Controller` (Web/REST API) $\rightarrow$ `Service` (Business logic) $\rightarrow$ `Repository/DAO` (Database access).
2. **Kế thừa Entity:**
   - Các Entity (`Project`, `Employee`, `Group`) phải kế thừa chung `AbstractBaseEntity` chứa cột `id` và `version`.
3. **Cơ sở dữ liệu & Truy vấn:**
   - Sử dụng **Spring Data JPA / Hibernate**.
   - Bắt buộc áp dụng **QueryDSL** để xây dựng các câu truy vấn động (đặc biệt cho chức năng tìm kiếm dự án US02).
   - Cấu trúc bảng Database phải chứa **chính xác các cột trong mục 4, không thừa không thiếu**.
4. **Quản lý Giao dịch (Transaction Management):**
   - Xử lý `@Transactional` ở tầng **Business Layer (`Service`)**, không đặt trên Controller hay Repository.
5. **Xử lý xung đột đồng thời (Concurrency Control):**
   - Phải xử lý trường hợp sửa/xóa cùng lúc bằng cơ chế **Optimistic Locking** (`@Version` trên cột `VERSION`).
   - Xử lý ngoại lệ `OptimisticLockException` / `ObjectOptimisticLockingFailureException` (HTTP 409 Conflict).
6. **Quản lý Ngoại lệ (Exception Handling):**
   - Định nghĩa các Business Exception chuyên biệt (ví dụ: `ProjectNumberAlreadyExistsException`, `VisaNotFoundException`, `InvalidProjectStatusException`).
   - Bắt buộc xử lý tập trung bằng cơ chế **Spring Exception Resolver (`@RestControllerAdvice`)**.
   - Bắt và log đầy đủ thông tin chi tiết lỗi hệ thống (`log.error`) để developer có thể trace vết khi cần.
7. **Đa ngôn ngữ (Multilingual / i18n):**
   - Áp dụng cơ chế đa ngôn ngữ (`MessageSource`) cho toàn bộ nhãn hiển thị và câu thông báo lỗi (hỗ trợ tiếng Anh `en` và tiếng Pháp `fr`).
8. **Quy tắc dọn dẹp mã nguồn (Clean Code Rule):**
   - **`Unused code from previous exercises must be removed.`** *(Toàn bộ mã nguồn cũ không sử dụng từ các bài tập trước - như Task, TaskAudit - phải được dọn dẹp sạch sẽ).*

---

## 6. Ma trận Tra cứu Thông báo Lỗi & Mã Ngoại lệ (Cheat Sheet)

| Tình huống lỗi | Thông báo người dùng (EN) | Exception Class | HTTP Status |
| :--- | :--- | :--- | :---: |
| **Thiếu trường bắt buộc** | `Please enter all the mandatory fields (*).` | `MethodArgumentNotValidException` | `400` |
| **Trùng số dự án** | `The project number already existed. Please select a different project number` | `ProjectNumberAlreadyExistsException` | `400` |
| **VISA không tồn tại** | `The following visas do not exist: {visa list}.` | `VisaNotFoundException` | `400` |
| **Xóa dự án không phải NEW** | `Only projects with status NEW can be deleted` | `InvalidProjectStatusException` | `400` |
| **Không tìm thấy dự án** | `Project not found with id: {id}` | `ProjectNotFoundException` | `404` |
| **Xung đột sửa đồng thời** | `The project has been modified by another user. Please refresh and try again` | `OptimisticLockException` / `ObjectOptimisticLockingFailureException` | `409` |
| **Lỗi hệ thống bất ngờ** | `Unexpected error occurred [<detail of the error>]. Please contact your administrator` | `Exception.class` | `500` |
