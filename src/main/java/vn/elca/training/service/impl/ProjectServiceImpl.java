package vn.elca.training.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.request.SearchProjectCriteria;
import vn.elca.training.model.dto.response.ProjectListResponse;
import vn.elca.training.repository.ProjectRepository;
import vn.elca.training.service.ProjectService;


@AllArgsConstructor
@Service
public class ProjectServiceImpl implements ProjectService {
    private ProjectRepository projectRepository;
    private ModelMapper modelMapper;

    @Transactional(readOnly = true)
    @Override
    public Page<ProjectListResponse> searchProjects(SearchProjectCriteria criteria, Pageable pageable) {
        return projectRepository.searchProjects(criteria, pageable)
                .map(p -> modelMapper.map(p, ProjectListResponse.class));
    }
}
