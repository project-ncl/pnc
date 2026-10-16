/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.datastore.limits;

import javax.enterprise.context.ApplicationScoped;

import org.jboss.pnc.spi.datastore.repositories.PageInfoProducer;
import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;
import org.jboss.pnc.spi.datastore.repositories.api.impl.DefaultPageInfo;

@ApplicationScoped
public class DefaultPageInfoProducer implements PageInfoProducer {

    @Override
    public PageInfo getPageInfo(int offset, int size) {
        return new DefaultPageInfo(offset, size);
    }
}
