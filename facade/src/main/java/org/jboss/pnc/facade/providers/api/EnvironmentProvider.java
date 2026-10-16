/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.Environment;

public interface EnvironmentProvider
        extends Provider<Integer, org.jboss.pnc.model.BuildEnvironment, Environment, Environment> {

    /**
     * Marks the environment as deprecated and adds attribute to indicate to what environment should be the deprecated
     * upgraded to.
     *
     * @param id ID of the environment to be deprecated;
     * @param replacementId ID of the environment that is replacing the environment.
     * @return The deprecated environment.
     */
    Environment deprecateEnvironment(String id, String replacementId);
}
