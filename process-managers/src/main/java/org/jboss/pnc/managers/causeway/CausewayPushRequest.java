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
package org.jboss.pnc.managers.causeway;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.model.TargetRepository;

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@AllArgsConstructor()
@Getter
public class CausewayPushRequest {

    private String name;
    private String version;
    private final String externalBuildSystem = "PNC";
    private String externalBuildId;
    private String externalBuildUrl;
    private Date startTime;
    private Date endTime;
    private String scmURL;
    private String scmRevision;

    private BuildRoot buildRoot;

    private Set<Dependency> dependencies;
    private Set<BuiltArtifact> builtArtifacts;

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

    @AllArgsConstructor
    @Getter
    public static class BuildRoot {
        private final String container = "DOCKER_IMAGE";
        private final String host = "rhel";
        private final String architecture = "x86_64"; //TODO set based on env, some env has native build tools
        private Map<String, String> tools;
    }

    @AllArgsConstructor
    @Getter
    public static class Dependency {
        private TargetRepository.Type type; // "MAVEN|http"
        private String fileName;
        private String md5;
        private String sha256;
        private long size;
    }

    @Getter
    public static class BuiltArtifact extends Dependency {
        private final String architecture = "noarch"; //TODO set architecture
        private String url; // url where the artifact can be downloaded from (deployURL)
        private String groupId;
        private String artifactId;
        private String version;

        public BuiltArtifact(
                TargetRepository.Type type,
                String fileName,
                String md5,
                String sha256,
                long size,
                String url,
                String groupId,
                String artifactId,
                String version) {
            super(type, fileName, md5, sha256, size);
            this.url = url;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }

}
