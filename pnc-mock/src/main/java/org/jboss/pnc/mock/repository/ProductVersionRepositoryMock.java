/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.repository;

import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.ProductVersionRepository;

public class ProductVersionRepositoryMock extends IntIdRepositoryMock<ProductVersion>
        implements ProductVersionRepository {

    @Override
    public long countMilestonesInThisVersion(Integer id) {
        return 0;
    }

    @Override
    public long countBuiltArtifactsInThisVersion(Integer id) {
        return 0;
    }
}
