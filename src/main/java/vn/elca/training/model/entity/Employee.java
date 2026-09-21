package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.BatchSize;

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
@Table(name = "EMPLOYEE")
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

    @BatchSize(size = 20)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    private Set<Group> groups = new HashSet<>();

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

    public void addGroup(Group group) {
        if (group != null) {
            if (this.groups == null) {
                this.groups = new HashSet<>();
            }
            this.groups.add(group);
            if (group.getGroupLeader() != this) {
                group.setGroupLeader(this);
            }
        }
    }

    public void removeGroup(Group group) {
        if (group != null && this.groups != null) {
            this.groups.remove(group);
            if (group.getGroupLeader() == this) {
                group.setGroupLeader(null);
            }
        }
    }

    public void setGroups(Set<Group> groups) {
        if (this.groups != null) {
            for (Group grp : new HashSet<>(this.groups)) {
                this.removeGroup(grp);
            }
        }
        if (groups != null) {
            for (Group grp : groups) {
                this.addGroup(grp);
            }
        }
    }
}
