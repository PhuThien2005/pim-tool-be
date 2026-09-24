package vn.elca.training.model.exception;

import lombok.Getter;

@Getter
public class ProjectNumberAlreadyExistsException extends BusinessException {

    private final Integer projectNumber;

    public ProjectNumberAlreadyExistsException(String message) {
        super(CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS, message);
        this.projectNumber = null;
    }

    public ProjectNumberAlreadyExistsException(Integer projectNumber) {
        super(CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS,
                "The project number already existed: " + projectNumber,
                new Object[]{projectNumber});
        this.projectNumber = projectNumber;
    }

    public ProjectNumberAlreadyExistsException(Integer projectNumber, String message) {
        super(CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS, message, new Object[]{projectNumber});
        this.projectNumber = projectNumber;
    }
}
