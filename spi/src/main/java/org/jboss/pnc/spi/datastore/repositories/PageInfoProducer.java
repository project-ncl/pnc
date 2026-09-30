/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.spi.datastore.repositories;

import org.jboss.pnc.spi.datastore.repositories.api.PageInfo;

/**
 * Creates page info based on provided page offset and size.
 */
public interface PageInfoProducer {

    /**
     * Gets PageInfo based on provided data.
     *
     * @param offset Page number.
     * @param size Page size.
     * @return Page Info object.
     */
    PageInfo getPageInfo(int offset, int size);
}
