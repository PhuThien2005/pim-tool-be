package vn.elca.training.model;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import vn.elca.training.model.dto.request.CreateProjectRequest;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.request.UpdateProjectRequest;
import vn.elca.training.model.dto.response.*;
import vn.elca.training.model.entity.*;
import vn.elca.training.model.exception.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

public class ModelAndExceptionTest {

    @Test
    public void testCreateProjectRequest_BuilderAndGetters() {
        Set<String> visas = new HashSet<>(Arrays.asList("DTH", "MDU"));
        CreateProjectRequest request = CreateProjectRequest.builder()
                .projectNumber(1001)
                .name("Project A")
                .customer("Customer A")
                .groupId(1L)
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .visas(visas)
                .build();

        Assert.assertEquals(Integer.valueOf(1001), request.getProjectNumber());
        Assert.assertEquals("Project A", request.getName());
        Assert.assertEquals("Customer A", request.getCustomer());
        Assert.assertEquals(Long.valueOf(1L), request.getGroupId());
        Assert.assertEquals(ProjectStatus.NEW, request.getStatus());
        Assert.assertEquals(LocalDate.of(2025, 1, 1), request.getStartDate());
        Assert.assertEquals(LocalDate.of(2025, 12, 31), request.getEndDate());
        Assert.assertEquals(2, request.getVisas().size());

        CreateProjectRequest noArg = new CreateProjectRequest();
        noArg.setProjectNumber(2002);
        Assert.assertEquals(Integer.valueOf(2002), noArg.getProjectNumber());
    }

    @Test
    public void testUpdateProjectRequest_BuilderAndGetters() {
        UpdateProjectRequest request = UpdateProjectRequest.builder()
                .version(2L)
                .projectNumber(1002)
                .name("Project Updated")
                .customer("Customer B")
                .groupId(2L)
                .status(ProjectStatus.INP)
                .startDate(LocalDate.of(2025, 2, 1))
                .endDate(LocalDate.of(2025, 11, 30))
                .visas(new HashSet<>(Collections.singletonList("ABC")))
                .build();

        Assert.assertEquals(Long.valueOf(2L), request.getVersion());
        Assert.assertEquals(Integer.valueOf(1002), request.getProjectNumber());
        Assert.assertEquals("Project Updated", request.getName());
        Assert.assertEquals("Customer B", request.getCustomer());
        Assert.assertEquals(Long.valueOf(2L), request.getGroupId());
        Assert.assertEquals(ProjectStatus.INP, request.getStatus());
        Assert.assertEquals(LocalDate.of(2025, 2, 1), request.getStartDate());
        Assert.assertEquals(LocalDate.of(2025, 11, 30), request.getEndDate());
        Assert.assertEquals(1, request.getVisas().size());

        UpdateProjectRequest noArg = new UpdateProjectRequest();
        noArg.setVersion(3L);
        Assert.assertEquals(Long.valueOf(3L), noArg.getVersion());
    }

    @Test
    public void testSearchProjectCriteria_AllDateBranches() {
        Set<String> visas = new HashSet<>(Arrays.asList("DTH", " ", null));
        SearchProjectCriteria criteria = SearchProjectCriteria.builder()
                .keyword("   ")
                .status(ProjectStatus.PLA)
                .leaderVisa("MDU")
                .memberVisas(visas)
                .startDateFrom(LocalDate.of(2025, 1, 1))
                .startDateTo(LocalDate.of(2025, 6, 30))
                .endDateFrom(LocalDate.of(2025, 7, 1))
                .endDateTo(LocalDate.of(2025, 12, 31))
                .build();

        Assert.assertEquals(ProjectStatus.PLA, criteria.getStatus());
        Assert.assertEquals("MDU", criteria.getLeaderVisa());
        Assert.assertNotNull(criteria.toPredicate());
    }

    @Test
    public void testResponseDTOs_BuildersAndGetters() {
        EmployeeListResponse emp = EmployeeListResponse.builder()
                .id(1L)
                .visa("ABC")
                .firstName("A")
                .lastName("BC")
                .build();
        Assert.assertEquals(Long.valueOf(1L), emp.getId());
        Assert.assertEquals("ABC", emp.getVisa());

        GroupListResponse grp = GroupListResponse.builder()
                .id(2L)
                .groupLeader(emp)
                .build();
        Assert.assertEquals(Long.valueOf(2L), grp.getId());
        Assert.assertEquals("ABC", grp.getGroupLeader().getVisa());

        ProjectListResponse projList = ProjectListResponse.builder()
                .id(3L)
                .projectNumber(1003)
                .name("P3")
                .customer("C3")
                .status(ProjectStatus.FIN)
                .startDate(LocalDate.now())
                .build();
        Assert.assertEquals(Integer.valueOf(1003), projList.getProjectNumber());

        ProjectDetailResponse projDetail = ProjectDetailResponse.builder()
                .id(4L)
                .version(1L)
                .projectNumber(1004)
                .name("P4")
                .customer("C4")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(30))
                .group(grp)
                .employees(Collections.singleton(emp))
                .build();
        Assert.assertEquals(Integer.valueOf(1004), projDetail.getProjectNumber());
        Assert.assertEquals(1, projDetail.getEmployees().size());

