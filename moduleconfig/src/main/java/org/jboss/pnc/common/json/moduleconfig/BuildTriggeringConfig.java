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

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.EnumMap;
import java.util.Map;

/**
 * Configuration controlling which kinds of builds (persistent and/or temporary) are allowed to be triggered, decided
 * per {@link BuildCategory}. The JSON top-level keys are build category names, each mapping to a
 * {@link CategoryConfig}, e.g.
 *
 * <pre>
 * {
 *     "&#64;module-config": "build-triggering-config",
 *     "STANDARD": { "persistent": true, "temporary": true }
 * }
 * </pre>
 *
 * A category that is not listed is permissive by default (both persistent and temporary builds are allowed), so
 * restrictive environments must list every category they want to gate.
 */
public class BuildTriggeringConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "build-triggering-config";

    private final Map<BuildCategory, CategoryConfig> categoryConfigs = new EnumMap<>(BuildCategory.class);

    @JsonAnySetter
    public void addCategoryConfig(String category, CategoryConfig categoryConfig) {
        categoryConfigs.put(BuildCategory.valueOf(category), categoryConfig);
    }

    public CategoryConfig getCategoryConfig(BuildCategory category) {
        return categoryConfigs.get(category);
    }

    /**
     * @return whether triggering persistent builds is allowed for the given category. Unconfigured categories are
     *         permissive.
     */
    public boolean isPersistentAllowed(BuildCategory category) {
        CategoryConfig categoryConfig = categoryConfigs.get(category);
        return categoryConfig == null || categoryConfig.isPersistent();
    }

    /**
     * @return whether triggering temporary builds is allowed for the given category. Unconfigured categories are
     *         permissive.
     */
    public boolean isTemporaryAllowed(BuildCategory category) {
        CategoryConfig categoryConfig = categoryConfigs.get(category);
        return categoryConfig == null || categoryConfig.isTemporary();
    }

    @Override
    public String toString() {
        return "BuildTriggeringConfig [categoryConfigs=" + categoryConfigs + "]";
    }

    public static class CategoryConfig {

        private final boolean persistent;

        private final boolean temporary;

        @JsonCreator
        public CategoryConfig(
                @JsonProperty("persistent") boolean persistent,
                @JsonProperty("temporary") boolean temporary) {
            this.persistent = persistent;
            this.temporary = temporary;
        }

        public boolean isPersistent() {
            return persistent;
        }

        public boolean isTemporary() {
            return temporary;
        }

        @Override
        public String toString() {
            return "CategoryConfig [persistent=" + persistent + ", temporary=" + temporary + "]";
        }
    }
}
