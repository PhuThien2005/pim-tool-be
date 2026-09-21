package vn.elca.training.model.dto.request;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.validator.StartBeforeEndDate;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@StartBeforeEndDate(
        startDateField = "startDateFrom",
        endDateField = "startDateTo",
        allowEqual = true,
        message = "{project.search.startDate.range}"
)
@StartBeforeEndDate(
        startDateField = "endDateFrom",
        endDateField = "endDateTo",
        allowEqual = true,
        message = "{project.search.endDate.range}"
)
public class SearchProjectCriteria {

    @Size(max = 100, message = "{project.search.keyword.size}")
    private String keyword;

    private ProjectStatus status;

    @Size(max = 50, message = "{project.search.leader.size}")
    private String projectLeader;

    @Size(max = 50, message = "{project.search.member.size}")
    private String member;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateTo;
}
