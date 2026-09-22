package vn.elca.training.controller;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.elca.training.model.dto.response.GroupListResponse;
import vn.elca.training.service.GroupService;

@AllArgsConstructor
@RestController
@RequestMapping("/groups")
public class GroupController {

    private GroupService groupService;

    @GetMapping
    public ResponseEntity<Slice<GroupListResponse>> getAllGroups(
            @PageableDefault(size = 20, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(groupService.getAll(pageable));
    }
}
