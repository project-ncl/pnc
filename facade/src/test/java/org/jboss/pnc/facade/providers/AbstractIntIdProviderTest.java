/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import java.util.concurrent.atomic.AtomicInteger;

import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <T> tested provider type
 */
public abstract class AbstractIntIdProviderTest<T extends GenericEntity<java.lang.Integer>>
        extends AbstractProviderTest<Integer, T> {

    protected AtomicInteger entityId = new AtomicInteger(1);

    public AbstractIntIdProviderTest() {
        super(Integer.class);
    }

    protected Integer getNextId() {
        return entityId.getAndIncrement();
    }

}
