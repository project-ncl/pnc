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
package org.jboss.pnc.dto.tasks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.enums.ResultStatus;

@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = RepositoryCreationResult.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryCreationResult {

    protected final ResultStatus status;
    protected final boolean repoCreatedSuccessfully; // did first step completed successfully;
    protected final String internalScmUrl;
    protected final String externalUrl;
    protected final boolean preBuildSyncEnabled;
    protected final Integer taskId;
    protected final JobNotificationType jobType;
    protected final BuildConfiguration buildConfiguration;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
