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
package org.jboss.pnc.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * The JPA entity class that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@Entity
public class RepositoryConfiguration implements GenericEntity<Integer> {

    private static final long serialVersionUID = 4248038054068607536L;

    private static final String SEQ_NAME = "repository_configuration_id_seq";

    @Id
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    private Integer id;

    /**
     * Repository URL containing project to be build.
     * This URL *MUST* be read/write.
     */
    @Size(max = 255)
    @Column(unique = true, nullable = false, updatable = false)
    @Getter
    @Setter
    private String internalScmRepoUrl;

    /**
     * URL of an upstream SCM repository.
     * This URL SHOULD be read-only, since push access is not needed
     */
    @Size(max = 255)
    @Getter
    @Setter
    private String externalScmRepoUrl;

    /**
     * Declares whether the pre-build repository synchronization should happen or not.
     */
    @Getter
    @Setter
    private boolean preBuildSyncEnabled = false;

    @OneToMany(mappedBy = "repositoryConfiguration")
    @Getter
    @Setter
    private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepositoryConfiguration)) return false;

        RepositoryConfiguration that = (RepositoryConfiguration) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    public static class Builder {
        private Integer id;

        private String internalScmRepoUrl;

        private String externalScmRepoUrl;

        private boolean preBuildSyncEnabled = false;

        private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

        public static Builder newBuilder() {
            return new Builder();
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public RepositoryConfiguration build() {
            RepositoryConfiguration repositoryConfiguration = new RepositoryConfiguration();
            repositoryConfiguration.setId(id);
            repositoryConfiguration.setInternalScmRepoUrl(internalScmRepoUrl);
            repositoryConfiguration.setExternalScmRepoUrl(externalScmRepoUrl);
            repositoryConfiguration.setPreBuildSyncEnabled(preBuildSyncEnabled);
            repositoryConfiguration.setBuildConfigurations(buildConfigurations);
            return repositoryConfiguration;
        }

        public Builder internalScmRepoUrl(String internalScmRepoUrl) {
            this.internalScmRepoUrl = internalScmRepoUrl;
            return this;
        }

        public Builder externalScmRepoUrl(String externalScmRepoUrl) {
            this.externalScmRepoUrl = externalScmRepoUrl;
            return this;
        }

        public Builder preBuildSyncEnabled(boolean preBuildSyncEnabled) {
            this.preBuildSyncEnabled = preBuildSyncEnabled;
            return this;
        }

        public Builder buildConfigurations(Set<BuildConfiguration> buildConfigurations) {
            this.buildConfigurations = buildConfigurations;
            return this;
        }
    }
}
