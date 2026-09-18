package vn.elca.training.service.impl;

import com.querydsl.core.BooleanBuilder;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.ProjectStatus;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;
import vn.elca.training.util.ApplicationMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author vlp
 *
 */
@Service
@Profile("!dummy | dev")
public class ProjectServiceImpl implements ProjectService {

    private ProjectRepository projectRepository;
    private ApplicationMapper mapper;

    public ProjectServiceImpl(ProjectRepository projectRepository, ApplicationMapper mapper) {
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    @Override
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Override
    public List<ProjectDto> searchByKeyword(String keyword) {

//        Cách 1: Sử dụng Stream API để lọc danh sách các dự án có tên chứa từ khóa
//        return projectRepository.findAll().stream()
//                .filter(p-> p.getName() != null && p.getName().contains(keyword))
//                .map(mapper::projectToProjectDto)
//                .collect(Collectors.toList());

//        Cách 2: Sử dụng phương thức tìm kiếm có sẵn trong ProjectRepository
        return projectRepository.findByNameContainingIgnoreCase(keyword).stream()
                .map(mapper::projectToProjectDto)
                .collect(Collectors.toList());

//        Cách 3: Sử dụng QuerydslPredicateExecutor để tìm kiếm với điều kiện WHERE name LIKE %keyword% OR customer LIKE %keyword%
//        QProject qProject = QProject.project;
//        BooleanBuilder predicate = new BooleanBuilder();
//
//        if (StringUtils.isNotBlank(keyword)) {
//            // Tạo điều kiện WHERE name LIKE %keyword% OR customer LIKE %keyword%
//            predicate.and(qProject.name.containsIgnoreCase(keyword)
//                    .or(qProject.customer.containsIgnoreCase(keyword)));
//        }
//
//        // projectRepository.findAll(predicate) là hàm CÓ SẴN từ QuerydslPredicateExecutor!
//        Iterable<Project> projects = projectRepository.findAll(predicate);
//
//        return StreamSupport.stream(projects.spliterator(), false)
//                .map(mapper::projectToProjectDto)
//                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return projectRepository.count();
    }

    @Override
    public ProjectDto findProjectById(Long id) {
        Project project = projectRepository.findById(id).orElse(null);
        return project != null ? mapper.projectToProjectDto(project) : null;
    }

    @Override
    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        Project project = projectRepository.findById(id).orElse(null);
        if (project != null) {
            if (projectDto.getName() != null) {
                project.setName(projectDto.getName());
            }
            if (projectDto.getCustomer() != null) {
                project.setCustomer(projectDto.getCustomer());
            }
            if (projectDto.getFinishingDate() != null) {
                project.setFinishingDate(projectDto.getFinishingDate());
            }
            project = projectRepository.save(project);
            return mapper.projectToProjectDto(project);
        }
        return null;
    }

    /**
     * Tạo Maintenance Project từ Project có sẵn trong một transaction.
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createMaintenanceProject(Long oldProjectId) {
        Project oldProject = projectRepository.findById(oldProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án với ID: " + oldProjectId));


        oldProject.setActivated(false);
        projectRepository.save(oldProject);


        int currentYear = LocalDate.now().getYear();
        String maintenanceProjectName = String.format("%s Maint. %d", oldProject.getName(), currentYear);


        Project maintenanceProject = Project.builder()
                .name(maintenanceProjectName)
                .finishingDate(LocalDate.now().plusYears(1))
                .customer(oldProject.getCustomer())
                .group(oldProject.getGroup())
                .status(ProjectStatus.NEW)
                .activated(true)
                .build();
//        if (true)
//            throw new RuntimeException("Test rollback");

        return projectRepository.save(maintenanceProject);
    }
}
