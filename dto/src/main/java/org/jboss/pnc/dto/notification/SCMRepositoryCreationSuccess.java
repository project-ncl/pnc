/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.dto.notification;


import lombok.Data;

import org.jboss.pnc.dto.SCMRepository;
import org.jboss.pnc.enums.JobNotificationProgress;
import org.jboss.pnc.enums.JobNotificationType;

import static org.jboss.pnc.enums.JobNotificationProgress.FINISHED;
import static org.jboss.pnc.enums.JobNotificationType.BUILD_CONFIG_CREATION;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Notification about created SCM Repository.
 *   
 * <pre>
 * Job: {@link JobNotificationType#BUILD_CONFIG_CREATION}
 * Notification type: {@code SCMR_CREATION_SUCCESS}
 * Progress:{@link JobNotificationProgress#FINISHED}
 * Message: no
 * <pre>
 * For notification about failure see {@link RepositoryCreationFailure}.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
public class SCMRepositoryCreationSuccess extends Notification {

    private static final String BC_CREATION_SUCCESS = "SCMR_CREATION_SUCCESS";

    /**
     * The created SCM Repository.
     */
    private final SCMRepository scmRepository;

    @JsonCreator
    public SCMRepositoryCreationSuccess(@JsonProperty("scmRepository") SCMRepository scmRepository) {
        super(BUILD_CONFIG_CREATION, BC_CREATION_SUCCESS, FINISHED);
        this.scmRepository = scmRepository;
    }
}
