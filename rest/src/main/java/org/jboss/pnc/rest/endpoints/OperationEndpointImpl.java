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
package org.jboss.pnc.rest.endpoints;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.jboss.pnc.dto.DeliverableAnalyzerOperation;
import org.jboss.pnc.dto.OperationRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.providers.api.DeliverableAnalyzerOperationProvider;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.rest.api.endpoints.OperationEndpoint;
import org.jboss.pnc.rest.api.parameters.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class OperationEndpointImpl implements OperationEndpoint {

    private static final Logger logger = LoggerFactory.getLogger(OperationEndpointImpl.class);

    @Inject
    private DeliverableAnalyzerOperationProvider delAnalyzerOperationProvider;

    private EndpointHelper<Base32LongID, DeliverableAnalyzerOperation, OperationRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(DeliverableAnalyzerOperation.class, delAnalyzerOperationProvider);
    }

    @Override
    public DeliverableAnalyzerOperation createNewOrUpdate(@NotNull DeliverableAnalyzerOperation operation) {
        if (operation.getId() != null) {
            return delAnalyzerOperationProvider.saveOrUpdate(operation.getId(), operation);
        } else {
            return endpointHelper.create(operation);
        }
    }

    @Override
    public void update(String id, @NotNull DeliverableAnalyzerOperation operation) {
        endpointHelper.update(id, operation);
    }

    @Override
    public DeliverableAnalyzerOperation getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public Page<DeliverableAnalyzerOperation> getAllDeliverableAnalyzerOperation(@Valid PageParameters pageParams) {
        logger.debug("Retrieving deliverable analyzer operations with these " + pageParams.toString());
        return delAnalyzerOperationProvider
                .getAll(pageParams.getPageIndex(), pageParams.getPageSize(), pageParams.getSort(), pageParams.getQ());
    }

}
