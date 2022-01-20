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
package org.jboss.pnc.bpm.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.bpm.model.BuildExecutionConfigurationRest;
import org.jboss.pnc.bpm.model.ComponentBuildParameters;
import org.jboss.pnc.mock.spi.BuildExecutionConfigurationMock;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ComponentBuildParametersSerialization {

    private final Logger log = LoggerFactory.getLogger(ComponentBuildParametersSerialization.class);

    protected static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    public void shouldSerializeParameters() throws JsonProcessingException {

        BuildExecutionConfiguration buildExecutionConfiguration = BuildExecutionConfigurationMock.mock();
        BuildExecutionConfigurationRest buildExecutionConfigurationRest = new BuildExecutionConfigurationRest(
                buildExecutionConfiguration);
        ComponentBuildParameters processParameters = new ComponentBuildParameters(
                "http://pncBaseUrl",
                "http://aproxBaseUrl",
                "http://repourBaseUrl",
                "http://daBaseUrl",
                false,
                true,
                buildExecutionConfigurationRest);

        String string = MAPPER.writeValueAsString(processParameters);
        log.debug("Serialized: {}", string);
        Assert.assertTrue(string.contains(BuildExecutionConfigurationMock.DEFAULT_SYSTEM_IMAGE_ID));
    }

}
