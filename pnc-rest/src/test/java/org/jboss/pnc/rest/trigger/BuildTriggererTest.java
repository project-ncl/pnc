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
package org.jboss.pnc.rest.trigger;

import org.jboss.pnc.core.builder.BuildCoordinator;
import org.jboss.pnc.datastore.repositories.BuildConfigurationAuditedRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationRepository;
import org.jboss.pnc.datastore.repositories.BuildConfigurationSetRepository;
import org.jboss.pnc.model.BuildConfiguration;
import org.jboss.pnc.model.User;
import org.junit.Ignore;
import org.junit.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BuildTriggererTest {

    @Test
    @Ignore //TODO enable
    public void shouldTriggerBuild() throws Exception {
        //given
        BuildConfiguration exampleConfiguration = new BuildConfiguration();
        exampleConfiguration.setId(6);

        BuildConfigurationRepository repository = mock(BuildConfigurationRepository.class);
        doReturn(exampleConfiguration).when(repository).findOne(6);

        BuildConfigurationAuditedRepository buildConfigAudRepository = mock(BuildConfigurationAuditedRepository.class);

        BuildConfigurationSetRepository buildConfigurationSetRepository = mock(BuildConfigurationSetRepository.class);

        BuildCoordinator builder = mock(BuildCoordinator.class);
        BuildTriggerer buildTriggerer = new BuildTriggerer(builder, repository, buildConfigAudRepository, buildConfigurationSetRepository);

        User user = null;

        //when
        buildTriggerer.triggerBuilds(6, user);

        verify(builder).build(eq(exampleConfiguration), user, null); //TODO validate return ?
    }

}