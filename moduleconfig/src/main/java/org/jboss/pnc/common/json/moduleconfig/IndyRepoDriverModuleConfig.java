/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
     * Comma-separated list of path patterns to be ignored from showing in UI and to be part of a promotion. This
     * applies for both downloads and uploads.
     */
    @JsonProperty("ignored-path-patterns")
    private IgnoredPathPatterns ignoredPathPatterns;

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
     * Name of the target repo to which the build repo of a successful build should be promoted.
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private String buildPromotionTarget = "pnc-builds";

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

    public IgnoredPathPatterns getIgnoredPathPatterns() {
        return ignoredPathPatterns;
    }

    public void setIgnoredPathPatterns(IgnoredPathPatterns ignoredPathPatterns) {
        this.ignoredPathPatterns = ignoredPathPatterns;
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


    @ToString
    public static class InternalRepoPatterns {

        @Setter
        @JsonProperty("maven")
        @JsonInclude(Include.NON_EMPTY)
        protected List<String> maven;

        @Setter
        @JsonProperty("npm")
        @JsonInclude(Include.NON_EMPTY)
        protected List<String> npm;

        @Setter
        @JsonProperty("generic")
        @JsonInclude(Include.NON_EMPTY)
        protected List<String> generic;

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

        /**
         * Gets the list of Generic-http strings.
         * @return the list of Generic-http strings or empty list if no value is set (never {@code null})
         */
        public List<String> getGeneric() {
            return generic == null ? Collections.emptyList() : generic;
        }

        /**
         * Adds extra members to the list of Generic-http strings.
         * @param addition added strings
         */
        public void addGeneric(List<String> addition) {
            if (addition != null) {
                if (generic == null) {
                    generic = new ArrayList<>(addition);
                } else {
                    npm.addAll(addition);
                }
            }
        }

    }


    public static class PatternsList {

        @Getter
        private List<Pattern> patterns;

        private PatternsList(List<String> strings) {
            if (strings != null) {
                patterns = new ArrayList<>(strings.size());
                for (String string : strings) {
                    patterns.add(Pattern.compile(string));
                }
            }
        }

        private PatternsList(PatternsList patterns1, PatternsList patterns2) {
            this.patterns = new ArrayList<>();
            if (patterns1 != null) {
                patterns.addAll(patterns1.patterns);
            }
            if (patterns2 != null) {
                patterns.addAll(patterns2.patterns);
            }
        }

        public boolean matchesOne(String string) {
            if (patterns != null) {
                for (Pattern pattern : patterns) {
                    if (pattern.matcher(string).matches()) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @ToString
    public static class IgnoredPathPatterns {

        @JsonIgnore
        private PatternsList maven;

        @JsonIgnore
        private PatternsList npm;

        @JsonIgnore
        private PatternsList shared;


        @JsonProperty("maven")
        public void setMaven(List<String> strPatterns) {
            maven = new PatternsList(strPatterns);
        }

        @JsonProperty("npm")
        public void setNpm(List<String> strPatterns) {
            npm = new PatternsList(strPatterns);
        }

        @JsonProperty("_shared")
        public void setShared(List<String> strPatterns) {
            shared = new PatternsList(strPatterns);
        }

        @JsonIgnore
        public PatternsList getMaven() {
            return maven == null ? new PatternsList(Collections.emptyList()) : maven;
        }

        @JsonIgnore
        public PatternsList getMavenWithShared() {
            return new PatternsList(maven, shared);
        }

        public PatternsList getNpm() {
            return npm == null ? new PatternsList(Collections.emptyList()) : npm;
        }

        @JsonIgnore
        public PatternsList getNpmWithShared() {
            return new PatternsList(npm, shared);
        }

        /**
         * Gets the list of ignored path patterns shared among all package types.
         * @return the list of shared strings or empty list if no value is set (never {@code null})
         */
        @JsonIgnore
        public PatternsList getShared() {
            return shared == null ? new PatternsList(Collections.emptyList()) : shared;
        }

        private List<String> genStringList(PatternsList patternsList) {
            if (patternsList != null && patternsList.patterns != null) {
                return patternsList.patterns.stream().map(p -> p.pattern()).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }

        @JsonProperty("maven")
        @JsonInclude(Include.NON_EMPTY)
        public List<String> getMavenStrings() {
            return genStringList(maven);
        }

        @JsonProperty("npm")
        @JsonInclude(Include.NON_EMPTY)
        public List<String> getNpmStrings() {
            return genStringList(npm);
        }

        @JsonProperty("_shared")
        @JsonInclude(Include.NON_EMPTY)
        public List<String> getSharedStrings() {
            return genStringList(shared);
        }

    }

}
