package vn.elca.training.repository.custom;

import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.entity.Project;
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
        JPAQuery<Project> query = new JPAQuery<Project>(em)
                .from(p)
                .leftJoin(p.group, QGroup.group).fetchJoin()
                .where(criteria != null ? criteria.toPredicate() : null)
                .orderBy(p.projectNumber.asc())
                .distinct();
        long total = (pageable != null && pageable.isPaged()) ? query.fetchCount() : 0;
        if (pageable != null && pageable.isPaged()) {
            query.offset(pageable.getOffset()).limit(pageable.getPageSize());
        }
        List<Project> content = query.fetch();
        return new PageImpl<>(content, pageable != null ? pageable : Pageable.unpaged(), total > 0 ? total : content.size());
    }
}
