/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.datastore.repositories;

import org.jboss.pnc.common.json.moduleconfig.AlignmentConfig;
import org.jboss.pnc.datastore.repositories.internal.AbstractRepository;
import org.jboss.pnc.datastore.repositories.internal.BuildConfigurationSpringRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.GenericEntity;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Date;
import java.util.Objects;

@Stateless
public class BuildConfigurationRepositoryImpl extends AbstractRepository<BuildConfiguration, Integer>
        implements BuildConfigurationRepository {

    private AlignmentConfig alignmentConfig;

    /**
     * @deprecated Created for CDI.
     */
    @Deprecated
    public BuildConfigurationRepositoryImpl() {
        super(null, null);
    }

    @Inject
    public BuildConfigurationRepositoryImpl(
            BuildConfigurationSpringRepository buildConfigurationSpringRepository,
            AlignmentConfig alignmentConfig) {

        super(buildConfigurationSpringRepository, buildConfigurationSpringRepository);
        this.alignmentConfig = alignmentConfig;
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public BuildConfiguration save(BuildConfiguration buildConfiguration) {
        Integer id = buildConfiguration.getId();
        BuildConfiguration persisted = queryById(id);
        if (persisted != null) {
            if (!areParametersEqual(persisted, buildConfiguration)
                    || !equalAuditedValues(persisted, buildConfiguration)) {
                // always increment the revision of main entity when the child collection is updated
                // the @PreUpdate method in BuildConfiguration was removed, the calculation of whether the
                // lastModificationTime needs to be changed is done here
                buildConfiguration.setLastModificationTime(new Date());
            } else {
                // No changes to audit, reset the lastModificationUser to previous existing
                buildConfiguration.setLastModificationUser(persisted.getLastModificationUser());
            }
        }
        // Update or save need to set the default alignment parameters
        buildConfiguration.setDefaultAlignmentParams(
                alignmentConfig.getAlignmentParameters().get(buildConfiguration.getBuildType().toString()));

        return springRepository.save(buildConfiguration);
    }

    private boolean equalAuditedValues(BuildConfiguration persisted, BuildConfiguration toUpdate) {
        return Objects.equals(persisted.getName(), toUpdate.getName())
                && Objects.equals(persisted.getBuildScript(), toUpdate.getBuildScript())
                && equalsId(persisted.getRepositoryConfiguration(), toUpdate.getRepositoryConfiguration())
                && Objects.equals(persisted.getScmRevision(), toUpdate.getScmRevision())
                && equalsId(persisted.getProject(), toUpdate.getProject())
                && equalsId(persisted.getBuildEnvironment(), toUpdate.getBuildEnvironment())
                && (persisted.isArchived() == toUpdate.isArchived())
                && (persisted.getBuildType() == toUpdate.getBuildType());
    }

    private boolean equalsId(GenericEntity<Integer> persisted, GenericEntity<Integer> toUpdate) {
        if (persisted == null && toUpdate == null) {
            return true;
        }

        if (persisted == null || toUpdate == null) {
            return false;
        }

        return persisted.getId().equals(toUpdate.getId());
    }

    private boolean areParametersEqual(BuildConfiguration persisted, BuildConfiguration newBC) {
        if (persisted.getGenericParameters() == null && newBC.getGenericParameters() == null) {
            return true;
        }

        if (persisted.getGenericParameters() == null || newBC.getGenericParameters() == null) {
            return false;
        }

        return persisted.getGenericParameters().equals(newBC.getGenericParameters());
    }

}
