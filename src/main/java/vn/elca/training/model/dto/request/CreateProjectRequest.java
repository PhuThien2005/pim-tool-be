package vn.elca.training.model.dto.request;

import vn.elca.training.model.entity.ProjectStatus;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Set;

public class CreateProjectRequest {
    @NotNull
    private Long groupId;
    @NotNull
    private Long projectNumber;
    @NotNull
    private String name;
    @NotNull
    private String customer;
    @NotNull
    private ProjectStatus projectStatus;
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    @NotNull
    private Set<String> visas;
}
