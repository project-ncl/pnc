/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.api.constants.BuildConfigurationParameterKeys;
import org.jboss.pnc.dto.response.Parameter;
import org.jboss.pnc.facade.providers.api.BuildConfigurationSupportedGenericParametersProvider;

import javax.annotation.security.PermitAll;
import javax.enterprise.context.ApplicationScoped;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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

    private Set<Parameter> supportedGenericParameters = new HashSet<>();

    public BuildConfigurationSupportedGenericParametersProviderImpl() throws IOException {
        for (BuildConfigurationParameterKeys value : BuildConfigurationParameterKeys.values()) {
            supportedGenericParameters.add(new Parameter(value.name(), value.getDesc()));
        }
    }

    @Override
    public Set<Parameter> getSupportedGenericParameters() {
        return supportedGenericParameters;
    }
}
