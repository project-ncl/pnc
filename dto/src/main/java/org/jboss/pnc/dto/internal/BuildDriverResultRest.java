/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;

import org.jboss.pnc.enums.BuildStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class BuildDriverResultRest implements Serializable {

    private BuildStatus buildStatus;
    private String outputChecksum;

    public void setBuildStatus(BuildStatus buildStatus) {
        this.buildStatus = buildStatus;
    }

    @Override
    public String toString() {
        return toStringLimited();
    }

    public String toStringLimited() {
        return "BuildDriverResultRest{" + "buildStatus=" + buildStatus + '}';
    }
}
