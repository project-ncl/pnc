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

package org.jboss.pnc.restmodel.serialization;

import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationRest;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.rest.restmodel.bpm.RepositoryCreationUrlAutoRest;
import org.jboss.pnc.rest.restmodel.mock.RepositoryCreationUrlAutoRestMockBuilder;
import org.jboss.pnc.spi.builddriver.exception.BuildDriverException;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class RepositoryCreationUrlAutoRestSerializationTest {

    private final Logger log = LoggerFactory.getLogger(RepositoryCreationUrlAutoRestSerializationTest.class);

    @Test
    public void serializeAndDeserialize() throws IOException, BuildDriverException {

        RepositoryCreationUrlAutoRest repositoryCreationRest = RepositoryCreationUrlAutoRestMockBuilder.mock("BC1", "mvn deploy", "http://giturl");

        String json = repositoryCreationRest.toString();
        log.debug("json : {}", json);

        RepositoryCreationUrlAutoRest deserialized = JsonOutputConverterMapper.readValue(json, RepositoryCreationUrlAutoRest.class);

        String message = "Deserialized object does not match the original.";

        Assert.assertEquals(message, repositoryCreationRest.getBuildConfigurationRest().getName(), deserialized.getBuildConfigurationRest().getName());
        Assert.assertEquals(message, repositoryCreationRest.getScmUrl(), deserialized.getScmUrl());

    }
}
