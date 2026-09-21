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
public class ProjectSearchResponse {
    private Long id;
    private Long projectNumber;
    private String name;
    private ProjectStatus projectStatus;
    private String customer;
    private LocalDate startDate;
}
