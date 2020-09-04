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

package org.jboss.pnc.facade.util;

import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildConfigurationSet;
import org.jboss.pnc.model.ProductVersion;
import org.jboss.pnc.spi.datastore.repositories.BuildConfigurationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

/**
 * A work around for Hibernate's lazy initialization errors
 *
 * @author Sebastian Laskawiec
 */
@Stateless
public class HibernateLazyInitializer {
    private static final Logger log = LoggerFactory.getLogger(HibernateLazyInitializer.class);

    @Inject
    private BuildConfigurationRepository buildConfigurationRepository;

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BuildConfiguration initializeBuildConfigurationBeforeTriggeringIt(BuildConfiguration buildConfiguration) {
        log.trace("Initializing BC {}.", buildConfiguration.getId());

        buildConfiguration.getDependencies();
        buildConfiguration.getIndirectDependencies();

        ProductVersion productVersion = buildConfiguration.getProductVersion();
        if (productVersion != null) {
            productVersion.getProduct();
            productVersion.getCurrentProductMilestone();
        }
        return buildConfiguration;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BuildConfigurationAudited initializeBuildConfigurationAuditedBeforeTriggeringIt(
            BuildConfigurationAudited buildConfigurationAudited) {
        log.trace("Initializing BCA {}.", buildConfigurationAudited.getIdRev());
        buildConfigurationAudited
                .setBuildConfiguration(buildConfigurationRepository.queryById(buildConfigurationAudited.getId()));
        initializeBuildConfigurationBeforeTriggeringIt(buildConfigurationAudited.getBuildConfiguration());

        return buildConfigurationAudited;
    }

    @TransactionAttribute(TransactionAttributeType.MANDATORY)
    public BuildConfigurationSet initializeBuildConfigurationSetBeforeTriggeringIt(BuildConfigurationSet bcs) {
        log.trace("Initializing {} build configurations in set {}.", bcs.getBuildConfigurations().size(), bcs.getId());
        bcs.getBuildConfigurations().stream().forEach(this::initializeBuildConfigurationBeforeTriggeringIt);
        return bcs;
    }
}
