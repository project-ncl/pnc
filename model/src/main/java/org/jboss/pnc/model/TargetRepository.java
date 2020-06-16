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

import java.util.Objects;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jboss.pnc.enums.RepositoryType;

/**
 * Contains information related to a repository of build artifacts (i.e. Maven, NPM, etc)
 */
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_targetrepo_identifier_repopath",
                columnNames = { "identifier", "repositoryPath" }))
public class TargetRepository implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String SEQUENCE_NAME = "target_repository_repo_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Flag that the repository is temporary.
     */
    @NotNull
    @Column(updatable = false)
    private Boolean temporaryRepo;

    /**
     * Identifier to link repository configurations (eg. hostname)
     */
    @NotNull
    @Size(max = 255)
    @Column(updatable = false)
    private String identifier;

    /**
     * Path that need to be appended to the hostname eg. "ga" for https://maven.repository.redhat.com/ga/ or "maven2"
     * for https://repo1.maven.org/maven2/ or "" (empty string) when the repository content starts at root
     */
    @NotNull
    @Size(max = 255)
    @Column(updatable = false)
    private String repositoryPath;

    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc). This field determines the format of the
     * identifier string.
     */
    @NotNull
    @Column(updatable = false)
    @Enumerated(EnumType.STRING)
    private RepositoryType repositoryType;

    @OneToMany(mappedBy = "targetRepository")
    private Set<Artifact> artifacts;

    public TargetRepository() {
    }

    private TargetRepository(Builder builder) {
        setId(builder.id);
        setTemporaryRepo(builder.temporaryRepo);
        setIdentifier(builder.identifier);
        setRepositoryPath(builder.repositoryPath);
        setRepositoryType(builder.repositoryType);
        setArtifacts(builder.artifacts);
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getTemporaryRepo() {
        return temporaryRepo;
    }

    public void setTemporaryRepo(Boolean temporaryRepo) {
        this.temporaryRepo = temporaryRepo;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getRepositoryPath() {
        return repositoryPath;
    }

    public void setRepositoryPath(String repositoryPath) {
        this.repositoryPath = repositoryPath;
    }

    public RepositoryType getRepositoryType() {
        return repositoryType;
    }

    public void setRepositoryType(RepositoryType repositoryType) {
        this.repositoryType = repositoryType;
    }

    public Set<Artifact> getArtifacts() {
        return artifacts;
    }

    public void setArtifacts(Set<Artifact> artifacts) {
        this.artifacts = artifacts;
    }

    @Transient
    public IdentifierPath getIdentifierPath() {
        return new IdentifierPath(identifier, repositoryPath);
    }

    @Override
    public String toString() {
        return "TargetRepository{" + "id=" + id + ", temporaryRepo=" + temporaryRepo + ", identifier='" + identifier
                + '\'' + ", repositoryPath='" + repositoryPath + '\'' + ", repositoryType=" + repositoryType + '}';
    }

    /**
     * List of trusted repository URLs. These repositories contain only artifacts which have been built from source in a
     * trusted build environment.
     */
    public static final String[] TRUSTED_REPOSITORY_URLS = { "http://mead/eap6", "http://mead/eap7" };

    /**
     * Check if a given artifact originates from a trusted source. Compares the given artifactOriginUrl to the list of
     * trusted repositories.
     *
     * @param artifactOriginUrl The URL from which the artifact was downloaded
     * @param targetRepository
     * @return true if the artifact url came from a trusted repo, false otherwise
     */
    static boolean isTrusted(String artifactOriginUrl, TargetRepository targetRepository) {
        if (targetRepository.temporaryRepo) {
            return false;
        }
        if (artifactOriginUrl == null || artifactOriginUrl.isEmpty()) {
            return false;
        }
        for (String trustedRepoUrl : TRUSTED_REPOSITORY_URLS) {
            if (artifactOriginUrl.startsWith(trustedRepoUrl)) {
                return true;
            }
        }
        return false;
    }

    public static final class Builder {

        private Integer id;

        private Boolean temporaryRepo;

        private String identifier;

        private String repositoryPath;

        private RepositoryType repositoryType;

        private Set<Artifact> artifacts;

        private Builder() {
        }

        public Builder id(Integer val) {
            id = val;
            return this;
        }

        public Builder temporaryRepo(Boolean val) {
            temporaryRepo = val;
            return this;
        }

        public Builder identifier(String val) {
            identifier = val;
            return this;
        }

        public Builder repositoryPath(String val) {
            repositoryPath = val;
            return this;
        }

        public Builder repositoryType(RepositoryType val) {
            repositoryType = val;
            return this;
        }

        public Builder artifacts(Set<Artifact> val) {
            artifacts = val;
            return this;
        }

        public TargetRepository build() {
            return new TargetRepository(this);
        }
    }

    public static class IdentifierPath {

        public static final String TO_STRING_DELIMITER = "$$";

        private final String identifier;
        private final String repositoryPath;

        public IdentifierPath(String identifier, String repositoryPath) {
            if (identifier == null) {
                throw new NullPointerException("Identifier is null.");
            }
            if (repositoryPath == null) {
                throw new NullPointerException("RepositoryPath is null.");
            }
            this.identifier = identifier;
            this.repositoryPath = repositoryPath;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getRepositoryPath() {
            return repositoryPath;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof IdentifierPath)) {
                return false;
            }
            IdentifierPath that = (IdentifierPath) o;
            return identifier.equals(that.identifier) && repositoryPath.equals(that.repositoryPath);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, repositoryPath);
        }

        @Override
        public String toString() {
            return identifier + TO_STRING_DELIMITER + repositoryPath;
        }
    }
}
