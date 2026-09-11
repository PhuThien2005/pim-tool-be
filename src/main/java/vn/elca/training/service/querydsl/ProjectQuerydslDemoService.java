package vn.elca.training.service.querydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.entity.QTask;
import vn.elca.training.model.entity.QUser;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.util.ApplicationMapper;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Service hướng dẫn và minh họa các cách sử dụng QueryDSL từ cơ bản đến nâng cao.
 * <p>
 * Package này nhằm mục đích:
 * 1. Giúp hiểu rõ cơ chế QueryDSL hoạt động với Spring Data JPA.
 * 2. Minh họa cách xây dựng Dynamic Query (truy vấn động) với BooleanBuilder.
 * 3. So sánh giải pháp QueryDSL với cách lọc thủ công bằng Java Stream trong code cũ.
 * 4. Cung cấp các kĩ thuật nâng cao: JOIN nhiều bảng, DTO Projection, Aggregation (Group By/Count).
 */
@Service
@Transactional(readOnly = true)
public class ProjectQuerydslDemoService {

    private final ProjectRepository projectRepository;
    private final ApplicationMapper mapper;

    @PersistenceContext
    private EntityManager em;

    public ProjectQuerydslDemoService(ProjectRepository projectRepository, ApplicationMapper mapper) {
        this.projectRepository = projectRepository;
        this.mapper = mapper;
    }

    // =========================================================================
    // 1. CƠ BẢN NHẤT: QuerydslPredicateExecutor với điều kiện tĩnh
    // =========================================================================
    /**
     * Tìm kiếm Project theo tên chính xác.
     * <p>
     * - `QProject.project.name.eq(name)` là một Predicate Type-safe (kiểm tra kiểu dữ liệu ngay lúc compile).
     * - `projectRepository` kế thừa `QuerydslPredicateExecutor<Project>`, nên có sẵn hàm `findAll(Predicate)`.
     */
    public List<Project> findProjectsByNameBasic(String name) {
        QProject qProject = QProject.project;

        // Tương đương SQL: SELECT * FROM project WHERE name = ?
        Iterable<Project> results = projectRepository.findAll(qProject.name.eq(name));

        List<Project> list = new ArrayList<>();
        results.forEach(list::add);
        return list;
    }

