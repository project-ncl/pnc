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

import org.jboss.pnc.model.Base32LongID;
import org.jboss.pnc.model.DeliverableAnalyzerOperation;
import org.jboss.pnc.model.DeliverableAnalyzerOperation_;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.Operation;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
public abstract class OperationRSQLMapper<T extends Operation> extends AbstractRSQLMapper<Base32LongID, T> {

    public OperationRSQLMapper(Class<T> type) {
        super(type);
    }

    @Override
    protected SingularAttribute<? super T, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "user":
                return DeliverableAnalyzerOperation_.user;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<T, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<? super T, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return DeliverableAnalyzerOperation_.id;
            case "endTime":
                return DeliverableAnalyzerOperation_.endTime;
            case "result":
                return DeliverableAnalyzerOperation_.result;
            case "progressStatus":
                return DeliverableAnalyzerOperation_.progressStatus;
            case "startTime":
                return DeliverableAnalyzerOperation_.startTime;
            case "submitTime":
                return DeliverableAnalyzerOperation_.submitTime;
            default:
                return null;
        }
    }

}
