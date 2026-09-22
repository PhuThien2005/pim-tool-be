package vn.elca.training.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.model.dto.response.EmployeeListResponse;
import vn.elca.training.service.EmployeeService;

@AllArgsConstructor
@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<Slice<EmployeeListResponse>> searchEmployee(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 10, sort = "visa", direction = Sort.Direction.ASC) Pageable pageable
    ) {
        return ResponseEntity.ok(employeeService.searchEmployee(keyword, pageable));
    }
}
