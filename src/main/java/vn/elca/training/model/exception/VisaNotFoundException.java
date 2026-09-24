package vn.elca.training.model.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Getter
public class VisaNotFoundException extends BusinessException {

    private final List<String> notFoundVisas;

    public VisaNotFoundException(String message) {
        super(CommonErrorCode.VISA_NOT_FOUND, message);
        this.notFoundVisas = Collections.emptyList();
    }

    public VisaNotFoundException(List<String> notFoundVisas) {
        super(CommonErrorCode.VISA_NOT_FOUND,
                "The following visas do not exist: " + (notFoundVisas != null ? String.join(", ", notFoundVisas) : ""),
                new Object[]{notFoundVisas != null ? String.join(", ", notFoundVisas) : ""});
        this.notFoundVisas = notFoundVisas != null ? new ArrayList<>(notFoundVisas) : Collections.emptyList();
    }

    public VisaNotFoundException(String message, List<String> notFoundVisas) {
        super(CommonErrorCode.VISA_NOT_FOUND,
                message,
                new Object[]{notFoundVisas != null ? String.join(", ", notFoundVisas) : ""});
        this.notFoundVisas = notFoundVisas != null ? new ArrayList<>(notFoundVisas) : Collections.emptyList();
    }
}
