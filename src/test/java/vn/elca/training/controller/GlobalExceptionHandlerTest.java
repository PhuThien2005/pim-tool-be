package vn.elca.training.controller;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import vn.elca.training.model.dto.response.ErrorResponse;
import vn.elca.training.model.exception.BusinessException;
import vn.elca.training.model.exception.CommonErrorCode;

import javax.persistence.OptimisticLockException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Path;
import java.util.Collections;
import java.util.Locale;

@RunWith(MockitoJUnitRunner.class)
public class GlobalExceptionHandlerTest {

    @Mock
    private MessageSource messageSource;

    @Mock
    private WebRequest webRequest;

    private GlobalExceptionHandler exceptionHandler;

    @Before
    public void setUp() {
        exceptionHandler = new GlobalExceptionHandler(messageSource);
    }

    @Test
    public void testHandleBusinessException() {
        BusinessException ex = new BusinessException(CommonErrorCode.PROJECT_NOT_FOUND, new Object[]{100L});
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.any(Locale.class)))
                .thenReturn("Project not found");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleBusinessException(ex, Locale.ENGLISH);

        Assert.assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("PROJECT_NOT_FOUND", response.getBody().getErrorCode());
        Assert.assertEquals("Project not found", response.getBody().getMessage());
    }

    @Test
    public void testHandleConstraintViolation() {
        ConstraintViolation<?> violation = Mockito.mock(ConstraintViolation.class);
        Path path = Mockito.mock(Path.class);
        Mockito.when(path.toString()).thenReturn("fieldName");
        Mockito.when(violation.getPropertyPath()).thenReturn(path);
        Mockito.when(violation.getMessage()).thenReturn("must not be empty");

        ConstraintViolationException ex = new ConstraintViolationException(Collections.singleton(violation));

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleConstraintViolation(ex);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("VALIDATION_ERROR", response.getBody().getErrorCode());
        Assert.assertTrue(response.getBody().getErrors().containsKey("fieldName"));
    }

    @Test
    public void testHandleOptimisticLock() {
        OptimisticLockException ex = new OptimisticLockException("Row was updated or deleted by another transaction");
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.any(Locale.class)))
                .thenReturn("Data has been modified by another user.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleOptimisticLock(ex, Locale.ENGLISH);

        Assert.assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("OPTIMISTIC_LOCK_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleDataIntegrityViolation_DuplicateProjectNumber() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Unique index or primary key violation: PROJECT_NUMBER 23505");
        Mockito.when(messageSource.getMessage(Mockito.anyString(), Mockito.any(), Mockito.anyString(), Mockito.any(Locale.class)))
                .thenReturn("Project number already exists.");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex, Locale.ENGLISH);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("PROJECT_NUMBER_ALREADY_EXISTS", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleDataIntegrityViolation_GenericConstraint() {
        DataIntegrityViolationException ex = new DataIntegrityViolationException("Foreign key violation FK_CUSTOMER");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleDataIntegrityViolation(ex, Locale.ENGLISH);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleUnexpectedError() {
        RuntimeException ex = new RuntimeException("Unexpected runtime error");

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleUnexpectedError(ex, Locale.ENGLISH);

        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        Assert.assertNotNull(response.getBody());
        Assert.assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getErrorCode());
    }

    @Test
    public void testHandleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = Mockito.mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "Name is required");

        Mockito.when(ex.getBindingResult()).thenReturn(bindingResult);
        Mockito.when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.assertEquals("VALIDATION_ERROR", errorResponse.getErrorCode());
        Assert.assertEquals("Name is required", errorResponse.getErrors().get("name"));
    }

    @Test
    public void testHandleTypeMismatch() {
        org.springframework.beans.TypeMismatchException ex = Mockito.mock(org.springframework.beans.TypeMismatchException.class);

        ResponseEntity<Object> response = exceptionHandler.handleTypeMismatch(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, webRequest);

        Assert.assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        Assert.assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.assertEquals("VALIDATION_ERROR", errorResponse.getErrorCode());
    }

    @Test
    public void testHandleExceptionInternal() {
        Exception ex = new Exception("Internal error message");

        ResponseEntity<Object> response = exceptionHandler.handleExceptionInternal(ex, null, new HttpHeaders(), HttpStatus.BAD_GATEWAY, webRequest);

        Assert.assertEquals(HttpStatus.BAD_GATEWAY, response.getStatusCode());
        Assert.assertTrue(response.getBody() instanceof ErrorResponse);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        Assert.assertEquals("BAD_GATEWAY", errorResponse.getErrorCode());
        Assert.assertEquals("Internal error message", errorResponse.getMessage());
    }
}
