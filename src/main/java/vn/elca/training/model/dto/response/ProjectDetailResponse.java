package vn.elca.training.model.dto.response;

import lombok.*;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectDetailResponse {
    private Long id;
    private Long version;
    private GroupDetailResponse groupResponse;
    private Integer projectNumber;
    private String name;
    private String customer;
    private ProjectStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<String> visas;

    public ProjectStatus getProjectStatus() {
        return status;
    }

    public void setProjectStatus(ProjectStatus projectStatus) {
        this.status = projectStatus;
    }
}
