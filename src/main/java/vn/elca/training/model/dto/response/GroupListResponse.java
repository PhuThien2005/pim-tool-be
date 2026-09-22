package vn.elca.training.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupListResponse {
    private Long id;
    private Long version;
    private EmployeeListResponse groupLeader;
}
