package vn.elca.training.dao;

import com.querydsl.jpa.impl.JPAQuery;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.ApplicationWebConfig;
import vn.elca.training.model.entity.Group;
import vn.elca.training.model.entity.QGroup;
import vn.elca.training.model.entity.Employee;
import vn.elca.training.repository.UserRepository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

/**
 * JUnit test để xác minh GroupRepository trong package vn.elca.training.dao hoạt động chính xác.
 */
@ContextConfiguration(classes = {ApplicationWebConfig.class})
@RunWith(SpringRunner.class)
@Transactional
public class GroupRepositoryTest {

    @PersistenceContext
    private EntityManager em;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSaveAndFindGroup() {

        Employee leader = new Employee("QMV_LEADER", "QMV Leader", "Group Leader");
        leader = userRepository.save(leader);


        Group group = new Group("Group QMV", leader);
        Group savedGroup = groupRepository.save(group);

        Assert.assertNotNull("ID của Group phải được sinh tự động", savedGroup.getId());


        Optional<Group> foundGroupOpt = groupRepository.findById(savedGroup.getId());
        Assert.assertTrue("Phải tìm thấy Group vừa lưu", foundGroupOpt.isPresent());

        Group foundGroup = foundGroupOpt.get();
        Assert.assertEquals("Tên nhóm phải khớp", "Group QMV", foundGroup.getName());
        Assert.assertNotNull("Group leader không được null", foundGroup.getGroupLeader());
        Assert.assertEquals("Username của Group Leader phải khớp", "QMV_LEADER", foundGroup.getGroupLeader().getUsername());
    }

    @Test
    public void testQueryGroupWithQueryDSL() {
        Employee leader = new Employee("HNH_LEADER", "HNH Leader", "Group Leader");
        leader = userRepository.save(leader);

        Group group = new Group("Group HNH", leader);
        groupRepository.save(group);


        Group queryResult = new JPAQuery<Group>(em)
                .from(QGroup.group)
                .where(QGroup.group.name.eq("Group HNH")
                        .and(QGroup.group.groupLeader.username.eq("HNH_LEADER")))
                .fetchFirst();

        Assert.assertNotNull("Phải tìm thấy group bằng QueryDSL", queryResult);
        Assert.assertEquals("Group HNH", queryResult.getName());
    }

    @Test
    public void testDeleteGroup() {
        Group group = new Group("Group To Delete");
        Group savedGroup = groupRepository.save(group);
        Long groupId = savedGroup.getId();
        Assert.assertNotNull(groupId);


        groupRepository.delete(savedGroup);
        em.flush();


        Optional<Group> deletedOpt = groupRepository.findById(groupId);
        Assert.assertFalse("Group phải không còn tồn tại sau khi xóa", deletedOpt.isPresent());
    }
}
