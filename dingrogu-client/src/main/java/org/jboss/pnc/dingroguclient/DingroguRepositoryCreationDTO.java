/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.dingroguclient;

import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.api.enums.JobNotificationType;
import org.jboss.pnc.dto.BuildConfiguration;

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
