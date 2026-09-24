package vn.elca.training.model.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class ProjectNotFoundException extends BusinessException {

    private final Long projectId;
    private final List<Long> notFoundProjectIds;

    public ProjectNotFoundException(String message) {
        super(CommonErrorCode.PROJECT_NOT_FOUND, message);
        this.projectId = null;
        this.notFoundProjectIds = null;
    }

    public ProjectNotFoundException(Long projectId) {
        super(CommonErrorCode.PROJECT_NOT_FOUND, "Project not found with id: " + projectId, new Object[]{projectId});
        this.projectId = projectId;
        this.notFoundProjectIds = null;
    }

    public ProjectNotFoundException(List<Long> notFoundProjectIds) {
        super(CommonErrorCode.PROJECT_NOT_FOUND,
                "Projects not found with ids: " + notFoundProjectIds,
                notFoundProjectIds != null && !notFoundProjectIds.isEmpty() ? new Object[]{notFoundProjectIds.get(0)} : null,
                notFoundProjectIds != null && !notFoundProjectIds.isEmpty()
                        ? Collections.singletonMap("notFoundProjectIds", notFoundProjectIds.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                        : null);
        this.projectId = notFoundProjectIds != null && !notFoundProjectIds.isEmpty() ? notFoundProjectIds.get(0) : null;
        this.notFoundProjectIds = notFoundProjectIds;
    }

    public ProjectNotFoundException(Long projectId, String message) {
        super(CommonErrorCode.PROJECT_NOT_FOUND, message, new Object[]{projectId});
        this.projectId = projectId;
        this.notFoundProjectIds = null;
    }
}
