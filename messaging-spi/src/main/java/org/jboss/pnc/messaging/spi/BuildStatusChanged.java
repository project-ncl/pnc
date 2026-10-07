/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.messaging.spi;

import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.dto.Build;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@Builder(buildMethodName = "buildMe")
@JsonDeserialize(builder = BuildStatusChanged.BuildStatusChangedBuilder.class)
public class BuildStatusChanged implements Message {

    private final String attribute = "state-change";

    private final String oldStatus;

    private final Build build;

    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }

    @JsonPOJOBuilder(withPrefix = "", buildMethodName = "buildMe")
    public static final class BuildStatusChangedBuilder {
    }
}
