/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.facade.rsql.mapper;

import org.jboss.pnc.model.BuildPushOperation;
import org.jboss.pnc.model.BuildPushOperation_;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@ApplicationScoped
public class BuildPushOperationRSQLMapper extends GenericOperationRSQLMapper<BuildPushOperation> {

    public BuildPushOperationRSQLMapper() {
        super(BuildPushOperation.class);
    }

    @Override
    protected SingularAttribute<? super BuildPushOperation, ? extends GenericEntity<?>> toEntity(String name) {
        switch (name) {
            case "build":
                return BuildPushOperation_.build;
            default:
                return super.toEntity(name);
        }
    }

    @Override
    protected SingularAttribute<? super BuildPushOperation, ?> toAttribute(String name) {
        switch (name) {
            default:
                return super.toAttribute(name);
        }
    }

}
