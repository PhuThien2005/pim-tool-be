package vn.elca.training.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import vn.elca.training.model.dto.response.ErrorResponse;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.CommonErrorCode;

import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, Locale locale) {
        CommonErrorCode code = ex.getErrorCode();
        String message = getLocalizedMessage(code.getMessageKey(), ex.getArgs(), code.name(), locale);
        return buildError(code.getHttpStatus(), code.name(), message, null);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : ex.getConstraintViolations()) {
            errors.put(violation.getPropertyPath().toString(), violation.getMessage());
        }
        return buildError(HttpStatus.BAD_REQUEST, CommonErrorCode.VALIDATION_ERROR.name(), "Constraint violation", errors);
    }

    @ExceptionHandler({OptimisticLockException.class, ObjectOptimisticLockingFailureException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(Exception ex, Locale locale) {
        String message = getLocalizedMessage(CommonErrorCode.OPTIMISTIC_LOCK_ERROR.getMessageKey(), null, "Data has been modified by another user.", locale);
        return buildError(HttpStatus.CONFLICT, CommonErrorCode.OPTIMISTIC_LOCK_ERROR.name(), message, null);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex, Locale locale) {
        if (ex.getMessage() != null && (ex.getMessage().contains("PROJECT_NUMBER") || ex.getMessage().contains("23505"))) {
            return handleBusinessException(new BusinessException(CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS), locale);
        }
        return buildError(HttpStatus.BAD_REQUEST, CommonErrorCode.DATA_INTEGRITY_VIOLATION.name(), "Database constraint violation", null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception ex, Locale locale) {
        log.error("Unexpected error: ", ex);
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR, CommonErrorCode.INTERNAL_SERVER_ERROR.name(), "Unexpected error occurred.", null);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return createValidationError(errors);
    }

    @Override
    protected ResponseEntity<Object> handleBindException(org.springframework.validation.BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return createValidationError(errors);
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(org.springframework.beans.TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return createValidationError(null);
    }

    private ResponseEntity<Object> createValidationError(Map<String, String> errors) {
        ErrorResponse body = ErrorResponse.builder()
                .status(HttpStatus.BAD_REQUEST.value())
                .errorCode(CommonErrorCode.VALIDATION_ERROR.name())
                .message("Validation failed")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        log.warn("Client error {}: {}", status, ex.getMessage());
        ErrorResponse err = ErrorResponse.builder()
                .status(status.value())
                .errorCode(status.name())
                .message(ex.getMessage())
                .timestamp(LocalDateTime.now())
                .build();
        return ResponseEntity.status(status).body(err);
    }

    private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String code, String message, Map<String, String> errors) {
        ErrorResponse res = ErrorResponse.builder().status(status.value()).errorCode(code).message(message).errors(errors).timestamp(LocalDateTime.now()).build();
        return ResponseEntity.status(status).body(res);
    }

    private String getLocalizedMessage(String key, Object[] args, String defaultMessage, Locale locale) {
        try {
            return messageSource.getMessage(key, args, defaultMessage, locale != null ? locale : LocaleContextHolder.getLocale());
        } catch (Exception e) {
            return defaultMessage;
        }
    }
}
