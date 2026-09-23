package vn.elca.training.repository.custom;

import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.repository.support.PageableExecutionUtils;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.entity.Project;
import vn.elca.training.model.entity.QEmployee;
import vn.elca.training.model.entity.QGroup;
import vn.elca.training.model.entity.QProject;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

public class ProjectRepositoryImpl implements ProjectRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Project> searchProjects(SearchProjectCriteria criteria, Pageable pageable) {
        QProject p = QProject.project;
        QGroup g = QGroup.group;
        QEmployee gl = new QEmployee("groupLeader");
        JPAQuery<Project> dataQuery = new JPAQuery<Project>(em)
                .from(p)
                .leftJoin(p.group, g).fetchJoin()
                .leftJoin(g.groupLeader, gl).fetchJoin()
                .where(criteria != null ? criteria.toPredicate() : null)
                .distinct();
        Sort sort = (pageable != null) ? pageable.getSort() : Sort.unsorted();
        if (sort.getOrderFor("projectNumber") == null) {
            sort = sort.and(Sort.by(Sort.Direction.ASC, "projectNumber"));
        }
        new Querydsl(em, new PathBuilder<>(Project.class, "project")).applySorting(sort, dataQuery);

        if (pageable != null && pageable.isPaged()) {
            dataQuery.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<Project> content = dataQuery.fetch();
        JPAQuery<Long> countQuery = new JPAQuery<Long>(em)
                .select(p.id.count())
                .from(p)
                .where(criteria != null ? criteria.toPredicate() : null);
        return PageableExecutionUtils.getPage(
                content,
                pageable != null ? pageable : Pageable.unpaged(),
                countQuery::fetchOne
        );
    }
}
