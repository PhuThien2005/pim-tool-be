package vn.elca.training.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import vn.elca.training.model.dto.request.CreateProjectRequest;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.request.UpdateProjectRequest;
import vn.elca.training.model.dto.response.ProjectDetailResponse;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.exception.InvalidProjectStatusException;
import vn.elca.training.model.exception.ProjectNotFoundException;
import vn.elca.training.service.ProjectService;

import java.time.LocalDate;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @Test
    public void testSearchProjects_Success() throws Exception {
        ProjectListResponse response = ProjectListResponse.builder()
                .id(1L)
                .projectNumber(1001)
                .name("EFV")
                .customer("Customer A")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Page<ProjectListResponse> page = new PageImpl<>(Collections.singletonList(response), PageRequest.of(0, 10), 1);
        Mockito.when(projectService.searchProjects(Mockito.any(SearchProjectCriteria.class), Mockito.any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/projects")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].projectNumber").value(1001))
                .andExpect(jsonPath("$.content[0].name").value("EFV"));
    }

    @Test
    public void testSearchProjects_WithKeyword() throws Exception {
        Page<ProjectListResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(projectService.searchProjects(Mockito.any(SearchProjectCriteria.class), Mockito.any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/projects")
                        .param("keyword", "EFV")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    public void testSearchProjects_InvalidLeaderVisa_Returns400() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("leaderVisa", "INVALID_VISA")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.leaderVisa").exists());
    }


    @Test
    public void testSearchProjects_InvalidStartDateRange_Returns400() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("startDateFrom", "2025-12-31")
                        .param("startDateTo", "2025-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.startDateTo").value("Start date 'To' must be on or after 'From'"));
    }

    @Test
    public void testSearchProjects_InvalidEndDateRange_Returns400() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("endDateFrom", "2025-12-31")
                        .param("endDateTo", "2025-01-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.endDateTo").value("End date 'To' must be on or after 'From'"));
    }

    @Test
    public void testSearchProjects_InvalidEndDateRange_FrenchLocale() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("endDateFrom", "2025-12-31")
                        .param("endDateTo", "2025-01-01")
                        .header("Accept-Language", "fr")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.endDateTo").value("La date de fin 'À' doit être postérieure ou égale à la date 'De'"));
    }

    @Test
    public void testSearchProjects_WithStatus_Returns200() throws Exception {
        Page<ProjectListResponse> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);
        Mockito.when(projectService.searchProjects(Mockito.any(SearchProjectCriteria.class), Mockito.any(Pageable.class)))
                .thenReturn(emptyPage);

        mockMvc.perform(get("/projects")
                        .param("keyword", "ee")
                        .param("status", "NEW")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void testSearchProjects_WithInvalidStatus_Returns400() throws Exception {
        mockMvc.perform(get("/projects")
                        .param("status", "INVALID_STATUS")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    public void testDeleteProject_Success_Returns204() throws Exception {
        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(projectService, Mockito.times(1)).deleteProject(1L);
    }

    @Test
    public void testDeleteProjects_Success_Returns204() throws Exception {
        mockMvc.perform(delete("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1, 2]"))
                .andExpect(status().isNoContent());

        Mockito.verify(projectService, Mockito.times(1)).deleteProjects(java.util.Arrays.asList(1L, 2L));
    }

    @Test
    public void testDeleteProjects_NotFound_Returns404_WithNotFoundIds() throws Exception {
        Mockito.doThrow(new ProjectNotFoundException(java.util.Arrays.asList(998L, 999L)))
                .when(projectService).deleteProjects(java.util.Arrays.asList(998L, 999L));

        mockMvc.perform(delete("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[998, 999]"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PROJECT_NOT_FOUND"))
                .andExpect(jsonPath("$.errors.notFoundProjectIds").value("998, 999"));
    }

    @Test
    public void testDeleteProjects_InvalidStatus_Returns400_WithInvalidIds() throws Exception {
        Mockito.doThrow(new InvalidProjectStatusException("Only projects with status NEW can be deleted", java.util.Arrays.asList(3L, 4L)))
                .when(projectService).deleteProjects(java.util.Arrays.asList(3L, 4L));

        mockMvc.perform(delete("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[3, 4]"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PROJECT_STATUS"))
                .andExpect(jsonPath("$.errors.invalidProjectIds").value("3, 4"));
    }

    @Test
    public void testDeleteProject_NotFound_Returns404() throws Exception {
        Mockito.doThrow(new ProjectNotFoundException(999L))
                .when(projectService).deleteProject(999L);

        mockMvc.perform(delete("/projects/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PROJECT_NOT_FOUND"));
    }

    @Test
    public void testDeleteProject_InvalidStatus_Returns400() throws Exception {
        Mockito.doThrow(new InvalidProjectStatusException("Only projects with status NEW can be deleted"))
                .when(projectService).deleteProject(1L);

        mockMvc.perform(delete("/projects/1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("INVALID_PROJECT_STATUS"));
    }

    @Test
    public void testGetProject_Success_Returns200() throws Exception {
        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(1L)
                .projectNumber(1001)
                .name("EFV")
                .customer("Customer A")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 10))
                .endDate(LocalDate.of(2025, 12, 31))
                .build();

        Mockito.when(projectService.getProject(1L)).thenReturn(detail);

        mockMvc.perform(get("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectNumber").value(1001))
                .andExpect(jsonPath("$.name").value("EFV"));
    }

    @Test
    public void testGetProject_NotFound_Returns404() throws Exception {
        Mockito.when(projectService.getProject(999L)).thenThrow(new ProjectNotFoundException(999L));

        mockMvc.perform(get("/projects/999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("PROJECT_NOT_FOUND"));
    }

    @Test
    public void testCreateProject_Success_Returns201() throws Exception {
        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(10L)
                .projectNumber(2001)
                .name("New Project")
                .customer("New Customer")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Mockito.when(projectService.createProject(Mockito.any(CreateProjectRequest.class))).thenReturn(detail);

        String json = "{\"projectNumber\":2001,\"name\":\"New Project\",\"customer\":\"New Customer\",\"groupId\":1,\"status\":\"NEW\",\"startDate\":\"2025-01-01\"}";

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectNumber").value(2001))
                .andExpect(jsonPath("$.name").value("New Project"));
    }

    @Test
    public void testCreateProject_ValidationError_Returns400() throws Exception {
        // Missing required fields
        String invalidJson = "{\"name\":\"\"}";

        mockMvc.perform(post("/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    public void testUpdateProject_Success_Returns200() throws Exception {
        ProjectDetailResponse detail = ProjectDetailResponse.builder()
                .id(1L)
                .projectNumber(1001)
                .name("Updated Project")
                .customer("Updated Customer")
                .status(ProjectStatus.INP)
                .startDate(LocalDate.of(2025, 1, 1))
                .build();

        Mockito.when(projectService.updateProject(Mockito.eq(1L), Mockito.any(UpdateProjectRequest.class))).thenReturn(detail);

        String json = "{\"version\":1,\"name\":\"Updated Project\",\"customer\":\"Updated Customer\",\"groupId\":1,\"status\":\"INP\",\"startDate\":\"2025-01-01\"}";

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Project"));
    }

    @Test
    public void testUpdateProject_ValidationError_Returns400() throws Exception {
        // Invalid json (missing version and groupId)
        String invalidJson = "{\"name\":\"\"}";

        mockMvc.perform(put("/projects/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }
}

