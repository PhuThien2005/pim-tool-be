package vn.elca.training.model.exception;

import lombok.Getter;

@Getter
public class ProjectNotFoundException extends RuntimeException {

    private final Long projectId;

    public ProjectNotFoundException(String message) {
        super(message);
        this.projectId = null;
    }

    public ProjectNotFoundException(Long projectId) {
        super("Project not found with id: " + projectId);
        this.projectId = projectId;
    }

    public ProjectNotFoundException(Long projectId, String message) {
        super(message);
        this.projectId = projectId;
    }
}
