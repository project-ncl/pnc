/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2022 Red Hat, Inc., and individual contributors
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.util.StringUtils;

/**
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 12/6/16 Time: 12:47 PM
 */
public class ScmModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "scm-config";

    private String internalScmAuthority;

    private String secondaryInternalScmAuthority;

    private String bannedScmAuthority;

    /**
     * Whitelist of external SCM groups/subgroups/organizations that are allowed even when they match
     * {@link #bannedScmAuthority}. Each entry is a {@code host/group[/subgroup...]} prefix (scheme and {@code .git}
     * suffix optional); it matches a repository URL under that path, segment-aware (so
     * {@code gitlab.example.com/lw/lw-builds} allows {@code .../lw-builds/foo} but not {@code .../lw-buildsX}).
     * Empty/absent = no exceptions (previous behavior).
     */
    private List<String> allowedScmGroups;

    public ScmModuleConfig(
            @JsonProperty("internalScmAuthority") String internalScmAuthority,
            @JsonProperty("secondaryInternalScmAuthority") String secondaryInternalScmAuthority,
            @JsonProperty("bannedScmAuthority") String bannedScmAuthority,
            @JsonProperty("allowedScmGroups") List<String> allowedScmGroups) {
        super();
        this.internalScmAuthority = StringUtils.stripEndingSlash(internalScmAuthority);
        this.secondaryInternalScmAuthority = StringUtils.stripEndingSlash(secondaryInternalScmAuthority);
        this.bannedScmAuthority = StringUtils.stripEndingSlash(bannedScmAuthority);
        this.allowedScmGroups = allowedScmGroups == null ? Collections.emptyList()
                : allowedScmGroups.stream()
                        .filter(g -> g != null && !g.isBlank())
                        .map(StringUtils::stripEndingSlash)
                        .collect(Collectors.toUnmodifiableList());
    }

    public String getInternalScmAuthority() {
        return internalScmAuthority;
    }

    public String getSecondaryInternalScmAuthority() {
        return secondaryInternalScmAuthority;
    }

    public String getBannedScmAuthority() {
        return bannedScmAuthority;
    }

    /** Whitelist of external SCM group prefixes allowed despite {@link #getBannedScmAuthority()}. */
    public List<String> getAllowedScmGroups() {
        return allowedScmGroups;
    }

    @Override
    public String toString() {
        return "ScmModuleConfig [internalScmAuthority=" + internalScmAuthority + ",secondaryInternalScmAuthority="
                + secondaryInternalScmAuthority + "]";
    }
}
