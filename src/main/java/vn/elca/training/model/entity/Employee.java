package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
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
public class Employee extends AbstractBaseEntity {
    @ManyToMany(mappedBy = "employees", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Set<Project> projects = new HashSet<>();

    public void addProject(Project project) {
        projects.add(project);
        project.setEmployees(this);
    }

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String fullName;

    @Column
    private String role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<Task> tasks = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Group> leadingGroups = new HashSet<>();

    @Builder.Default
    @OneToMany(mappedBy = "projectLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Project> leadingProjects = new HashSet<>();

    public Employee(String username) {
        this.username = username;
    }

    public Employee(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public Employee(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    @Access(AccessType.PROPERTY)
    @Column(nullable = true)
    public Long getUsernameLength() {
        return getUsername() != null ? (long) getUsername().length() : 0L;
    }

    public void setUsernameLength(Long length) {

    }
}
