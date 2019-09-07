/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.providers.api;

import org.jboss.pnc.dto.ProductMilestone;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.dto.response.Page;
import org.jboss.pnc.facade.validation.EmptyEntityException;
import org.jboss.pnc.facade.validation.RepositoryViolationException;

public interface ProductMilestoneProvider extends Provider<Integer, org.jboss.pnc.model.ProductMilestone, ProductMilestone, ProductMilestoneRef> {

    void closeMilestone(String id, ProductMilestone restEntity);

    void closeMilestone(String id, ProductMilestone restEntity, String accessToken);

    void cancelMilestoneCloseProcess(String id)
            throws RepositoryViolationException, EmptyEntityException;

    Page<ProductMilestone> getProductMilestonesForProductVersion(int pageIndex,
                                                                 int pageSize,
                                                                 String sortingRsql,
                                                                 String query,
                                                                 String productVersionId);
}
