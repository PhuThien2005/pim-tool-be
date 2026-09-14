package vn.elca.training.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author gtn
 *
 */
@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column
    private String fullName;

    @Column
    private String role;

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Task> tasks;

    @OneToMany(mappedBy = "groupLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Group> leadingGroups = new HashSet<>();

    @OneToMany(mappedBy = "projectLeader", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Project> leadingProjects = new HashSet<>();

    @ManyToMany(mappedBy = "members", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<Project> projects = new HashSet<>();

    public User() {}

    public User(String username) {
        this.username = username;
    }

    public User(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public User(String username, String fullName, String role) {
        this.username = username;
        this.fullName = fullName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }

    public Set<Group> getLeadingGroups() {
        return leadingGroups;
    }

    public void setLeadingGroups(Set<Group> leadingGroups) {
        this.leadingGroups = leadingGroups;
    }

    public Set<Project> getLeadingProjects() {
        return leadingProjects;
    }

    public void setLeadingProjects(Set<Project> leadingProjects) {
        this.leadingProjects = leadingProjects;
    }

    public Set<Project> getProjects() {
        return projects;
    }

    public void setProjects(Set<Project> projects) {
        this.projects = projects;
    }

    @Access(AccessType.PROPERTY)
    @Column(nullable = true)
    public Long getUsernameLength() {
        return getUsername() != null ? (long) getUsername().length() : 0L;
    }

    public void setUsernameLength(Long length) {

    }
}
