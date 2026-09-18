/*
 * ITaskAuditService
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

package vn.elca.training.service;

/**
 * @author vlp
 *
 */
public interface AuditService {
    void saveAuditDataForTask(Task task, AuditType auditType, Status status, String message);
}
