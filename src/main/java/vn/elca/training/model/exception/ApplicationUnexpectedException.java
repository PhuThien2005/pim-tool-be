package vn.elca.training.model.exception;

public class ApplicationUnexpectedException extends RuntimeException {
    public ApplicationUnexpectedException(String message) {
        super(message);
    }

    public ApplicationUnexpectedException(Throwable e) {
        super("Unexpected exception", e);
    }

    public ApplicationUnexpectedException(String message, Throwable cause) {
        super(message, cause);
    }
}
