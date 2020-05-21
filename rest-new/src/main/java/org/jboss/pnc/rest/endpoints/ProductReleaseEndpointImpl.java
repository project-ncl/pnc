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

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.enums.SupportLevel;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.rest.api.endpoints.ProductReleaseEndpoint;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Stateless
public class ProductReleaseEndpointImpl implements ProductReleaseEndpoint {

    @Inject
    private ProductReleaseProvider productReleaseProvider;

    private EndpointHelper<Integer, ProductRelease, ProductReleaseRef> endpointHelper;

    @PostConstruct
    public void init() {
        endpointHelper = new EndpointHelper<>(ProductRelease.class, productReleaseProvider);
    }

    @Override
    public ProductRelease createNew(ProductRelease productRelease) {
        return endpointHelper.create(productRelease);
    }

    @Override
    public ProductRelease getSpecific(String id) {
        return endpointHelper.getSpecific(id);
    }

    @Override
    public void update(String id, ProductRelease productRelease) {
        endpointHelper.update(id, productRelease);
    }

    @Override
    public ProductRelease patchSpecific(String id, ProductRelease productRelease) {
        return endpointHelper.update(id, productRelease);
    }

    @Override
    public Set<SupportLevel> getSupportLevels() {
        List<SupportLevel> supportLevels = Arrays.asList(SupportLevel.values());
        return new HashSet<>(supportLevels);

    }
}
