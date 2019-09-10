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
package org.jboss.pnc.integration_new.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.client.RemoteResourceException;
import org.jboss.pnc.client.patch.ObjectMapperProvider;
import org.jboss.pnc.client.patch.PatchBuilderException;
import org.jboss.pnc.dto.BuildConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildConfigurationSerializationTest {

    private static final Logger logger = LoggerFactory.getLogger(BuildConfigurationSerializationTest.class);

    @Test
    public void shouldPatchBuildConfiguration() throws RemoteResourceException, PatchBuilderException, IOException {
        Instant now = Instant.now();
        BuildConfiguration buildConfiguration = BuildConfiguration.builder()
                .id("1")
                .name("name")
                .creationTime(now)
                .build();

        ObjectMapper mapper = ObjectMapperProvider.getInstance();
        String serialized = mapper.writeValueAsString(buildConfiguration);
        logger.info(serialized);

        BuildConfiguration deserialized = mapper.readValue(serialized, BuildConfiguration.class);
        Assert.assertEquals(now, deserialized.getCreationTime());

    }



}
