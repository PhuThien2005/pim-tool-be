package vn.elca.training.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum CommonErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "error.mandatory.fields"),
    PROJECT_NUMBER_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "error.project.number.exist"),
    VISA_NOT_FOUND(HttpStatus.BAD_REQUEST, "error.visa.not.exist"),
    INVALID_PROJECT_STATUS(HttpStatus.BAD_REQUEST, "error.invalid.project.status"),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, "error.unexpected"),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "error.project.not.found"),
    GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "error.group.not.found"),
    OPTIMISTIC_LOCK_ERROR(HttpStatus.CONFLICT, "error.optimistic.lock"),
    MISSING_PARAMETER(HttpStatus.BAD_REQUEST, "error.unexpected"),
    MALFORMED_JSON_REQUEST(HttpStatus.BAD_REQUEST, "error.unexpected"),
    INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "error.unexpected"),
    ARGUMENT_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "error.unexpected"),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "error.unexpected"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.unexpected");

    private final HttpStatus httpStatus;
    private final String messageKey;

    CommonErrorCode(HttpStatus httpStatus, String messageKey) {
        this.httpStatus = httpStatus;
        this.messageKey = messageKey;
    }
}
