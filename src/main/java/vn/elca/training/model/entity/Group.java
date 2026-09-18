package vn.elca.training.model.entity;

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
 * Entity Group đại diện cho nhóm dự án (Group) trong tổ chức.
 * Được ánh xạ vào bảng PROJECT_GROUP để tránh từ khóa 'GROUP' trong SQL.
 */
@Entity
@Table(name = "GROUPS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_leader_id")
    private Employee groupLeader;

    @Builder.Default
    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Project> projects = new HashSet<>();

    public Group(String name) {
        this.name = name;
    }

    public Group(String name, Employee groupLeader) {
        this.name = name;
        this.groupLeader = groupLeader;
    }
}
