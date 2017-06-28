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
package org.jboss.pnc.rest.restmodel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;
import org.jboss.pnc.rest.validation.validators.ScmUrl;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * The REST entity that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@EqualsAndHashCode
@ToString
@XmlRootElement(name = "RepositoryConfiguration")
public class RepositoryConfigurationRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @Getter
    @Setter
    @NotBlank(groups = {WhenUpdating.class, WhenCreatingNew.class})
    @ScmUrl(groups = {WhenUpdating.class, WhenCreatingNew.class} )
    private String internalScmRepoUrl;

    @Getter
    @Setter
    @ScmUrl(groups = {WhenUpdating.class, WhenCreatingNew.class} )
    private String externalScmRepoUrl;

    @Getter
    @Setter
    private boolean preBuildSyncEnabled;

    public RepositoryConfigurationRest() {
    }

    public RepositoryConfigurationRest(RepositoryConfiguration repositoryConfiguration) {
        this.id = repositoryConfiguration.getId();
        this.internalScmRepoUrl = repositoryConfiguration.getInternalScmRepoUrl();
        this.externalScmRepoUrl = repositoryConfiguration.getExternalScmRepoUrl();
        this.preBuildSyncEnabled = repositoryConfiguration.isPreBuildSyncEnabled();
    }

    /**
     * Gets Id.
     *
     * @return Id.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets id.
     *
     * @param id id.
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public RepositoryConfiguration.Builder toDBEntityBuilder() {
        RepositoryConfiguration.Builder builder = RepositoryConfiguration.Builder.newBuilder()
                .id(id)
                .internalScmRepoUrl(internalScmRepoUrl)
                .externalScmRepoUrl(externalScmRepoUrl)
                .preBuildSyncEnabled(preBuildSyncEnabled);
        return builder;
    }
}
