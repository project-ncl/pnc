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

import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigSetRecord_;
import org.jboss.pnc.model.GenericEntity;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.metamodel.SetAttribute;
import javax.persistence.metamodel.SingularAttribute;

/**
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 */
@ApplicationScoped
public class GroupBuildRSQLMapper extends AbstractRSQLMapper<Long, BuildConfigSetRecord> {

    public GroupBuildRSQLMapper() {
        super(BuildConfigSetRecord.class);
    }

    @Override
    protected SingularAttribute<BuildConfigSetRecord, ? extends GenericEntity<Integer>> toEntity(String name) {
        switch (name) {
            case "user":
                return BuildConfigSetRecord_.user;
            case "groupConfig":
                return BuildConfigSetRecord_.buildConfigurationSet;
            case "productVersion":
                return BuildConfigSetRecord_.productVersion;
            default:
                return null;
        }
    }

    @Override
    protected SetAttribute<BuildConfigSetRecord, ? extends GenericEntity<?>> toEntitySet(String name) {
        return null;
    }

    @Override
    protected SingularAttribute<BuildConfigSetRecord, ?> toAttribute(String name) {
        switch (name) {
            case "id":
                return BuildConfigSetRecord_.id;
            case "startTime":
                return BuildConfigSetRecord_.startTime;
            case "endTime":
                return BuildConfigSetRecord_.endTime;
            case "status":
                return BuildConfigSetRecord_.status;
            case "temporaryBuild":
                return BuildConfigSetRecord_.temporaryBuild;
            default:
                return null;
        }
    }
}
