package vn.elca.training.model.exception;

import lombok.Getter;

@Getter
public class ProjectNumberAlreadyExistsException extends RuntimeException {

    private final Integer projectNumber;

    public ProjectNumberAlreadyExistsException(String message) {
        super(message);
        this.projectNumber = null;
    }

    public ProjectNumberAlreadyExistsException(Integer projectNumber) {
        super("The project number already existed: " + projectNumber);
        this.projectNumber = projectNumber;
    }

    public ProjectNumberAlreadyExistsException(Integer projectNumber, String message) {
        super(message);
        this.projectNumber = projectNumber;
    }
}
