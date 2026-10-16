/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Map;

public interface HealthCheckProvider {

    /**
     * Checks for the system's health should be performed in this method. The returned map should contain the
     * description of the test, and whether it passed or not
     *
     * @return results
     */
    Map<String, Boolean> check();
}
