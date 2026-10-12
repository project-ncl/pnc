/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jboss.pnc.common.json.AbstractModuleConfig;
import org.jboss.pnc.common.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

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
