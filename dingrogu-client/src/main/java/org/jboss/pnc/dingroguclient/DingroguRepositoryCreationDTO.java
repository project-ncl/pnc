/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dingroguclient;

import org.jboss.pnc.api.enums.JobNotificationType;
import org.jboss.pnc.dto.BuildConfiguration;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DingroguRepositoryCreationDTO {
    String orchUrl;
    String reqourUrl;

    String externalRepoUrl;
    String ref;
    boolean preBuildSyncEnabled;
    JobNotificationType jobNotificationType;
    BuildConfiguration buildConfiguration;

    // needed for notification, TODO: maybe switch to operation id in the future?
    String taskId;
}
