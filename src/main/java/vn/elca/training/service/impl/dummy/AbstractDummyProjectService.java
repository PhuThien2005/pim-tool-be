package vn.elca.training.service.impl.dummy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import vn.elca.training.model.dto.ProjectDto;
import vn.elca.training.model.entity.Project;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author gtn
 *
 */
public abstract class AbstractDummyProjectService {
    private Log logger = LogFactory.getLog(getClass());

    @Value("${spring.profiles.active:dummy}")
    private String activeProfiles;

    protected static final Map<Long, ProjectDto> DUMMY_PROJECTS = new ConcurrentHashMap<>();

    static {
        DUMMY_PROJECTS.put(1L, new ProjectDto(1L, "EFV", "ELCA", LocalDate.of(2020, 4, 20)));
        DUMMY_PROJECTS.put(2L, new ProjectDto(2L, "CXTRANET", "KSTA", LocalDate.of(2020, 4, 25)));
        DUMMY_PROJECTS.put(3L, new ProjectDto(3L, "CRYSTAL BALL", "SECUTIX", LocalDate.of(2020, 4, 28)));
        DUMMY_PROJECTS.put(4L, new ProjectDto(4L, "IOC CLIENT EXTRANET", "IOC", LocalDate.of(2020, 6, 7)));
        DUMMY_PROJECTS.put(5L, new ProjectDto(5L, "TRADEECO", "ECO", LocalDate.of(2020, 6, 8)));
    }

    protected void printCurrentActiveProfiles() {
        logger.debug("Currently active profile - " + activeProfiles);
    }

    public List<Project> findAll() {
        printCurrentActiveProfiles();
        return DUMMY_PROJECTS.values().stream()
                .map(dto -> {
                    Project p = new Project(dto.getId(), dto.getName(), dto.getFinishingDate());
                    p.setCustomer(dto.getCustomer());
                    return p;
                })
                .collect(Collectors.toList());
    }

    public List<ProjectDto> searchByKeyword(String keyword) {
        printCurrentActiveProfiles();
        return DUMMY_PROJECTS.values().stream()
                .filter(dto -> dto.getName() != null && dto.getName().contains(keyword))
                .collect(Collectors.toList());
    }

    public long count() {
        printCurrentActiveProfiles();
        return DUMMY_PROJECTS.size();
    }

    public ProjectDto findProjectById(Long id) {
        printCurrentActiveProfiles();
        ProjectDto dto = DUMMY_PROJECTS.get(id);
        if (dto != null) {
            return new ProjectDto(dto.getId(), dto.getName(), dto.getCustomer(), dto.getFinishingDate());
        }
        return null;
    }

    public ProjectDto updateProject(Long id, ProjectDto projectDto) {
        printCurrentActiveProfiles();
        ProjectDto existing = DUMMY_PROJECTS.get(id);
        if (existing == null) {
            existing = new ProjectDto();
            existing.setId(id);
        }
        if (projectDto.getName() != null) {
            existing.setName(projectDto.getName());
        }
        if (projectDto.getCustomer() != null) {
            existing.setCustomer(projectDto.getCustomer());
        }
        if (projectDto.getFinishingDate() != null) {
            existing.setFinishingDate(projectDto.getFinishingDate());
        }
        DUMMY_PROJECTS.put(id, existing);
        return new ProjectDto(existing.getId(), existing.getName(), existing.getCustomer(), existing.getFinishingDate());
    }

    public Project createMaintenanceProject(Long oldProjectId) {
        ProjectDto oldDto = DUMMY_PROJECTS.get(oldProjectId);
        if (oldDto == null) {
            throw new IllegalArgumentException("Project not found with id: " + oldProjectId);
        }
        int currentYear = LocalDate.now().getYear();
        String maintName = String.format("%s Maint. %d", oldDto.getName(), currentYear);
        long newId = System.currentTimeMillis();
        ProjectDto maintDto = new ProjectDto(newId, maintName, oldDto.getCustomer(), LocalDate.now().plusYears(1));
        DUMMY_PROJECTS.put(newId, maintDto);
        Project p = new Project(newId, maintName, LocalDate.now().plusYears(1));
        p.setCustomer(oldDto.getCustomer());
        return p;
    }
}
