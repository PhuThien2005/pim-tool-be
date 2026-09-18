package vn.elca.training.repository.custom;

import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.Task;
import vn.elca.training.model.entity.QProject;
import vn.elca.training.model.entity.QTask;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Repository;

/**
 * @author gtn
 *
 */
@Repository
public class TaskRepositoryImpl implements TaskRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Project> findProjectsByTaskName(String taskName) {
        return new JPAQuery<Project>(em)
                .from(QProject.project)
                .leftJoin(QProject.project.tasks, QTask.task).fetchJoin()
                .where(QProject.project.id.in(
                        JPAExpressions.select(QTask.task.project.id)
                                .from(QTask.task)
                                .where(QTask.task.name.eq(taskName))
                ))
                .distinct()
                .fetch();
    }

    @Override
    public List<Task> listRecentTasks(int limit) {
        return new JPAQuery<Task>(em)
                .from(QTask.task)
                .innerJoin(QTask.task.project, QProject.project).fetchJoin()
                .orderBy(QTask.task.id.desc())
                .limit(limit)
                .fetch();
    }
}