    // =========================================================================
    // 2. DYNAMIC QUERY VỚI BOOLEANBUILDER: Giải quyết bài toán lọc nhiều tiêu chí
    // =========================================================================
    /**
     * Tìm kiếm động linh hoạt dựa trên ProjectSearchCriteria:
     * - Người dùng nhập tiêu chí nào thì hệ thống MỚI THÊM điều kiện WHERE cho tiêu chí đó.
     * - Nếu không nhập tiêu chí nào -> BooleanBuilder rỗng -> SELECT toàn bộ không có WHERE.
     * - Tránh hoàn toàn việc nối chuỗi String JPQL dễ sinh lỗi cú pháp hay SQL Injection.
     */
    public List<Project> searchProjectsDynamic(ProjectSearchCriteria criteria) {
        QProject qProject = QProject.project;
        BooleanBuilder builder = new BooleanBuilder();

        if (criteria != null) {
            // 1. Nếu có keyword: tìm tương đối (LIKE %keyword%) không phân biệt hoa thường
            // Tìm trong cả 'name' HOẶC 'customer'
            if (StringUtils.isNotBlank(criteria.getKeyword())) {
                builder.and(
                        qProject.name.containsIgnoreCase(criteria.getKeyword())
                                .or(qProject.customer.containsIgnoreCase(criteria.getKeyword()))
                );
            }

            // 2. Nếu có tên khách hàng cụ thể: lọc chính xác hoặc tương đối
            if (StringUtils.isNotBlank(criteria.getCustomer())) {
                builder.and(qProject.customer.equalsIgnoreCase(criteria.getCustomer()));
            }

            // 3. Nếu có ngày kết thúc từ ngày: finishingDate >= finishingDateFrom (goe: Greater than or Equal)
            if (criteria.getFinishingDateFrom() != null) {
                builder.and(qProject.finishingDate.goe(criteria.getFinishingDateFrom()));
            }

            // 4. Nếu có ngày kết thúc đến ngày: finishingDate <= finishingDateTo (loe: Less than or Equal)
            if (criteria.getFinishingDateTo() != null) {
                builder.and(qProject.finishingDate.loe(criteria.getFinishingDateTo()));
            }
        }

        // Thực thi query trực tiếp trên Database!
        Iterable<Project> iterable = projectRepository.findAll(builder);
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList());
    }

    // =========================================================================
    // 3. GIẢI PHÁP TỐI ƯU THAY THẾ CHO PROJECTSERVICE CŨ
    // =========================================================================
    /**
     * So sánh:
     * [CODE CŨ trong ProjectServiceImpl]:
     * projectRepository.findAll().stream()
     *         .filter(p -> p.getName().contains(keyword))
     *         ...
     * => HẠI: Kéo TOÀN BỘ database lên RAM server rồi mới lọc bằng Java -> Chết RAM (OOM) nếu DB lớn.
     *
     * [CODE MỚI với QueryDSL]:
     * => LỢI: Đẩy câu lệnh WHERE xuống tận H2/PostgreSQL/Oracle xử lý, server chỉ nhận về đúng dữ liệu cần tìm.
     */
    public List<ProjectDto> searchByKeywordOptimized(String keyword) {
        QProject qProject = QProject.project;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(keyword)) {
            builder.and(qProject.name.containsIgnoreCase(keyword)
                    .or(qProject.customer.containsIgnoreCase(keyword)));
        }

        return StreamSupport.stream(projectRepository.findAll(builder).spliterator(), false)
                .map(mapper::projectToProjectDto)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 4. NÂNG CAO: JPAQuery với JOIN nhiều bảng và quan hệ phức tạp
    // =========================================================================
    /**
     * Khi câu truy vấn cần JOIN sang các bảng liên kết (Task, User),
     * QuerydslPredicateExecutor trên một Repository không đủ linh hoạt.
     * Ta sử dụng `JPAQuery` (hoặc `JPAQueryFactory`) kết hợp `EntityManager`:
     * - JOIN bảng Project với Task và User.
     * - Lọc động: Tìm Project có chứa Task theo tên hoặc phân công cho User cụ thể.
     */
    public List<Project> searchProjectsWithTasks(ProjectSearchCriteria criteria) {
        QProject qProject = QProject.project;
        QTask qTask = QTask.task;
        QUser qUser = QUser.user;

        // Khởi tạo JPAQuery từ EntityManager
        JPAQuery<Project> query = new JPAQuery<Project>(em)
                .from(qProject)
                .distinct(); // Tránh trùng lặp do kết quả 1-N Join

        BooleanBuilder whereClause = new BooleanBuilder();

        // Join sang Task nếu có điều kiện liên quan đến Task hoặc User
        boolean needJoinTask = criteria != null &&
                (StringUtils.isNotBlank(criteria.getTaskName()) || StringUtils.isNotBlank(criteria.getAssigneeUsername()));

        if (needJoinTask) {
            query.innerJoin(qProject.tasks, qTask);

            if (StringUtils.isNotBlank(criteria.getTaskName())) {
                whereClause.and(qTask.name.containsIgnoreCase(criteria.getTaskName()));
            }

            if (StringUtils.isNotBlank(criteria.getAssigneeUsername())) {
                query.innerJoin(qTask.user, qUser);
                whereClause.and(qUser.username.eq(criteria.getAssigneeUsername()));
            }
        }

        // Thêm các điều kiện cơ bản của Project
        if (criteria != null) {
            if (StringUtils.isNotBlank(criteria.getKeyword())) {
                whereClause.and(qProject.name.containsIgnoreCase(criteria.getKeyword())
                        .or(qProject.customer.containsIgnoreCase(criteria.getKeyword())));
            }
            if (criteria.getFinishingDateFrom() != null) {
                whereClause.and(qProject.finishingDate.goe(criteria.getFinishingDateFrom()));
            }
        }

        return query.where(whereClause)
                .orderBy(qProject.id.desc())
                .fetch();
    }

    // =========================================================================
    // 5. PROJECTION (SELECT TRỰC TIẾP RA DTO - TỐI ƯU HIỆU NĂNG)
    // =========================================================================
    /**
     * Thay vì `SELECT *` toàn bộ Entity Project đưa vào JPA L1 Cache rồi map qua DTO,
     * QueryDSL cho phép chỉ SELECT đúng các cột id, name, customer, finishingDate
     * và tự động map thẳng vào constructor của ProjectDto.
     * <p>
     * Rất hữu ích cho các trang Danh sách/Bảng hiển thị nhiều nghìn dòng: Giảm 50-80% dung lượng RAM và I/O.
     */
    public List<ProjectDto> findProjectDtosProjected(String keyword) {
        QProject qProject = QProject.project;
        BooleanBuilder builder = new BooleanBuilder();

        if (StringUtils.isNotBlank(keyword)) {
            builder.and(qProject.name.containsIgnoreCase(keyword));
        }

        return new JPAQuery<ProjectDto>(em)
                .select(Projections.constructor(
                        ProjectDto.class,
                        qProject.id,
                        qProject.name,
                        qProject.customer,
                        qProject.finishingDate
                ))
                .from(qProject)
                .where(builder)
                .orderBy(qProject.name.asc())
                .fetch();
    }

    // =========================================================================
    // 6. TÍNH TOÁN VÀ GOM NHÓM (AGGREGATION / GROUP BY)
    // =========================================================================
    /**
     * Đếm số lượng task thuộc về mỗi dự án:
     * SQL: SELECT p.name, count(t.id) FROM project p LEFT JOIN task t ON p.id = t.project_id GROUP BY p.name
     */
    public Map<String, Long> countTasksPerProject() {
        QProject qProject = QProject.project;
        QTask qTask = QTask.task;

        List<Tuple> rows = new JPAQuery<Tuple>(em)
                .select(qProject.name, qTask.id.count())
                .from(qProject)
                .leftJoin(qProject.tasks, qTask)
                .groupBy(qProject.name)
                .fetch();

        Map<String, Long> summary = new HashMap<>();
        for (Tuple row : rows) {
            String projectName = row.get(qProject.name);
            Long taskCount = row.get(qTask.id.count());
            summary.put(projectName, taskCount != null ? taskCount : 0L);
        }
        return summary;
    }
}
