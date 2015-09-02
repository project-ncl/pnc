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
package org.jboss.pnc.integration;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.arquillian.transaction.api.annotation.Transactional;
import org.jboss.pnc.integration.deployments.Deployments;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordSet;
import org.jboss.pnc.model.ProductMilestone;
import org.jboss.pnc.rest.provider.BuildRecordSetProvider;
import org.jboss.pnc.rest.restmodel.BuildRecordSetRest;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordRepository;
import org.jboss.pnc.spi.datastore.repositories.BuildRecordSetRepository;
import org.jboss.pnc.spi.datastore.repositories.ProductMilestoneRepository;
import org.jboss.pnc.test.category.ContainerTest;
import org.jboss.shrinkwrap.api.spec.EnterpriseArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.lang.invoke.MethodHandles;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(Arquillian.class)
@Category(ContainerTest.class)
public class BuildRecordSetsTest {

    public static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static Integer buildRecordSetId;
    private static Integer buildRecordId;
    private static Integer productMilestoneId;

    @Inject
    private BuildRecordRepository buildRecordRepository;

    @Inject
    private BuildRecordSetRepository buildRecordSetRepository;

    @Inject
    private ProductMilestoneRepository productMilestoneRepository;

    @Inject
    private BuildRecordSetProvider buildRecordSetProvider;

    @Deployment
    public static EnterpriseArchive deploy() {
        EnterpriseArchive enterpriseArchive = Deployments.baseEarWithTestDependencies();
        WebArchive war = enterpriseArchive.getAsType(WebArchive.class, "/rest.war");
        war.addClass(BuildRecordSetsTest.class);
        logger.info(enterpriseArchive.toString(true));
        return enterpriseArchive;
    }

    @Test
    @InSequence(-1)
    @Transactional
    public void shouldInsertValuesIntoDB() {

        BuildRecord buildRecord = buildRecordRepository.queryAll().iterator().next();
        ProductMilestone productMilestone = productMilestoneRepository.queryAll().iterator().next();

        buildRecordId = buildRecord.getId();
        productMilestoneId = productMilestone.getId();

        BuildRecordSet.Builder builder = BuildRecordSet.Builder.newBuilder();
        BuildRecordSet buildRecordSet = builder.buildRecord(buildRecord).performedInProductMilestone(productMilestone).build();

        buildRecordSet = buildRecordSetRepository.save(buildRecordSet);

        buildRecordSetId = buildRecordSet.getId();
    }

    @Test
    @InSequence(1)
    public void shouldGetAllBuildRecordSets() {
        // when
        Collection<BuildRecordSetRest> buildRecordSets = buildRecordSetProvider.getAll(0, 50, null, null).getContent();

        // then
        assertThat(buildRecordSets).isNotNull();
        assertThat(buildRecordSets.size() > 1);
    }

    @Test
    @InSequence(2)
    public void shouldGetSpecificBuildRecordSet() {
        // when
        BuildRecordSetRest buildRecordSet = buildRecordSetProvider.getSpecific(buildRecordSetId);

        // then
        assertThat(buildRecordSet).isNotNull();
    }

    @Test
    @InSequence(3)
    public void shouldGetBuildRecordSetOfProductMilestone() {
        // when
        Collection<BuildRecordSetRest> buildRecordSetRests = buildRecordSetProvider.getAllForPerformedInProductMilestone(0, 50, null, null, productMilestoneId).getContent();

        // then
        assertThat(buildRecordSetRests).hasSize(1);
    }

    @Test
    @InSequence(4)
    public void shouldGetBuildRecordSetOfBuildRecord() {
        // when
        Collection<BuildRecordSetRest> buildRecordSetRests = buildRecordSetProvider.getAllForBuildRecord(0, 50, null, null, buildRecordId).getContent();

        // then
        assertThat(buildRecordSetRests).hasSize(1);
    }

    @Test
    @InSequence(5)
    public void shouldNotCascadeDeletionOfBuildRecordSet() throws Exception {
        // when
        int buildRecordCount = buildRecordRepository.count();
        int productMilestoneCount = productMilestoneRepository.count();

        buildRecordSetProvider.delete(buildRecordSetId);

        // then
        assertThat(buildRecordRepository.count()).isEqualTo(buildRecordCount);
        assertThat(productMilestoneRepository.count()).isEqualTo(productMilestoneCount);
    }

}
