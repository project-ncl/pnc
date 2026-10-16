/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import org.jboss.pnc.dto.SCMRepository;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;

/**
 * The result of Create&Sync call for creating SCM repository config. If the SCM repository config can be created
 * immediately, it is returned by {@link #getRepository()} property. If the repository needs to be synchronized first,
 * {@link #getTaskId()} property provides id of the synchronization task.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = RepositoryCreationResponse.Builder.class)
public class RepositoryCreationResponse {

    /**
     * Id of the task that will create and sync the repository. When the repository doesn't require sync, this is null
     * and {@link #getRepository()} is returned instead.
     */
    private Long taskId;

    /**
     * The created SCM Repistory config. When the repository require sync, this is null and {@link #getTaskId()} is
     * returned instead.
     */
    private SCMRepository repository;

    public RepositoryCreationResponse(long taskId) {
        this.taskId = taskId;
    }

    public RepositoryCreationResponse(SCMRepository repository) {
        this.repository = repository;
    }

    @lombok.Builder(builderClassName = "Builder")
    private RepositoryCreationResponse(Long taskId, SCMRepository repository) {
        this.taskId = taskId;
        this.repository = repository;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
