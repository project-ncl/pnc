/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import org.jboss.pnc.common.concurrent.Sequence;
import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.GenericEntity;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @param <T> tested provider type
 */
public abstract class AbstractBase32LongIDProviderTest<T extends GenericEntity<Base32LongID>>
        extends AbstractProviderTest<Base32LongID, T> {

    public AbstractBase32LongIDProviderTest() {
        super(Base32LongID.class);
    }

    protected Base32LongID getNextId() {
        return new Base32LongID(Sequence.nextId());
    }

}
