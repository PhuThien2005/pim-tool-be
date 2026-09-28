package vn.elca.training.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.model.dto.response.EmployeeListResponse;
import vn.elca.training.service.EmployeeService;

import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Test
    public void testSearchEmployee_DefaultParams_Returns200() throws Exception {
        EmployeeListResponse emp = EmployeeListResponse.builder()
                .id(1L)
                .visa("DTH")
                .firstName("Thien")
                .lastName("Doan")
                .build();
        Slice<EmployeeListResponse> slice = new SliceImpl<>(Collections.singletonList(emp));

        Mockito.when(employeeService.searchEmployee(Mockito.anyString(), Mockito.any(Pageable.class)))
                .thenReturn(slice);

        mockMvc.perform(get("/employees")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].visa").value("DTH"))
                .andExpect(jsonPath("$.content[0].firstName").value("Thien"));
    }

    @Test
    public void testSearchEmployee_WithKeywordAndPagination_Returns200() throws Exception {
        Slice<EmployeeListResponse> emptySlice = new SliceImpl<>(Collections.emptyList());

        Mockito.when(employeeService.searchEmployee(Mockito.eq("DTH"), Mockito.any(Pageable.class)))
                .thenReturn(emptySlice);

        mockMvc.perform(get("/employees")
                        .param("keyword", "DTH")
                        .param("page", "0")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
}
