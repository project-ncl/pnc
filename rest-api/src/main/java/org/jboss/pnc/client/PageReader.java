/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.client;

import java.util.function.Function;

import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.rest.api.parameters.PageParameters;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class PageReader<T> {

    private Function<PageParameters, Page<T>> endpoint;

    private RemoteCollectionConfig config;

    public PageReader(Function<PageParameters, Page<T>> endpoint, RemoteCollectionConfig config) {
        this.endpoint = endpoint;
        this.config = config;
    }

    public RemoteCollection<T> getCollection() {
        return new DefaultRemoteCollection<>(endpoint, config);
    }

}
