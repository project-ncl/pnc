/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.Set;

import org.jboss.pnc.dto.response.Parameter;

public interface BuildConfigurationSupportedGenericParametersProvider {

    Set<Parameter> getSupportedGenericParameters();
}
