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
package org.jboss.pnc.rest.restmodel.bpm;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.rest.validation.validators.ScmUrl;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Set;

/**
 * When a new BC is created, the upstream repo is optionally
 * cloned into an internal mirror.
 * This is done when the scmExternal* fields are set.
 */
@XmlRootElement(name = "BpmBuildConfigurationCreation")
@ToString
public class BpmBuildConfigurationCreationRest implements Serializable {

    @NotNull
    @Pattern(regexp = "^[a-zA-Z0-9_.][a-zA-Z0-9_.-]*(?<!\\.git)$")
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private String description;

    @NotNull
    @Getter
    @Setter
    private String buildScript;

    @ScmUrl
    @Getter
    @Setter
    private String scmRepoURL;

    @Getter
    @Setter
    private String scmRevision;

    @ScmUrl
    @Getter
    @Setter
    private String scmExternalRepoURL;

    @Getter
    @Setter
    private String scmExternalRevision;

    @NotNull
    @Getter
    @Setter
    private Integer projectId;

    @NotNull
    @Getter
    @Setter
    private Integer buildEnvironmentId;

    @Getter
    @Setter
    private Set<Integer> dependencyIds;

    @Getter
    @Setter
    private Integer productVersionId;
}
