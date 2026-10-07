/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.util.Map;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class IndyRepoDriverModuleConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "indy-repo-driver";

    /**
     * Request timeout for the whole client in seconds
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private Integer defaultRequestTimeout = 600;

    /**
     * Mapping of {@code BuildCategory} name to the Indy hosted repo used as the temp build promotion target. Keys are
     * build category names (e.g. {@code "STANDARD"}, {@code "SERVICE"}).
     */
    @Getter
    @Setter
    @JsonProperty(required = false)
    private Map<String, String> tempBuildPromotionTargets = Map.of();

    public String getTempBuildPromotionTarget(String buildCategory) {
        if (buildCategory != null && tempBuildPromotionTargets != null) {
            return tempBuildPromotionTargets.get(buildCategory);
        }
        return null;
    }

}
