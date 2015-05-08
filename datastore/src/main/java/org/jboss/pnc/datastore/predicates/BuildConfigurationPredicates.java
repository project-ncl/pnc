/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.predicates;

import com.mysema.query.types.expr.BooleanExpression;
import org.jboss.pnc.model.QBuildConfiguration;

import static org.jboss.pnc.datastore.predicates.Utils.createNotNullPredicate;

public class BuildConfigurationPredicates {

    public static BooleanExpression withConfigurationId(Integer buildConfigurationId) {
        return createNotNullPredicate(buildConfigurationId != null, () -> QBuildConfiguration.buildConfiguration.id.eq(buildConfigurationId));
    }

    public static BooleanExpression withProjectId(Integer projectId) {
        return createNotNullPredicate(projectId != null, () -> QBuildConfiguration.buildConfiguration.project.id.eq(projectId));
    }

    public static BooleanExpression withProductId(Integer productId) {
        return createNotNullPredicate(productId != null, () -> QBuildConfiguration.buildConfiguration.productVersion.product.id.eq(productId));
    }

    public static BooleanExpression withProductVersionId(Integer productVersionId) {
        return createNotNullPredicate(productVersionId != null, () -> QBuildConfiguration.buildConfiguration.productVersion.id.eq(productVersionId));
    }

}
