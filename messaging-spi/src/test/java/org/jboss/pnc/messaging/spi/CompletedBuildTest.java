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
package org.jboss.pnc.messaging.spi;

import org.assertj.core.api.Assertions;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class CompletedBuildTest {

    Logger logger = LoggerFactory.getLogger(CompletedBuildTest.class);

    @Test
    public void shouldSerializeObject() throws IOException {
        //given
        String pncBuildId = "123";
        String buildConfigurationName = "project-1.2.3";
        CompletedBuild completedBuild = new CompletedBuild(pncBuildId, buildConfigurationName);

        //when
        String serialized = completedBuild.toJson();
        logger.info("Serialized: {}", serialized);

        CompletedBuild deserialized = JsonOutputConverterMapper.readValue(serialized, CompletedBuild.class);

        //then
        Assertions.assertThat(deserialized).isEqualToComparingFieldByField(completedBuild);

    }
}
