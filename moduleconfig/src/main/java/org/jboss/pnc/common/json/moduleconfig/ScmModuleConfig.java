/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

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

    public ScmModuleConfig(
            @JsonProperty("internalScmAuthority") String internalScmAuthority,
            @JsonProperty("secondaryInternalScmAuthority") String secondaryInternalScmAuthority,
            @JsonProperty("bannedScmAuthority") String bannedScmAuthority) {
        super();
        this.internalScmAuthority = StringUtils.stripEndingSlash(internalScmAuthority);
        this.secondaryInternalScmAuthority = StringUtils.stripEndingSlash(secondaryInternalScmAuthority);
        this.bannedScmAuthority = StringUtils.stripEndingSlash(bannedScmAuthority);
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

    @Override
    public String toString() {
        return "ScmModuleConfig [internalScmAuthority=" + internalScmAuthority + ",secondaryInternalScmAuthority="
                + secondaryInternalScmAuthority + "]";
    }
}
