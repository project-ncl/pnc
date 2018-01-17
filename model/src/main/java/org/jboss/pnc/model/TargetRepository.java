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

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Contains information related to a repository of build artifacts (i.e. Maven, NPM, etc)
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = { "identifier", "repositoryPath" }) )
public class TargetRepository implements GenericEntity<Integer> {

    private static final long serialVersionUID = 1L;

    public static final String SEQUENCE_NAME = "target_repository_repo_id_seq";

    @Id
    @Getter
    @Setter
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    /**
     * Flag that the repository is temporary.
     */
    @Getter
    @Setter
    @NotNull
    @Column(updatable=false)
    private Boolean temporaryRepo;

    /**
     * Identifier to link repository configurations (eg. hostname)
     */
    @Getter
    @Setter
    @NotNull
    @Size(max=255)
    @Column(updatable=false)
    private String identifier;

    /**
     * Path that need to be appended to the hostname
     * eg. "ga" for https://maven.repository.redhat.com/ga/
     * or "maven2" for https://repo1.maven.org/maven2/
     * or "" (empty string) when the repository content starts at root
     */
    @Getter
    @Setter
    @NotNull
    @Size(max=255)
    @Column(updatable=false)
    private String repositoryPath;


    /**
     * The type of repository which hosts this artifact (Maven, NPM, etc).  This field determines
     * the format of the identifier string.
     */
    @Getter
    @Setter
    @NotNull
    @Column(updatable=false)
    @Enumerated(EnumType.STRING)
    private TargetRepository.Type repositoryType;

    @OneToMany(mappedBy = "targetRepository")
    private Set<Artifact> artifacts;

    /**
     * Types of artifact repositories
     *
     * Types *_TEMPORAL are repository groups for temporal build (Snapshots / Pull Request)
     */
    public enum Type {
        /**
         * Maven artifact repository such as Maven central (http://central.maven.org/maven2/)
         */
        MAVEN,
        MAVEN_TEMPORARY,

        /**
         * Node.js package repository such as https://registry.npmjs.org/
         */
        NPM,

        /**
         * CocoaPod repository for managing Swift and Objective-C Cocoa dependencies
         */
        COCOA_POD,

        /**
         * Generic HTTP proxy that captures artifacts with an unsupported, or no specific, repository type.
         */
        GENERIC_PROXY
    }

    /**
     * List of trusted repository URLs.  These repositories contain only artifacts which have been built
     * from source in a trusted build environment.
     */
    public static final String [] TRUSTED_REPOSITORY_URLS = {
            "http://mead/eap6",
            "http://mead/eap7"
    };

    /**
     * Check if a given artifact originates from a trusted source.  Compares the given artifactOriginUrl
     * to the list of trusted repositories.
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

}
