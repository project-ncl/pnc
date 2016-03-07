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
package org.jboss.pnc.integration.client.util;

import java.io.IOException;
import java.lang.invoke.MethodHandles;

import org.jboss.pnc.model.BuildConfigurationAudited;
import org.jboss.pnc.model.BuildEnvironment;
import org.jboss.pnc.model.IdRev;
import org.jboss.pnc.model.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;

/*
 * Required as the default Deserializer would call setId() which throws a UnsupportedOperationException("Not supported in audited entity")
 * causing the unmarshalling to fail
 */
public class BuildConfigurationAuditedDeserializer extends JsonDeserializer<BuildConfigurationAudited> {

    protected Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BuildConfigurationAudited deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = jp.getCodec().readTree(jp);

        int rev = (Integer) ((IntNode) node.get("rev")).numberValue();
        String name = node.get("name").asText();
        String description = node.get("description").asText();
        String buildScript = node.get("buildScript").asText();
        String scmRepoURL = node.get("scmRepoURL").asText();
        String scmRevision = node.get("scmRevision").asText();
        String scmMirrorRepoURL = node.get("scmMirrorRepoURL").asText();
        String scmMirrorRevision = node.get("scmMirrorRevision").asText();

        JsonNode idRevObj = node.get("idRev");
        JsonNode projectObj = node.get("project");
        JsonNode buildEnvironmentObj = node.get("buildEnvironment");

        BuildConfigurationAudited configurationAudited = new BuildConfigurationAudited();
        configurationAudited.setBuildScript(buildScript);
        configurationAudited.setDescription(description);
        configurationAudited.setName(name);
        configurationAudited.setScmRepoURL(scmRepoURL);
        configurationAudited.setScmRevision(scmRevision);
        configurationAudited.setScmMirrorRepoURL(scmMirrorRepoURL);
        configurationAudited.setScmMirrorRevision(scmMirrorRevision);
        configurationAudited.setRev(rev);

        if (idRevObj != null) {
            IdRev idRev = objectMapper.convertValue(idRevObj, IdRev.class);
            configurationAudited.setIdRev(idRev);
            configurationAudited.setBuildRecordId(idRev.getId());
        }
        if (projectObj != null) {
            configurationAudited.setProject(objectMapper.convertValue(projectObj, Project.class));
        }
        if (buildEnvironmentObj != null) {
            configurationAudited.setBuildEnvironment(objectMapper.convertValue(buildEnvironmentObj, BuildEnvironment.class));
        }

        logger.info("Node: {}, BuildConfigurationAudited: {}", node, configurationAudited);

        return configurationAudited;
    }

}
