package vn.elca.training.model.dto.request;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.format.annotation.DateTimeFormat;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.entity.QEmployee;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.validator.annotation.StartBeforeEndDate;
import vn.elca.training.validator.annotation.ValidVisa;

import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Set;

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

    @ValidVisa(message = "{employee.visa.invalid}")
    private String leaderVisa;

    private Set<@ValidVisa(message = "{employee.visa.invalid}") String> memberVisas;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDateTo;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDateTo;

    public Predicate toPredicate() {
        return toPredicate(QProject.project, null);
    }

    public Predicate toPredicate(QProject p, QEmployee leader) {
        if (p == null) {
            p = QProject.project;
        }
        String kw = StringUtils.isNotBlank(keyword) ? keyword.trim() : null;
        BooleanExpression keywordExp = (kw == null) ? null : p.name.containsIgnoreCase(kw)
                .or(p.customer.containsIgnoreCase(kw))
                .or(StringUtils.isNumeric(kw) && kw.length() <= 4 ? p.projectNumber.eq(Integer.parseInt(kw)) : null);
        BooleanBuilder membersExp = new BooleanBuilder();
        if (memberVisas != null && !memberVisas.isEmpty()) {
            for (String visa : memberVisas) {
                if (StringUtils.isNotBlank(visa)) {
                    membersExp.and(p.employees.any().visa.equalsIgnoreCase(visa.trim()));
                }
            }
        }

        BooleanExpression leaderExp = null;
        if (StringUtils.isNotBlank(leaderVisa)) {
            String trimmedVisa = leaderVisa.trim();
            if (leader != null) {
                leaderExp = leader.visa.equalsIgnoreCase(trimmedVisa);
            } else {
                leaderExp = p.group.groupLeader.visa.equalsIgnoreCase(trimmedVisa);
            }
        }

        return new BooleanBuilder()
                .and(keywordExp)
                .and(status != null ? p.status.eq(status) : null)
                .and(leaderExp)
                .and(membersExp)
                .and(startDateFrom != null ? p.startDate.goe(startDateFrom) : null)
                .and(startDateTo != null ? p.startDate.loe(startDateTo) : null)
                .and(endDateFrom != null ? p.endDate.goe(endDateFrom) : null)
                .and(endDateTo != null ? p.endDate.loe(endDateTo) : null);
    }
}
