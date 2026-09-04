package vn.elca.training.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;

/**
 * @author gtn
 *
 */
public class ProjectDto {
    private Long id;
    private String name;
    private String customer;

    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate finishingDate;

    public ProjectDto() {
    }

    public ProjectDto(Long id, String name, String customer, LocalDate finishingDate) {
        this.id = id;
        this.name = name;
        this.customer = customer;
        this.finishingDate = finishingDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCustomer() {
        return customer;
    }

    public void setCustomer(String customer) {
        this.customer = customer;
    }

    public LocalDate getFinishingDate() {
        return finishingDate;
    }

    public void setFinishingDate(LocalDate finishingDate) {
        this.finishingDate = finishingDate;
    }
}
