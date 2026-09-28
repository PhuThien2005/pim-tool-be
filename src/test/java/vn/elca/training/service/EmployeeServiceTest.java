package vn.elca.training.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import vn.elca.training.model.dto.response.EmployeeListResponse;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.repository.EmployeeRepository;
import vn.elca.training.service.impl.EmployeeServiceImpl;

import java.util.Collections;

@RunWith(MockitoJUnitRunner.class)
public class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private Pageable pageable;
    private Employee employee;
    private EmployeeListResponse response;

    @Before
    public void setUp() {
        pageable = PageRequest.of(0, 10);

        employee = new Employee();
        employee.setId(1L);
        employee.setVisa("DTH");
        employee.setFirstName("Thien");
        employee.setLastName("Doan");

        response = EmployeeListResponse.builder()
                .id(1L)
                .visa("DTH")
                .firstName("Thien")
                .lastName("Doan")
                .build();
    }

    @Test
    public void testSearchEmployee_NullKeyword_ReturnsEmptySlice() {
        Slice<EmployeeListResponse> result = employeeService.searchEmployee(null, pageable);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getContent().isEmpty());
        Mockito.verifyNoInteractions(employeeRepository);
    }

    @Test
    public void testSearchEmployee_BlankKeyword_ReturnsEmptySlice() {
        Slice<EmployeeListResponse> result = employeeService.searchEmployee("   ", pageable);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.getContent().isEmpty());
        Mockito.verifyNoInteractions(employeeRepository);
    }

    @Test
    public void testSearchEmployee_ValidKeyword_ReturnsMappedSlice() {
        Slice<Employee> employeeSlice = new SliceImpl<>(Collections.singletonList(employee), pageable, false);

        Mockito.when(employeeRepository.findByVisaStartingWithIgnoreCaseOrFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase(
                "DTH", "DTH", "DTH", pageable)).thenReturn(employeeSlice);
        Mockito.when(modelMapper.map(employee, EmployeeListResponse.class)).thenReturn(response);

        Slice<EmployeeListResponse> result = employeeService.searchEmployee("  DTH  ", pageable);

        Assert.assertNotNull(result);
        Assert.assertEquals(1, result.getContent().size());
        Assert.assertEquals("DTH", result.getContent().get(0).getVisa());
        Assert.assertEquals("Thien", result.getContent().get(0).getFirstName());

        Mockito.verify(employeeRepository, Mockito.times(1))
                .findByVisaStartingWithIgnoreCaseOrFirstNameStartingWithIgnoreCaseOrLastNameStartingWithIgnoreCase("DTH", "DTH", "DTH", pageable);
        Mockito.verify(modelMapper, Mockito.times(1)).map(employee, EmployeeListResponse.class);
    }
}
