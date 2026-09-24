package vn.elca.training.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {

    private final CommonErrorCode errorCode;
    private final Object[] args;
    private final Map<String, String> errors;

    public BusinessException(CommonErrorCode errorCode) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = null;
        this.errors = null;
    }

    public BusinessException(CommonErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.args = null;
        this.errors = null;
    }

    public BusinessException(CommonErrorCode errorCode, Object[] args) {
        super(errorCode.name());
        this.errorCode = errorCode;
        this.args = args;
        this.errors = null;
    }

    public BusinessException(CommonErrorCode errorCode, String message, Object[] args) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
        this.errors = null;
    }

    public BusinessException(CommonErrorCode errorCode, String message, Object[] args, Map<String, String> errors) {
        super(message);
        this.errorCode = errorCode;
        this.args = args;
        this.errors = errors;
    }

    public HttpStatus getStatus() {
        return errorCode.getHttpStatus();
    }

    public String getMessageKey() {
        return errorCode.getMessageKey();
    }
}
