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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@ToString
public class IndyRepoDriverModuleConfig extends AbstractModuleConfig{

    public static final String MODULE_NAME = "indy-repo-driver";

    /**
     * Base url to Indy repository manager
     */
    private String baseUrl;

    /**
     * Comma-separated lists of repository name patterns to use when considering whether a remote repository
     * represents an internal build (from a trusted build system, for instance). The structure contains a list of
     * patterns for every supported package type.
     */
    @JsonProperty("internal-repo-patterns")
    private InternalRepoPatterns internalRepoPatterns;

    /**
     * Comma-separated list of path suffixes to be ignored from showing in UI and to be part of a promotion. This
     * applies for both downloads and uploads.
     */
    @JsonProperty("ignored-path-suffixes")
    private IgnoredPathSuffixes ignoredPathSuffixes;

    /**
     * Internal network (cloud) maven repository path
     */
    @JsonProperty
    private String internalRepositoryMvnPath;

    /**
     * External network maven repository path
     */
    @JsonProperty
    private String externalRepositoryMvnPath;

    /**
     * Internal network (cloud) NPM repository path
     */
    @JsonProperty
    private String internalRepositoryNpmPath;

    /**
     * External network NPM repository path
     */
    @JsonProperty
    private String externalRepositoryNpmPath;

    /**
     * Request timeout for the whole client in seconds
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private Integer defaultRequestTimeout = 600;

    /**
     * Should be the build repositories configured to allow snapshots?
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private Boolean buildRepositoryAllowSnapshots = false;

    /**
     * Name of the group to which the build repo of a successful build should be promoted.
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private String buildPromotionGroup = "pnc-builds";

    /**
     * Name of the group to which the build repo of a successful TEMPORARY build should be promoted.
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private String tempBuildPromotionGroup = "temporary-builds";

    public IndyRepoDriverModuleConfig(@JsonProperty("base-url") String baseUrl){
        this.baseUrl = baseUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public InternalRepoPatterns getInternalRepoPatterns() {
        return internalRepoPatterns;
    }

    public void setInternalRepoPatterns(InternalRepoPatterns internalRepoPatterns) {
        this.internalRepoPatterns = internalRepoPatterns;
    }

    public IgnoredPathSuffixes getIgnoredPathSuffixes() {
        return ignoredPathSuffixes;
    }

    public void setIgnoredPathSuffixes(IgnoredPathSuffixes ignoredPathSuffixes) {
        this.ignoredPathSuffixes = ignoredPathSuffixes;
    }

    public String getInternalRepositoryMvnPath() {
        return internalRepositoryMvnPath;
    }

    public void setInternalRepositoryMvnPath(String internalRepositoryMvnPath) {
        this.internalRepositoryMvnPath = internalRepositoryMvnPath;
    }

    public String getExternalRepositoryMvnPath() {
        return externalRepositoryMvnPath;
    }

    public void setExternalRepositoryMvnPath(String externalRepositoryMvnPath) {
        this.externalRepositoryMvnPath = externalRepositoryMvnPath;
    }

    public String getInternalRepositoryNpmPath() {
        return internalRepositoryNpmPath;
    }

    public void setInternalRepositoryNpmPath(String internalRepositoryNpmPath) {
        this.internalRepositoryNpmPath = internalRepositoryNpmPath;
    }

    public String getExternalRepositoryNpmPath() {
        return externalRepositoryNpmPath;
    }

    public void setExternalRepositoryNpmPath(String externalRepositoryNpmPath) {
        this.externalRepositoryNpmPath = externalRepositoryNpmPath;
    }


    private static class PackageTypeSpecificStringLists {

        @Setter
        @JsonProperty("maven")
        @JsonInclude(Include.NON_EMPTY)
        protected List<String> maven;

        @Setter
        @JsonProperty("npm")
        @JsonInclude(Include.NON_EMPTY)
        protected List<String> npm;

        /**
         * Gets the list of Maven strings.
         * @return the list of Maven strings or empty list if no value is set (never {@code null})
         */
        public List<String> getMaven() {
            return maven == null ? Collections.emptyList() : maven;
        }

        /**
         * Adds extra members to the list of Maven strings.
         * @param addition added strings
         */
        public void addMaven(List<String> addition) {
            addExtraMembers(addition);
        }

        private void addExtraMembers(List<String> addition) {
            if (addition != null) {
                if (maven == null) {
                    maven = new ArrayList<>(addition);
                } else {
                    maven.addAll(addition);
                }
            }
        }

        /**
         * Gets the list of NPM strings.
         * @return the list of NPM strings or empty list if no value is set (never {@code null})
         */
        public List<String> getNpm() {
            return npm == null ? Collections.emptyList() : npm;
        }

        /**
         * Adds extra members to the list of NPM strings.
         * @param addition added strings
         */
        public void addNpm(List<String> addition) {
            if (addition != null) {
                if (npm == null) {
                    npm = new ArrayList<>(addition);
                } else {
                    npm.addAll(addition);
                }
            }
        }

    }


    @ToString
    public static class InternalRepoPatterns extends PackageTypeSpecificStringLists {

    }


    @ToString
    public static class IgnoredPathSuffixes extends PackageTypeSpecificStringLists {

        @Setter
        @JsonProperty("_shared")
        @JsonInclude(Include.NON_EMPTY)
        private List<String> shared;

        @JsonIgnore
        public List<String> getMavenWithShared() {
            List<String> mavenWithShared = (maven == null ? new ArrayList<>() : new ArrayList<>(maven));
            mavenWithShared.addAll(getShared());
            return mavenWithShared;
        }

        @JsonIgnore
        public List<String> getNpmWithShared() {
            List<String> npmWithShared = (npm == null ? new ArrayList<>() : new ArrayList<>(npm));
            npmWithShared.addAll(getShared());
            return npmWithShared;
        }

        /**
         * Gets the list of ignored path suffixes shared among all package types.
         * @return the list of shared strings or empty list if no value is set (never {@code null})
         */
        public List<String> getShared() {
            return shared == null ? Collections.emptyList() : shared;
        }

        /**
         * Adds extra members to the list of shared strings.
         * @param addition added strings
         */
        public void addShared(List<String> addition) {
            if (addition != null) {
                if (shared == null) {
                    shared = new ArrayList<>(addition);
                } else {
                    shared.addAll(addition);
                }
            }
        }

    }

}
