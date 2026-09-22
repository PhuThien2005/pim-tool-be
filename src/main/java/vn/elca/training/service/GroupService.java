package vn.elca.training.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import vn.elca.training.model.dto.response.GroupListResponse;

public interface GroupService {
    Slice<GroupListResponse> getAll(Pageable pageable);
}
