/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.endpoints;

import org.jboss.pnc.dto.Environment;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.EnvironmentProvider;
import org.jboss.pnc.rest.api.endpoints.EnvironmentEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class EnvironmentEndpointImpl implements EnvironmentEndpoint {

    @Inject
    private EnvironmentProvider environmentProvider;

    private EndpointHelper<Integer, Environment, Environment> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(Environment.class, environmentProvider);
    }

    @Override
    public Page<Environment> getAll(PageParameters pageParameters) {
        return endpointHelper.getAll(pageParameters);
    }

    @Override
    public Environment getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public Environment createNew(Environment environment) {
        return endpointHelper.create(environment);
    }

}
