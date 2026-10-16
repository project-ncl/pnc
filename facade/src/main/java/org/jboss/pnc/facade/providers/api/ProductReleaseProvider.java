/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.ProductRelease;
import org.jboss.pnc.dto.ProductReleaseRef;
import org.jboss.pnc.dto.response.Page;

public interface ProductReleaseProvider
        extends Provider<Integer, org.jboss.pnc.model.ProductRelease, ProductRelease, ProductReleaseRef> {

    Page<ProductRelease> getProductReleasesForProductVersion(
            int pageIndex,
            int pageSize,
            String sortingRsql,
            String query,
            String productVersionId);
}
