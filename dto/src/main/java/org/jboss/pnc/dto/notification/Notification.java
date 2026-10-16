/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.notification;

import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import lombok.Data;

/**
 * Notification about progress of asynchronous job.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public abstract class Notification {

    /**
     * Type of the asynchronous job.
     */
    private final JobNotificationType job;

    /**
     * Specific type of the notification.
     */
    private final String notificationType;

    /**
     * Progress of the asynchonous job.
     */
    private final JobNotificationProgress progress;

    /**
     * Progress of the asynchonous job.
     */
    private final JobNotificationProgress oldProgress;

    /**
     * Optional notification message.
     */
    private final String message;

    protected Notification(
            JobNotificationType job,
            String notificationType,
            JobNotificationProgress progress,
            JobNotificationProgress oldProgress,
            String message) {
        this.job = job;
        this.notificationType = notificationType;
        this.progress = progress;
        this.oldProgress = oldProgress;
        this.message = message;
    }

    protected Notification(
            JobNotificationType job,
            String notificationType,
            JobNotificationProgress progress,
            JobNotificationProgress oldProgress) {
        this.job = job;
        this.notificationType = notificationType;
        this.progress = progress;
        this.oldProgress = oldProgress;
        this.message = null;
    }
}
