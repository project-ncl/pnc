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
package org.jboss.pnc.rest.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jboss.pnc.dto.BuildConfiguration;
import org.jboss.pnc.dto.ProjectRef;
import org.jboss.pnc.dto.requests.BuildConfigWithSCMRequest;
import org.junit.Test;

import java.io.IOException;

/**
 *
 * @author jbrazdil
 */
public class DeserializerTest {

    private final JacksonProvider provider = new JacksonProvider();

    public DeserializerTest() throws Exception {
    }

    @Test
    public void testDTOAsRefDeserialization3() throws IOException {
        String json = "{" + "         \"id\":122," + "         \"name\":\"mtest180719\","
                + "         \"description\":null," + "         \"issueTrackerUrl\":null,"
                + "         \"projectUrl\":null," + "         \"buildConfigs\":[" + "            {"
                + "               \"id\":134," + "               \"name\":\"mtest1907252\","
                + "               \"description\":null," + "               \"buildScript\":\"mvn clean deploy\","
                + "               \"scmRevision\":\"master\","
                + "               \"creationTime\":\"2019-07-25T09:32:44.206Z\","
                + "               \"modificationTime\":\"2019-08-14T11:37:51.355Z\","
                + "               \"archived\":false," + "               \"buildType\":\"MVN\"" + "            }"
                + "         ]" + "      }";
        ObjectMapper mapper = provider.getContext(null);
        ProjectRef obj = mapper.readValue(json, ProjectRef.class);
    }

    @Test
    public void testDTOAsRefDeserialization2() throws IOException {
        String json = "{" + "      \"name\":\"mtest190826\"," + "      \"environment\":{" + "         \"id\":8,"
                + "         \"name\":\"OpenJDK 1.8.0; Mvn 3.5.2\","
                + "         \"description\":\"OpenJDK 1.8.0; Mvn 3.5.2\","
                + "         \"systemImageRepositoryUrl\":\"docker-registry-default.cloud.registry.upshift.redhat.com\","
                + "         \"systemImageId\":\"newcastle/builder-rhel-7-j8-mvn3.5.2:latest\","
                + "         \"attributes\":{" + "            \"JDK\":\"1.8.0\"," + "            \"MAVEN\":\"3.5.2\","
                + "            \"OS\":\"Linux\"" + "         }," + "         \"systemImageType\":\"DOCKER_IMAGE\","
                + "         \"deprecated\":false" + "      }," + "      \"buildType\":\"MVN\","
                + "      \"buildScript\":\"mvn clean deploy\"," + "      \"parameters\":{      },"
                + "      \"dependencies\":{}," + "      \"scmRevision\":\"master\"," + "      \"project\":{"
                + "         \"id\":122," + "         \"name\":\"mtest180719\"," + "         \"description\":null,"
                + "         \"issueTrackerUrl\":null," + "         \"projectUrl\":null," + "         \"buildConfigs\":{"
                + "            \"134\": {" + "               \"id\":134," + "               \"name\":\"mtest1907252\","
                + "               \"description\":null," + "               \"buildScript\":\"mvn clean deploy\","
                + "               \"scmRevision\":\"master\","
                + "               \"creationTime\":\"2019-07-25T09:32:44.206Z\","
                + "               \"modificationTime\":\"2019-08-14T11:37:51.355Z\","
                + "               \"archived\":false," + "               \"buildType\":\"MVN\"" + "            }"
                + "         }" + "      }," + "      \"groupConfigs\":{}" + "   }";
        ObjectMapper mapper = provider.getContext(null);
        BuildConfiguration obj = mapper.readValue(json, BuildConfiguration.class);
    }

    @Test
    public void testDTOAsRefDeserialization() throws IOException {
        String json = "{" + "   \"scmUrl\":\"https://github.com/matedo1/empty_19082666\","
                + "   \"preBuildSyncEnabled\":true," + "   \"buildConfig\":{" + "      \"name\":\"mtest190826\","
                + "      \"environment\":{" + "         \"id\":8," + "         \"name\":\"OpenJDK 1.8.0; Mvn 3.5.2\","
                + "         \"description\":\"OpenJDK 1.8.0; Mvn 3.5.2\","
                + "         \"systemImageRepositoryUrl\":\"docker-registry-default.cloud.registry.upshift.redhat.com\","
                + "         \"systemImageId\":\"newcastle/builder-rhel-7-j8-mvn3.5.2:latest\","
                + "         \"attributes\":{" + "            \"JDK\":\"1.8.0\"," + "            \"MAVEN\":\"3.5.2\","
                + "            \"OS\":\"Linux\"" + "         }," + "         \"systemImageType\":\"DOCKER_IMAGE\","
                + "         \"deprecated\":false" + "      }," + "      \"buildType\":\"MVN\","
                + "      \"buildScript\":\"mvn clean deploy\"," + "      \"parameters\":{      },"
                + "      \"dependencies\":{}," + "      \"scmRevision\":\"master\"," + "      \"project\":{"
                + "         \"id\":122," + "         \"name\":\"mtest180719\"," + "         \"description\":null,"
                + "         \"issueTrackerUrl\":null," + "         \"projectUrl\":null," + "         \"buildConfigs\":{"
                + "            \"134\": {" + "               \"id\":134," + "               \"name\":\"mtest1907252\","
                + "               \"description\":null," + "               \"buildScript\":\"mvn clean deploy\","
                + "               \"scmRevision\":\"master\","
                + "               \"creationTime\":\"2019-07-25T09:32:44.206Z\","
                + "               \"modificationTime\":\"2019-08-14T11:37:51.355Z\","
                + "               \"archived\":false," + "               \"buildType\":\"MVN\"" + "            }"
                + "         }" + "      }," + "      \"groupConfigs\":{}" + "   }" + "}";
        ObjectMapper mapper = provider.getContext(null);
        BuildConfigWithSCMRequest obj = mapper.readValue(json, BuildConfigWithSCMRequest.class);
    }
}
