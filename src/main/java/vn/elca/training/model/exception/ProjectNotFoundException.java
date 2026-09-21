package vn.elca.training.model.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class ProjectNotFoundException extends RuntimeException {

    private final Long projectId;
    private final List<Long> notFoundProjectIds;

    public ProjectNotFoundException(String message) {
        super(message);
        this.projectId = null;
        this.notFoundProjectIds = null;
    }

    public ProjectNotFoundException(Long projectId) {
        super("Project not found with id: " + projectId);
        this.projectId = projectId;
        this.notFoundProjectIds = null;
    }

    public ProjectNotFoundException(List<Long> notFoundProjectIds) {
        super("Projects not found with ids: " + notFoundProjectIds);
        this.projectId = notFoundProjectIds != null && !notFoundProjectIds.isEmpty() ? notFoundProjectIds.get(0) : null;
        this.notFoundProjectIds = notFoundProjectIds;
    }

    public ProjectNotFoundException(Long projectId, String message) {
        super(message);
        this.projectId = projectId;
        this.notFoundProjectIds = null;
    }
}
