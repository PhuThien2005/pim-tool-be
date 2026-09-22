package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.LazyToOne;
import org.hibernate.annotations.LazyToOneOption;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gtn
 *
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "EMPLOYEE", indexes = {
        @Index(name = "idx_employee_visa", columnList = "VISA", unique = true)
})
public class Employee extends AbstractBaseEntity {
    @Column(name = "VISA", length = 3, nullable = false)
    private String visa;

    @Column(name = "FIRST_NAME", length = 50, nullable = false)
    private String firstName;

    @Column(name = "LAST_NAME", length = 50, nullable = false)
    private String lastName;

    @Column(name = "BIRTH_DATE", nullable = false)
    private LocalDate birthDate;

    @BatchSize(size = 20)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Project> projects = new HashSet<>();

    @Setter(AccessLevel.NONE)
    @OneToOne(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    @LazyToOne(LazyToOneOption.NO_PROXY)
    private Group group;

    public void addProject(Project project) {
        if (project != null) {
            if (this.projects == null) {
                this.projects = new HashSet<>();
            }
            this.projects.add(project);
            if (project.getEmployees() != null) {
                project.getEmployees().add(this);
            }
        }
    }

    public void removeProject(Project project) {
        if (project != null && this.projects != null) {
            this.projects.remove(project);
            if (project.getEmployees() != null) {
                project.getEmployees().remove(this);
            }
        }
    }

    public void setProjects(Set<Project> projects) {
        if (this.projects != null) {
            for (Project prj : new HashSet<>(this.projects)) {
                this.removeProject(prj);
            }
        }
        if (projects != null) {
            for (Project prj : projects) {
                this.addProject(prj);
            }
        }
    }

    public void setGroup(Group group) {
        if (this.group == group) {
            return;
        }
        Group oldGroup = this.group;
        this.group = group;
        if (oldGroup != null && oldGroup.getGroupLeader() == this) {
            oldGroup.setGroupLeader(null);
        }
        if (group != null && group.getGroupLeader() != this) {
            group.setGroupLeader(this);
        }
    }

    public void removeGroup() {
        this.setGroup(null);
    }
}
