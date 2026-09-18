/*
 * TaskAudit
 * 
 * Project: Training
 * 
 * Copyright 2015 by ELCA
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of ELCA. ("Confidential Information"). You
 * shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license
 * agreement you entered into with ELCA.
 */

package vn.elca.training.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * @author vlp
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotNull
    private AuditType auditType;

    @Column(nullable = false)
    @NotNull
    private Status status;

    @Column(nullable = false)
    @NotNull
    private String taskName;

    @Column(nullable = false)
    @NotNull
    private LocalDate taskDeadline;

    @Column
    private long projectId;

    @Column(length = 1000)
    private String message;

    public enum AuditType {
        INSERT, UPDATE, DELETE;
    }

    public enum Status {
        SUCCESS, FAILED;
    }

    public TaskAudit(Task task, AuditType auditType, Status status, String message) {
        setProjectId(task.getProject().getId());
        setTaskName(task.getName());
        setTaskDeadline(task.getDeadline());
        setAuditType(auditType);
        setStatus(status);
        setMessage(message);
    }
}
