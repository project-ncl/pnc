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
package org.jboss.pnc.mapper;

import org.jboss.pnc.mapper.api.BuildMapper;
import org.jboss.pnc.mapper.api.GroupBuildMapper;
import org.jboss.pnc.mapper.api.IdMapper;
import org.jboss.pnc.mapper.api.ProductMilestoneMapper;
import org.jboss.pnc.model.BuildConfigSetRecord;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.spi.datastore.Datastore;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.Serializable;

/**
 * Maps <ID> fields into Database references without actual data (data are fetched if other fields than id is accessed)
 *
 * @author jmichalo
 */
@ApplicationScoped
@Transactional
public class IDToReferenceMapper {

    private EntityManager em;

    private BuildMapper buildMapper;

    private ProductMilestoneMapper productMilestoneMapper;

    private GroupBuildMapper groupBuildMapper;

    private Datastore datastore;

    // CDI
    public IDToReferenceMapper() {
    }

    @Inject
    public IDToReferenceMapper(
            EntityManager em,
            BuildMapper buildMapper,
            ProductMilestoneMapper productMilestoneMapper,
            GroupBuildMapper groupBuildMapper,
            Datastore datastore) {
        this.em = em;
        this.buildMapper = buildMapper;
        this.productMilestoneMapper = productMilestoneMapper;
        this.groupBuildMapper = groupBuildMapper;
        this.datastore = datastore;
    }

    public <ID extends Serializable, DB extends GenericEntity<ID>, DTOID> DB map(
            DTOID id,
            IdMapper<ID, DTOID> idMapper,
            Class<DB> type) {
        if (id == null) {
            return null;
        }
        return em.getReference(type, idMapper.toEntity(id));
    }

    public ProductMilestone toProductMilestoneReference(String id) {
        return map(id, productMilestoneMapper.getIdMapper(), ProductMilestone.class);
    }

    public BuildConfigurationAudited toBCAReference(IdRev id) {
        return datastore.getBuildConfigurationAudited(id);
    }

    public BuildRecord toBuildRecordReference(String id) {
        return map(id, buildMapper.getIdMapper(), BuildRecord.class);
    }

    public BuildConfigSetRecord toBCSRReference(String id) {
        return map(id, groupBuildMapper.getIdMapper(), BuildConfigSetRecord.class);
    }
}
