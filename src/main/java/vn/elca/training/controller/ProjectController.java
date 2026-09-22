package vn.elca.training.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.elca.training.model.dto.request.CreateProjectRequest;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.request.UpdateProjectRequest;
import vn.elca.training.model.dto.response.ProjectDetailResponse;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.service.ProjectService;

import javax.validation.Valid;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private ProjectService projectService;

    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> getProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(projectService.getProject(projectId));
    }

    @PostMapping
    public ResponseEntity<ProjectDetailResponse> createProject(@Valid @RequestBody CreateProjectRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDetailResponse> updateProject(
            @PathVariable Long projectId,
            @Valid @RequestBody UpdateProjectRequest request) {
        return ResponseEntity.ok(projectService.updateProject(projectId, request));
    }

    @GetMapping
    public ResponseEntity<Page<ProjectListResponse>> searchProjects(
            @Valid SearchProjectCriteria criteria,
            @PageableDefault(sort = "projectNumber", direction = Sort.Direction.ASC) Pageable pageable) {
        Page<ProjectListResponse> result = projectService.searchProjects(criteria, pageable);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProjects(@RequestBody List<Long> projectIds) {
        projectService.deleteProjects(projectIds);
        return ResponseEntity.noContent().build();
    }
}
