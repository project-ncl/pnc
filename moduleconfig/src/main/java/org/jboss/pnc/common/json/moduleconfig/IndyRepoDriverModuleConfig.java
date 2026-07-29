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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.Map;

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
