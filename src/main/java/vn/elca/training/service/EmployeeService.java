package vn.elca.training.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import vn.elca.training.model.dto.response.EmployeeListResponse;

public interface EmployeeService {
    public Slice<EmployeeListResponse> searchEmployee(String keyword, Pageable pageable);
}
