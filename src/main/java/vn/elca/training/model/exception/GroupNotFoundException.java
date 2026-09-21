package vn.elca.training.model.exception;

import lombok.Getter;

@Getter
public class GroupNotFoundException extends RuntimeException {

    private final Long groupId;

    public GroupNotFoundException(String message) {
        super(message);
        this.groupId = null;
    }

    public GroupNotFoundException(Long groupId) {
        super("Group not found with id: " + groupId);
        this.groupId = groupId;
    }

    public GroupNotFoundException(Long groupId, String message) {
        super(message);
        this.groupId = groupId;
    }
}
