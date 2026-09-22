package vn.elca.training.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.elca.training.model.dto.response.GroupListResponse;

import java.util.List;

public interface GroupService {
    public Page<GroupListResponse> getAll(Pageable pageable);
}
