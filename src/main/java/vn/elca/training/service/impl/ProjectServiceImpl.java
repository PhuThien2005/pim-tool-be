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
import vn.elca.training.model.exception.ApplicationUnexpectedException;
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
     * Quy tắc:
     * - Tên dự án mới = <tên dự án cũ> + " Maint. " + <năm hiện tại>
     * - Dự án cũ sẽ chuyển thành không kích hoạt (activated = false)
     * - Cả hai hành động phải diễn ra trong cùng 1 transaction atomic.
     * - Nếu xảy ra lỗi (exception), toàn bộ transaction phải rollback (dự án mới không được tạo, dự án cũ không bị update).
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createMaintenanceProject(Long oldProjectId) throws Exception {
        return createMaintenanceProjectInternal(oldProjectId, false);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Project createMaintenanceProjectWithException(Long oldProjectId, boolean simulateError) throws Exception {
        return createMaintenanceProjectInternal(oldProjectId, simulateError);
    }

    private Project createMaintenanceProjectInternal(Long oldProjectId, boolean simulateError) throws Exception {
        Project oldProject = projectRepository.findById(oldProjectId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án với ID: " + oldProjectId));

        // 1. Chuyển trạng thái dự án cũ sang không kích hoạt (activated = false)
        oldProject.setActivated(false);
        projectRepository.save(oldProject);

        // 2. Tạo tên cho dự án bảo trì: <tên cũ> + " Maint. " + <năm hiện tại>
        int currentYear = LocalDate.now().getYear();
        String maintenanceProjectName = String.format("%s Maint. %d", oldProject.getName(), currentYear);

        // 3. Khởi tạo đối tượng bảo trì mới
        Project maintenanceProject = new Project();
        maintenanceProject.setName(maintenanceProjectName);
        maintenanceProject.setFinishingDate(LocalDate.now().plusYears(1));
        maintenanceProject.setCustomer(oldProject.getCustomer());
        maintenanceProject.setGroup(oldProject.getGroup());
        maintenanceProject.setStatus(ProjectStatus.NEW);
        maintenanceProject.setActivated(true);

        // 4. Nếu có yêu cầu giả lập lỗi hoặc có lỗi trong tiến trình: throw exception để kích hoạt Rollback
        if (simulateError) {
            throw new ApplicationUnexpectedException("Lỗi giả lập trong quá trình tạo dự án bảo trì để kiểm thử Transactional Rollback");
        }

        // 5. Lưu dự án mới vào cơ sở dữ liệu
        return projectRepository.save(maintenanceProject);
    }
}
