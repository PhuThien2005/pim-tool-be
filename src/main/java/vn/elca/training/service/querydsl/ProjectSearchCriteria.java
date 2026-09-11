package vn.elca.training.service.querydsl;

import java.time.LocalDate;

/**
 * DTO đóng gói các tiêu chí tìm kiếm động từ phía người dùng (Client / UI).
 * Người dùng có thể truyền vào một vài hoặc tất cả các trường, hoặc để trống.
 */
public class ProjectSearchCriteria {

    /**
     * Từ khóa tìm kiếm (có thể tìm trong tên dự án hoặc tên khách hàng)
     */
    private String keyword;

    /**
     * Tên khách hàng cụ thể
     */
    private String customer;

    /**
     * Ngày kết thúc dự án từ ngày (finishingDate >= finishingDateFrom)
     */
    private LocalDate finishingDateFrom;

    /**
     * Ngày kết thúc dự án đến ngày (finishingDate <= finishingDateTo)
     */
    private LocalDate finishingDateTo;

    /**
     * Tìm các dự án có chứa task có tên này
     */
    private String taskName;

    /**
     * Tìm các dự án có task được phân công cho username này
     */
    private String assigneeUsername;

    public ProjectSearchCriteria() {
    }

    public ProjectSearchCriteria(String keyword, String customer, LocalDate finishingDateFrom, LocalDate finishingDateTo) {
        this.keyword = keyword;
        this.customer = customer;
        this.finishingDateFrom = finishingDateFrom;
        this.finishingDateTo = finishingDateTo;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public LocalDate getFinishingDateFrom() {
        return finishingDateFrom;
    }

    public void setFinishingDateFrom(LocalDate finishingDateFrom) {
        this.finishingDateFrom = finishingDateFrom;
    }

    public LocalDate getFinishingDateTo() {
        return finishingDateTo;
    }

    public void setFinishingDateTo(LocalDate finishingDateTo) {
        this.finishingDateTo = finishingDateTo;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getAssigneeUsername() {
        return assigneeUsername;
    }

    public void setAssigneeUsername(String assigneeUsername) {
        this.assigneeUsername = assigneeUsername;
    }
}
