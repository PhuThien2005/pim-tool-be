package vn.elca.training.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.validator.annotation.StartBeforeEndDate;
import vn.elca.training.validator.annotation.ValidVisas;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@StartBeforeEndDate
public class CreateProjectRequest {

    @NotNull
    @Min(1)
    @Max(9999)
    private Integer projectNumber;

    @NotBlank
    @Size(max = 50)
    private String name;

    @NotBlank
    @Size(max = 50)
    private String customer;

    @NotNull
    private Long groupId;

    private ProjectStatus status;

    @NotNull
    private LocalDate startDate;

    private LocalDate endDate;

    @ValidVisas
    @Builder.Default
    private Set<String> visas = new HashSet<>();
}
