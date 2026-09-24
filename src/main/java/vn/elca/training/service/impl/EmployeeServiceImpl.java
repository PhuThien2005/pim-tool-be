package vn.elca.training.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.response.EmployeeListResponse;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.service.EmployeeService;

import java.util.Collections;

@Transactional
@Service
@AllArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private EmployeeRepository employeeRepository;
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Slice<EmployeeListResponse> searchEmployee(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new SliceImpl<>(Collections.emptyList(), pageable, false);
        }
        String trimmed = keyword.trim();
        return employeeRepository.findByVisaStartingWithIgnoreCaseOrFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase(
                        trimmed, trimmed, trimmed, pageable)
                .map(e -> modelMapper.map(e, EmployeeListResponse.class));
    }
}
