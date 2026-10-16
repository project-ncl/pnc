/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <T> tested provider type
 */
public abstract class AbstractLongIdProviderTest<T extends GenericEntity<Long>> extends AbstractProviderTest<Long, T> {

    public AbstractLongIdProviderTest() {
        super(Long.class);
    }

    protected Long getNextId() {
        return Sequence.nextId();
    }

}
