package vn.elca.training.model.exception;

import lombok.Getter;

@Getter
public class GroupNotFoundException extends BusinessException {

    private final Long groupId;

    public GroupNotFoundException(String message) {
        super(CommonErrorCode.GROUP_NOT_FOUND, message);
        this.groupId = null;
    }

    public GroupNotFoundException(Long groupId) {
        super(CommonErrorCode.GROUP_NOT_FOUND, "Group not found with id: " + groupId, new Object[]{groupId});
        this.groupId = groupId;
    }

    public GroupNotFoundException(Long groupId, String message) {
        super(CommonErrorCode.GROUP_NOT_FOUND, message, new Object[]{groupId});
        this.groupId = groupId;
    }
}
