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
package org.jboss.pnc.common.json.moduleconfig;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

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
public class IndyRepoDriverModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "indy-repo-driver";

    /**
     * Lists of dependency repository store key patterns to use when considering whether a repository represents an
     * internal build (from a trusted build system, for instance). The structure contains a list of patterns for every
     * supported package type.
     */
    @JsonProperty("ignored-repo-patterns")
    private List<String> ignoredRepoPatterns;

    /**
     * Lists of path patterns to be excluded from promotion and showing in UI. This applies for both downloads and
     * uploads.
     */
    @JsonProperty("ignored-path-patterns")
    private IgnoredPathPatterns ignoredPathPatterns;

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
     * Name of the target repo to which the build repo of a successful TEMPORARY build should be promoted.
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private String tempBuildPromotionTarget = "temporary-builds";

    /**
     * Name of metadata-key to set in the Indy build group to activate / de-activate brew pull
     */
    @Getter
    @Setter
    @JsonProperty("brew-pull-active-metadata-key")
    private String brewPullActiveMetadataKey;

    @Getter
    @Setter
    @JsonProperty("indy-sidecar-enabled")
    private boolean indySidecarEnabled;

    @Getter
    @Setter
    @JsonProperty("indy-sidecar-url")
    private String indySidecarUrl;

    public IndyRepoDriverModuleConfig() {
    }

    public List<String> getIgnoredRepoPatterns() {
        return ignoredRepoPatterns;
    }

    public void setIgnoredRepoPatterns(List<String> internalRepoPatterns) {
        this.ignoredRepoPatterns = internalRepoPatterns;
    }

    public IgnoredPathPatterns getIgnoredPathPatterns() {
        return ignoredPathPatterns;
    }

    public void setIgnoredPathPatterns(IgnoredPathPatterns ignoredPathPatterns) {
        this.ignoredPathPatterns = ignoredPathPatterns;
    }

    public static class PatternsList {

        @JsonIgnore
        private List<Pattern> patterns;

        public List<Pattern> getPatterns() {
            return patterns == null ? Collections.emptyList() : patterns;
        }

        public PatternsList(List<String> strings) {
            if (strings != null) {
                patterns = new ArrayList<>(strings.size());
                for (String string : strings) {
                    patterns.add(Pattern.compile(string));
                }
            }
        }

    }

    @ToString
    public static class IgnoredPatterns {

        @JsonIgnore
        private PatternsList generic;

        @JsonIgnore
        private PatternsList maven;

        @JsonIgnore
        private PatternsList npm;

        @JsonProperty("generic")
        public void setGeneric(List<String> strPatterns) {
            generic = new PatternsList(strPatterns);
        }

        @JsonProperty("maven")
        public void setMaven(List<String> strPatterns) {
            maven = new PatternsList(strPatterns);
        }

        @JsonProperty("npm")
        public void setNpm(List<String> strPatterns) {
            npm = new PatternsList(strPatterns);
        }

        @JsonIgnore
        public PatternsList getGeneric() {
            return getPatternsList(generic);
        }

        @JsonIgnore
        public PatternsList getMaven() {
            return getPatternsList(maven);
        }

        @JsonIgnore
        public PatternsList getNpm() {
            return getPatternsList(npm);
        }

        private List<String> genStringList(PatternsList patternsList) {
            if (patternsList != null && patternsList.patterns != null) {
                return patternsList.patterns.stream().map(Pattern::pattern).collect(Collectors.toList());
            } else {
                return Collections.emptyList();
            }
        }

        @JsonProperty("generic")
        @JsonInclude(Include.NON_EMPTY)
        public List<String> getGenericStrings() {
            return genStringList(generic);
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

        /**
         * Safely gets patterns list. Ensures that output is never null.
         *
         * @param list the input list
         * @return an empty list in case of input list is null, otherwise the input list
         */
        @JsonIgnore
        private PatternsList getPatternsList(PatternsList list) {
            return list == null ? new PatternsList(Collections.emptyList()) : list;
        }

    }

    @ToString
    public static class IgnoredPathPatterns {
        @Setter
        private IgnoredPatterns promotion;
        @Setter
        private IgnoredPatterns data;

        public IgnoredPatterns getPromotion() {
            return promotion == null ? new IgnoredPatterns() : promotion;
        }

        public IgnoredPatterns getData() {
            return data == null ? new IgnoredPatterns() : data;
        }
    }
}
