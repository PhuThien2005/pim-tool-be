package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Table(name = "PROJECT", indexes = {
        @Index(name = "idx_project_status", columnList = "STATUS"),
        @Index(name = "idx_project_customer", columnList = "CUSTOMER"),
        @Index(name = "idx_project_group_id", columnList = "GROUP_ID"),
        @Index(name = "idx_project_status_customer", columnList = "STATUS, CUSTOMER")
})
public class Project extends AbstractBaseEntity {
    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    private Group group;

    @Column(name = "PROJECT_NUMBER", precision = 4, scale = 0, nullable = false, unique = true)
    private Integer projectNumber;

    @Column(name = "NAME", length = 50, nullable = false)
    private String name;

    @Column(name = "CUSTOMER", length = 50, nullable = false)
    private String customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", length = 3, nullable = false)
    private ProjectStatus status;

    public ProjectStatus getProjectStatus() {
        return status;
    }

    public void setProjectStatus(ProjectStatus status) {
        this.status = status;
    }

    @Column(name = "START_DATE", nullable = false)
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @BatchSize(size = 20)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "PROJECT_EMPLOYEE",
            joinColumns = @JoinColumn(name = "PROJECT_ID"),
            inverseJoinColumns = @JoinColumn(name = "EMPLOYEE_ID")
    )
    private Set<Employee> employees = new HashSet<>();

    public void setGroup(Group group) {
        if (this.group == group) {
            return;
        }
        if (this.group != null && this.group.getProjects() != null) {
            this.group.getProjects().remove(this);
        }
        this.group = group;
        if (group != null && group.getProjects() != null) {
            if (!group.getProjects().contains(this)) {
                group.getProjects().add(this);
            }
        }
    }

    public void removeGroup() {
        this.setGroup(null);
    }

    public void addEmployee(Employee employee) {
        if (employee != null) {
            if (this.employees == null) {
                this.employees = new HashSet<>();
            }
            this.employees.add(employee);
            if (employee.getProjects() != null) {
                employee.getProjects().add(this);
            }
        }
    }

    public void removeEmployee(Employee employee) {
        if (employee != null && this.employees != null) {
            this.employees.remove(employee);
            if (employee.getProjects() != null) {
                employee.getProjects().remove(this);
            }
        }
    }

    public void setEmployees(Set<Employee> employees) {
        if (this.employees != null) {
            for (Employee emp : new HashSet<>(this.employees)) {
                this.removeEmployee(emp);
            }
        }
        if (employees != null) {
            for (Employee emp : employees) {
                this.addEmployee(emp);
            }
        }
    }
}