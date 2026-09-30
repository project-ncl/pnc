/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.rest.endpoints;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.enums.SupportLevel;
import org.jboss.pnc.facade.providers.api.ProductReleaseProvider;
import org.jboss.pnc.rest.api.endpoints.ProductReleaseEndpoint;

@ApplicationScoped
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
