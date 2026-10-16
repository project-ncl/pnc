/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.dto.internal;

import java.io.Serializable;

import lombok.Data;

@Data
public class RepositoryCreationDataWrapper implements Serializable {

    private String message;
    private String externalUrl;
    private String internalUrl;
    private String preBuildSyncEnabled;

    public boolean isPreBuildSyncEnabled() {
        return Boolean.parseBoolean(preBuildSyncEnabled);
    }
}