        Map<String, String> errors = new HashMap<>();
        errors.put("field", "error");
        ErrorResponse err = ErrorResponse.builder()
                .status(400)
                .errorCode("ERR")
                .message("Message")
                .errors(errors)
                .timestamp(LocalDateTime.now())
                .build();
        Assert.assertEquals(400, err.getStatus());
        Assert.assertEquals("ERR", err.getErrorCode());
        Assert.assertEquals("Message", err.getMessage());
        Assert.assertEquals("error", err.getErrors().get("field"));
    }

    @Test
    public void testEntityClasses_GettersSetters() {
        Employee emp = new Employee();
        emp.setId(10L);
        emp.setVersion(0L);
        emp.setVisa("XYZ");
        emp.setFirstName("First");
        emp.setLastName("Last");
        emp.setBirthDate(LocalDate.of(1995, 5, 20));

        Assert.assertEquals(Long.valueOf(10L), emp.getId());
        Assert.assertEquals(Long.valueOf(0L), emp.getVersion());
        Assert.assertEquals("XYZ", emp.getVisa());
        Assert.assertEquals("First", emp.getFirstName());
        Assert.assertEquals("Last", emp.getLastName());
        Assert.assertEquals(LocalDate.of(1995, 5, 20), emp.getBirthDate());

        Group group = new Group();
        group.setId(20L);
        group.setVersion(1L);
        group.setGroupLeader(emp);
        Assert.assertEquals(Long.valueOf(20L), group.getId());
        Assert.assertEquals(emp, group.getGroupLeader());

        Project project = new Project();
        project.setId(30L);
        project.setVersion(2L);
        project.setProjectNumber(3001);
        project.setName("Project Test");
        project.setCustomer("Customer Test");
        project.setStatus(ProjectStatus.NEW);
        project.setStartDate(LocalDate.of(2025, 1, 1));
        project.setEndDate(LocalDate.of(2025, 12, 31));
        project.setGroup(group);
        project.setEmployees(new HashSet<>(Collections.singletonList(emp)));

        Assert.assertEquals(Integer.valueOf(3001), project.getProjectNumber());
        Assert.assertEquals("Project Test", project.getName());
        Assert.assertEquals(group, project.getGroup());
        Assert.assertEquals(1, project.getEmployees().size());

        // Enum coverage
        Assert.assertEquals(4, ProjectStatus.values().length);
        Assert.assertEquals(ProjectStatus.NEW, ProjectStatus.valueOf("NEW"));
    }

    @Test
    public void testExceptions_ConstructorsAndGetters() {
        // BusinessException
        BusinessException be1 = new BusinessException(CommonErrorCode.PROJECT_NOT_FOUND, new Object[]{100L});
        Assert.assertEquals(CommonErrorCode.PROJECT_NOT_FOUND, be1.getErrorCode());
        Assert.assertArrayEquals(new Object[]{100L}, be1.getArgs());

        BusinessException beDefault = new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR);
        Assert.assertEquals(CommonErrorCode.INTERNAL_SERVER_ERROR, beDefault.getErrorCode());

        BusinessException beMsg = new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Custom message");
        Assert.assertEquals("Custom message", beMsg.getMessage());

        BusinessException beMsgArgs = new BusinessException(CommonErrorCode.INTERNAL_SERVER_ERROR, "Msg with args", new Object[]{"arg1"});
        Assert.assertEquals("Msg with args", beMsgArgs.getMessage());

        Map<String, String> errMap = Collections.singletonMap("k", "v");
        BusinessException be2 = new BusinessException(CommonErrorCode.VALIDATION_ERROR, "Validation error", new Object[]{"arg"}, errMap);
        Assert.assertEquals(errMap, be2.getErrors());
        Assert.assertEquals(HttpStatus.BAD_REQUEST, be2.getStatus());

        // CommonErrorCode
        CommonErrorCode code = CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS;
        Assert.assertEquals(HttpStatus.BAD_REQUEST, code.getHttpStatus());
        Assert.assertEquals("error.project.number.exist", code.getMessageKey());

        // GroupNotFoundException
        GroupNotFoundException gnfe = new GroupNotFoundException(99L);
        Assert.assertEquals(CommonErrorCode.GROUP_NOT_FOUND, gnfe.getErrorCode());

        // ProjectNotFoundException
        ProjectNotFoundException pnfe1 = new ProjectNotFoundException(88L);
        Assert.assertEquals(CommonErrorCode.PROJECT_NOT_FOUND, pnfe1.getErrorCode());

        ProjectNotFoundException pnfe2 = new ProjectNotFoundException(Arrays.asList(1L, 2L));
        Assert.assertEquals(CommonErrorCode.PROJECT_NOT_FOUND, pnfe2.getErrorCode());
        Assert.assertTrue(pnfe2.getErrors().containsKey("notFoundProjectIds"));

        // InvalidProjectStatusException
        InvalidProjectStatusException ipse1 = new InvalidProjectStatusException("Status invalid");
        Assert.assertEquals(CommonErrorCode.INVALID_PROJECT_STATUS, ipse1.getErrorCode());

        InvalidProjectStatusException ipse2 = new InvalidProjectStatusException("Status invalid", Arrays.asList(3L, 4L));
        Assert.assertEquals(CommonErrorCode.INVALID_PROJECT_STATUS, ipse2.getErrorCode());
        Assert.assertTrue(ipse2.getErrors().containsKey("invalidProjectIds"));

        // ProjectNumberAlreadyExistsException
        ProjectNumberAlreadyExistsException pnaee = new ProjectNumberAlreadyExistsException(1234);
        Assert.assertEquals(CommonErrorCode.PROJECT_NUMBER_ALREADY_EXISTS, pnaee.getErrorCode());

        // VisaNotFoundException
        VisaNotFoundException vnfe = new VisaNotFoundException(Arrays.asList("V1", "V2"));
        Assert.assertEquals(CommonErrorCode.VISA_NOT_FOUND, vnfe.getErrorCode());
        Assert.assertEquals(Arrays.asList("V1", "V2"), vnfe.getNotFoundVisas());

        VisaNotFoundException vnfeMsg = new VisaNotFoundException("Visa msg");
        Assert.assertEquals("Visa msg", vnfeMsg.getMessage());

        VisaNotFoundException vnfeMsgList = new VisaNotFoundException("Visa msg", Collections.singletonList("V1"));
        Assert.assertEquals(1, vnfeMsgList.getNotFoundVisas().size());

        // Other exception constructors
        ProjectNotFoundException pnfeMsg = new ProjectNotFoundException("Msg");
        Assert.assertEquals("Msg", pnfeMsg.getMessage());

        ProjectNotFoundException pnfeIdMsg = new ProjectNotFoundException(99L, "Msg with id");
        Assert.assertEquals(Long.valueOf(99L), pnfeIdMsg.getProjectId());

        GroupNotFoundException gnfeMsg = new GroupNotFoundException("Group msg");
        Assert.assertEquals("Group msg", gnfeMsg.getMessage());

        GroupNotFoundException gnfeIdMsg = new GroupNotFoundException(10L, "Group msg with id");
        Assert.assertEquals(Long.valueOf(10L), gnfeIdMsg.getGroupId());

        ProjectNumberAlreadyExistsException pnaeeMsg = new ProjectNumberAlreadyExistsException("Already exists");
        Assert.assertEquals("Already exists", pnaeeMsg.getMessage());

        ProjectNumberAlreadyExistsException pnaeeNumMsg = new ProjectNumberAlreadyExistsException(1001, "Already exists 1001");
        Assert.assertEquals(Integer.valueOf(1001), pnaeeNumMsg.getProjectNumber());

        InvalidProjectStatusException ipseEmpty = new InvalidProjectStatusException("Invalid status", Collections.emptyList());
        Assert.assertNull(ipseEmpty.getErrors());
    }

    @Test
    public void testEntityEqualsAndHashCodeAndToString() {
        Employee emp1 = new Employee();
        emp1.setId(1L);
        emp1.setVisa("ABC");
        Assert.assertEquals(emp1, emp1);
        Assert.assertNotNull(emp1.toString());

        Group grp1 = new Group();
        grp1.setId(1L);
        Assert.assertEquals(grp1, grp1);
        Assert.assertNotNull(grp1.toString());

        Project p1 = Project.builder().projectNumber(1001).name("P1").build();
        p1.setId(1L);
        Assert.assertEquals(p1, p1);
        Assert.assertNotNull(p1.toString());
    }
}
