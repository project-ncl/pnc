/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Credentials needed to connect to the builder pod.
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@AllArgsConstructor
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = SSHCredentials.Builder.class)
public class SSHCredentials {

    /**
     * Command to run to connect to the builder pod.
     */
    private final String command;

    /**
     * The ssh password needed to connect to the builder pod.
     */
    private final String password;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}