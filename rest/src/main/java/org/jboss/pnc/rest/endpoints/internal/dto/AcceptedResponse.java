/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints.internal.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@Builder
@AllArgsConstructor
@JsonDeserialize(builder = AcceptedResponse.AcceptedResponseBuilder.class)
public class AcceptedResponse {

    /**
     * Id of the started operation
     */
    private final String id;

    /**
     * Url to subscribe to operation updates.
     */
    private final String pushUpdatesUrl;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class AcceptedResponseBuilder {
    }

}
