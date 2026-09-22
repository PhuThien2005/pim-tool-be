package vn.elca.training.service.impl;

import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.elca.training.model.dto.response.GroupListResponse;
import vn.elca.training.repository.GroupRepository;
import vn.elca.training.service.GroupService;

@Transactional
@Service
@AllArgsConstructor
public class GroupServiceImpl implements GroupService {

    private GroupRepository groupRepository;
    private ModelMapper modelMapper;

    @Override
    @Transactional(readOnly = true)
    public Slice<GroupListResponse> getAll(Pageable pageable) {
        return groupRepository.findAllBy(pageable)
                .map(g -> modelMapper.map(g, GroupListResponse.class));
    }
}
