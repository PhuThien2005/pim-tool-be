package vn.elca.training.model.dto.response;

import lombok.*;
import vn.elca.training.model.entity.ProjectStatus;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectListResponse {
    private Long id;
    private Integer projectNumber;
    private String name;
    private ProjectStatus status;
    private String customer;
    private LocalDate startDate;
}
