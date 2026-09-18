package vn.elca.training.model.entity;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "GROUP")
public class Group extends AbstractBaseEntity {
    @Setter(AccessLevel.NONE)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GROUP_LEADER_ID", nullable = false)
    private Employee groupLeader;

    public void setGroupLeader(Employee groupLeader) {
        if (this.groupLeader == groupLeader) {
            return;
        }
        if (this.groupLeader != null && this.groupLeader.getGroups() != null) {
            this.groupLeader.getGroups().remove(this);
        }
        this.groupLeader = groupLeader;
        if (groupLeader != null && groupLeader.getGroups() != null) {
            if (!groupLeader.getGroups().contains(this)) {
                groupLeader.getGroups().add(this);
            }
        }
    }

    public void removeGroupLeader() {
        this.setGroupLeader(null);
    }

    @Builder.Default
    @Setter(AccessLevel.NONE)
    @OneToMany(mappedBy = "group", fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

    public void addProject(Project project) {
        if (project != null) {
            if (this.projects == null) {
                this.projects = new HashSet<>();
            }
            this.projects.add(project);
            if (project.getGroup() != this) {
                project.setGroup(this);
            }
        }
    }

    public void removeProject(Project project) {
        if (project != null && this.projects != null) {
            this.projects.remove(project);
            if (project.getGroup() == this) {
                project.setGroup(null);
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
}
