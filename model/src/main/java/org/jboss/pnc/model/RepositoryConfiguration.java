/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.pnc.common.util.StringUtils;
import org.jboss.pnc.common.util.UrlUtils;

/**
 * The JPA entity class that contains configuration of the SCM repositories.
 *
 * @author Jakub Bartecek
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_repositoryconfiguration_externalurl", columnNames = { "externalurl" }),
                @UniqueConstraint(
                        name = "uk_repositoryconfiguration_externalurlnormalized",
                        columnNames = { "externalurlnormalized" }),
                @UniqueConstraint(name = "uk_repositoryconfiguration_internalurl", columnNames = { "internalurl" }),
                @UniqueConstraint(
                        name = "uk_repositoryconfiguration_internalurlnormalized",
                        columnNames = { "internalurlnormalized" }) })
public class RepositoryConfiguration implements GenericEntity<Integer> {

    private static final long serialVersionUID = 4248038054068607536L;

    private static final String SEQ_NAME = "repository_configuration_id_seq";

    @PrePersist
    protected void onCreate() {
        setNormalizedUrls();
    }

    @PreUpdate
    protected void onUpdate() {
        setNormalizedUrls();
    }

    private void setNormalizedUrls() {
        internalUrlNormalized = StringUtils.stripSuffix(UrlUtils.keepHostAndPathOnly(internalUrl), ".git");
        externalUrlNormalized = externalUrl == null ? null
                : StringUtils.stripSuffix(UrlUtils.keepHostAndPathOnly(externalUrl), ".git");
    }

    @Id
    @SequenceGenerator(name = SEQ_NAME, sequenceName = SEQ_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQ_NAME)
    private Integer id;

    /**
     * URL to the internal SCM repository, which is the main repository used for the builds. New commits can be added to
     * this repository, during the pre-build steps of the build process.
     */
    @Size(max = 255)
    @Column(unique = true, nullable = false, updatable = false)
    private String internalUrl;

    /**
     * Normalized version of scm url to query against. Normalized version is without the protocol and .git extension.
     */
    @Size(max = 255)
    @Column(unique = true, updatable = false)
    private String internalUrlNormalized;

    /**
     * URL to the upstream SCM repository.
     */
    @Size(max = 255)
    @Column(unique = true)
    private String externalUrl;

    /**
     * Normalized version of scm url to query against. Normalized version is without the protocol and .git extension.
     */
    @Size(max = 255)
    @Column(unique = true)
    private String externalUrlNormalized;

    /**
     * Declares whether the pre-build repository synchronization should happen or not.
     */
    private boolean preBuildSyncEnabled = true;

    @OneToMany(mappedBy = "repositoryConfiguration")
    private Set<BuildConfiguration> buildConfigurations = new HashSet<>();

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getInternalUrl() {
        return internalUrl;
    }

    public void setInternalUrl(String internalUrl) {
        this.internalUrl = internalUrl;
    }

    public String getInternalUrlNormalized() {
        return internalUrlNormalized;
    }

    public void setInternalUrlNormalized(String internalUrlNormalized) {
        this.internalUrlNormalized = internalUrlNormalized;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        if (externalUrl != null && externalUrl.isEmpty()) {
            externalUrl = null;
        }
        this.externalUrl = externalUrl;
    }

    public String getExternalUrlNormalized() {
        return externalUrlNormalized;
    }

    public void setExternalUrlNormalized(String externalUrlNormalized) {
        this.externalUrlNormalized = externalUrlNormalized;
    }

    public boolean isPreBuildSyncEnabled() {
        return preBuildSyncEnabled;
    }

    public void setPreBuildSyncEnabled(boolean preBuildSyncEnabled) {
        this.preBuildSyncEnabled = preBuildSyncEnabled;
    }

    public Set<BuildConfiguration> getBuildConfigurations() {
        return buildConfigurations;
    }

    public void setBuildConfigurations(Set<BuildConfiguration> buildConfigurations) {
        this.buildConfigurations = buildConfigurations;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof RepositoryConfiguration))
            return false;

        RepositoryConfiguration that = (RepositoryConfiguration) o;

        return getId() != null ? getId().equals(that.getId()) : that.getId() == null;
    }

    @Override
    public int hashCode() {
        return getId() != null ? getId().hashCode() : 0;
    }

    @Override
    public String toString() {
        return "RepositoryConfiguration{" + "id=" + id + ", internalUrl='" + internalUrl + '\''
                + ", internalUrlNormalized='" + internalUrlNormalized + '\'' + ", externalUrl='" + externalUrl + '\''
                + ", externalUrlNormalized='" + externalUrlNormalized + '\'' + ", preBuildSyncEnabled="
                + preBuildSyncEnabled + ", buildConfigurations=" + buildConfigurations + '}';
    }

    public static class Builder {
        private Integer id;

        private String internalUrl;

        private String externalUrl;

        private boolean preBuildSyncEnabled = true;

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
            repositoryConfiguration.setInternalUrl(internalUrl);
            repositoryConfiguration.setExternalUrl(externalUrl);
            repositoryConfiguration.setPreBuildSyncEnabled(preBuildSyncEnabled);
            repositoryConfiguration.setBuildConfigurations(buildConfigurations);
            return repositoryConfiguration;
        }

        public Builder internalUrl(String internalUrl) {
            this.internalUrl = internalUrl;
            return this;
        }

        public Builder externalUrl(String externalUrl) {
            this.externalUrl = externalUrl;
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
