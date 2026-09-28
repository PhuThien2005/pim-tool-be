package vn.elca.training.model.entity;

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.PathInits;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;

public class QueryDslModelTest {

    @Test
    public void testQAbstractBaseEntity_Constructors() {
        QAbstractBaseEntity qBase1 = new QAbstractBaseEntity("base");
        Assert.assertNotNull(qBase1);
        Assert.assertEquals("base", qBase1.getMetadata().getName());

        QAbstractBaseEntity qBase2 = new QAbstractBaseEntity(QAbstractBaseEntity.abstractBaseEntity);
        Assert.assertNotNull(qBase2);

        QAbstractBaseEntity qBase3 = new QAbstractBaseEntity(PathMetadataFactory.forVariable("base3"));
        Assert.assertNotNull(qBase3);
    }

    @Test
    public void testQEmployee_Constructors() {
        QEmployee qEmp1 = new QEmployee("emp1");
        Assert.assertNotNull(qEmp1);
        Assert.assertNotNull(qEmp1.visa);

        QEmployee qEmp2 = new QEmployee(PathMetadataFactory.forVariable("emp2"));
        Assert.assertNotNull(qEmp2);

        QEmployee qEmp3 = new QEmployee(QEmployee.employee);
        Assert.assertNotNull(qEmp3);
    }

    @Test
    public void testQGroup_Constructors() {
        QGroup qGrp1 = new QGroup("grp1");
        Assert.assertNotNull(qGrp1);
        Assert.assertNotNull(qGrp1.groupLeader);

        QGroup qGrp2 = new QGroup(PathMetadataFactory.forVariable("grp2"));
        Assert.assertNotNull(qGrp2);

        QGroup qGrp3 = new QGroup(QGroup.group);
        Assert.assertNotNull(qGrp3);

        QGroup qGrp4 = new QGroup(PathMetadataFactory.forVariable("grp4"), PathInits.DIRECT2);
        Assert.assertNotNull(qGrp4);

        QGroup qGrp5 = new QGroup(Group.class, PathMetadataFactory.forVariable("grp5"), PathInits.DIRECT2);
        Assert.assertNotNull(qGrp5);
    }

    @Test
    public void testQProject_Constructors() {
        QProject qPrj1 = new QProject("prj1");
        Assert.assertNotNull(qPrj1);
        Assert.assertNotNull(qPrj1.projectNumber);

        QProject qPrj2 = new QProject(PathMetadataFactory.forVariable("prj2"));
        Assert.assertNotNull(qPrj2);

        QProject qPrj3 = new QProject(QProject.project);
        Assert.assertNotNull(qPrj3);

        QProject qPrj4 = new QProject(PathMetadataFactory.forVariable("prj4"), PathInits.DIRECT2);
        Assert.assertNotNull(qPrj4);

        QProject qPrj5 = new QProject(Project.class, PathMetadataFactory.forVariable("prj5"), PathInits.DIRECT2);
        Assert.assertNotNull(qPrj5);
    }

    @Test
    public void testAbstractBaseEntity_DirectSubclass() {
        class ConcreteEntity extends AbstractBaseEntity {}

        ConcreteEntity entity = new ConcreteEntity();
        entity.setId(123L);
        entity.setVersion(5L);

        Assert.assertEquals(Long.valueOf(123L), entity.getId());
        Assert.assertEquals(Long.valueOf(5L), entity.getVersion());
    }

    @Test
    public void testEntityBuilders_Complete() {
        Employee emp = Employee.builder()
                .visa("TEST")
                .firstName("Test")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .projects(new HashSet<>())
                .build();
        emp.setId(1L);
        emp.setVersion(0L);

        Assert.assertEquals("TEST", emp.getVisa());
        Assert.assertNotNull(emp.toString());

        Group grp = Group.builder()
                .groupLeader(emp)
                .projects(new HashSet<>())
                .build();
        grp.setId(10L);
        grp.setVersion(0L);

        Assert.assertEquals(emp, grp.getGroupLeader());
        Assert.assertNotNull(grp.toString());

        Project prj = Project.builder()
                .projectNumber(9999)
                .name("Full Project")
                .customer("Customer")
                .status(ProjectStatus.NEW)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusDays(10))
                .group(grp)
                .employees(new HashSet<>(Collections.singleton(emp)))
                .build();
        prj.setId(100L);
        prj.setVersion(0L);

        Assert.assertEquals(Integer.valueOf(9999), prj.getProjectNumber());
        Assert.assertNotNull(prj.toString());
    }
}
