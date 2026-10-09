/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.facade.providers.api.BuildConfigurationSupportedGenericParametersProvider;

/**
 * Provider of statically defined BuildConfiguration generic parameters, that are known to the Orchestrator.
 *
 * The parameters are set in the resources file
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 *
 */
@PermitAll
@ApplicationScoped
public class BuildConfigurationSupportedGenericParametersProviderImpl
        implements BuildConfigurationSupportedGenericParametersProvider {

    private final Set<Parameter> supportedGenericParameters = new HashSet<>();

    public BuildConfigurationSupportedGenericParametersProviderImpl() {
        for (BuildConfigurationParameterKeys value : BuildConfigurationParameterKeys.values()) {
            supportedGenericParameters.add(new Parameter(value.name(), value.getDesc(), value.getValues()));
        }
    }

    @Override
    public Set<Parameter> getSupportedGenericParameters() {
        return supportedGenericParameters;
    }
}
