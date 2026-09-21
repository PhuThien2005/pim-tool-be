package vn.elca.training.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDetailResponse {
    private Long id;
    private Long version;
    private GroupResponse groupResponse;
    private Long projectNumber;
    private String name;
    private String customer;
    private ProjectStatus projectStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private Set<String> visas;
}
