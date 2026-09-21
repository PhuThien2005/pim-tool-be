package vn.elca.training.model.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class InvalidProjectStatusException extends RuntimeException {

    private final List<Long> invalidProjectIds;

    public InvalidProjectStatusException(String message) {
        super(message);
        this.invalidProjectIds = null;
    }

    public InvalidProjectStatusException(String message, List<Long> invalidProjectIds) {
        super(message);
        this.invalidProjectIds = invalidProjectIds;
    }
}
