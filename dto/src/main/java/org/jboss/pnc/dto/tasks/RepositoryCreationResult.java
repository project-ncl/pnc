/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.tasks;

import org.jboss.pnc.api.dto.ExceptionResolution;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.enums.JobNotificationType;
import org.jboss.pnc.enums.ResultStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

@Data
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = RepositoryCreationResult.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class RepositoryCreationResult {

    protected final ResultStatus status;
    protected final ExceptionResolution exceptionResolution;
    protected final boolean repoCreatedSuccessfully; // did first step completed successfully;
    protected final String internalScmUrl;
    protected final String externalUrl;
    protected final boolean preBuildSyncEnabled;
    protected final Long taskId;
    protected final JobNotificationType jobType;
    protected final BuildConfiguration buildConfiguration;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
