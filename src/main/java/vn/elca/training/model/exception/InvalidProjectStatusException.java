package vn.elca.training.model.exception;

import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class InvalidProjectStatusException extends BusinessException {

    private final List<Long> invalidProjectIds;

    public InvalidProjectStatusException(String message) {
        super(CommonErrorCode.INVALID_PROJECT_STATUS, message);
        this.invalidProjectIds = null;
    }

    public InvalidProjectStatusException(String message, List<Long> invalidProjectIds) {
        super(CommonErrorCode.INVALID_PROJECT_STATUS,
                message,
                null,
                invalidProjectIds != null && !invalidProjectIds.isEmpty()
                        ? Collections.singletonMap("invalidProjectIds", invalidProjectIds.stream().map(String::valueOf).collect(Collectors.joining(", ")))
                        : null);
        this.invalidProjectIds = invalidProjectIds;
    }
}
