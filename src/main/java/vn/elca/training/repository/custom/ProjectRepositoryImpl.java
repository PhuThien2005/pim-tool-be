package vn.elca.training.repository.custom;

import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        applySorting(dataQuery, p, g, pageable);

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

    private void applySorting(JPAQuery<Project> dataQuery, QProject p, QGroup g, Pageable pageable) {
        if (pageable != null && pageable.getSort().isSorted()) {
            boolean hasProjectNumberSort = false;
            for (Sort.Order order : pageable.getSort()) {
                Order direction = order.isAscending() ? Order.ASC : Order.DESC;
                String property = order.getProperty();
                if ("projectNumber".equalsIgnoreCase(property) || "number".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.projectNumber));
                    hasProjectNumberSort = true;
                } else if ("name".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.name));
                } else if ("customer".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.customer));
                } else if ("status".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.status));
                } else if ("startDate".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.startDate));
                } else if ("endDate".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.endDate));
                } else if ("groupId".equalsIgnoreCase(property) || "group".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, g.id));
                } else if ("id".equalsIgnoreCase(property)) {
                    dataQuery.orderBy(new OrderSpecifier<>(direction, p.id));
                }
            }
            if (!hasProjectNumberSort) {
                dataQuery.orderBy(p.projectNumber.asc());
            }
        } else {
            dataQuery.orderBy(p.projectNumber.asc());
        }
    }
}
