/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.pnc.api.enums.BuildCategory;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
 * A category that is not listed is forbidden by default (neither persistent nor temporary builds are allowed), so
 * permissive environments must list every category they want to enable.
 * <p>
 * Each category may additionally restrict triggering of <b>persistent</b> builds to Build Configurations belonging to
 * specific projects, matched by exact project name. Temporary builds are not restricted by project.
 *
 * <pre>
 * "LIGHTWELL": { "persistent": true, "temporary": true, "allowedProjects": [ "Bouncycastle" ] }
 * </pre>
 *
 * If {@code allowedProjects} is omitted or empty, persistent builds of any project are allowed. Otherwise only the
 * listed projects may be built persistently.
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
     *         forbidden.
     */
    public boolean isPersistentBuildingAllowed(BuildCategory category) {
        return isPersistentBuildingAllowed(categoryConfigs.get(category));
    }

    /**
     * @return whether triggering temporary builds is allowed for the given category. Unconfigured categories are
     *         forbidden.
     */
    public boolean isTemporaryBuildingAllowed(BuildCategory category) {
        return isTemporaryBuildingAllowed(categoryConfigs.get(category));
    }

    /**
     * @return whether triggering persistent builds is allowed for the given category config. A {@code null} config
     *         (unconfigured category) is forbidden.
     */
    public static boolean isPersistentBuildingAllowed(CategoryConfig categoryConfig) {
        return categoryConfig != null && categoryConfig.isPersistent();
    }

    /**
     * @return whether triggering temporary builds is allowed for the given category config. A {@code null} config
     *         (unconfigured category) is forbidden.
     */
    public static boolean isTemporaryBuildingAllowed(CategoryConfig categoryConfig) {
        return categoryConfig != null && categoryConfig.isTemporary();
    }

    /**
     * @return whether triggering persistent builds of Build Configurations belonging to the given project is allowed
     *         for the given category. Unconfigured categories are forbidden; categories with an omitted or empty
     *         {@code allowedProjects} list allow any project.
     */
    public boolean isProjectAllowed(BuildCategory category, String projectName) {
        return isProjectAllowed(categoryConfigs.get(category), projectName);
    }

    /**
     * @return whether triggering persistent builds of Build Configurations belonging to the given project is allowed
     *         for the given category config. A {@code null} config (unconfigured category) is forbidden; a config with
     *         an omitted or
     *         empty {@code allowedProjects} list allows any project.
     */
    public static boolean isProjectAllowed(CategoryConfig categoryConfig, String projectName) {
        if (categoryConfig == null) {
            return false;
        }
        Set<String> allowedProjects = categoryConfig.getAllowedProjects();
        return allowedProjects.isEmpty() || (projectName != null && allowedProjects.contains(projectName));
    }

    @Override
    public String toString() {
        return "BuildTriggeringConfig [categoryConfigs=" + categoryConfigs + "]";
    }

    public static class CategoryConfig {

        private final boolean persistent;

        private final boolean temporary;

        /**
         * Names of projects whose Build Configurations may be built persistently; empty means no restriction.
         */
        private final Set<String> allowedProjects;

        @JsonCreator
        public CategoryConfig(
                @JsonProperty("persistent") boolean persistent,
                @JsonProperty("temporary") boolean temporary,
                @JsonProperty("allowedProjects") Set<String> allowedProjects) {
            this.persistent = persistent;
            this.temporary = temporary;
            this.allowedProjects = allowedProjects == null ? Collections.emptySet()
                    : Collections.unmodifiableSet(new LinkedHashSet<>(allowedProjects));
        }

        public boolean isPersistent() {
            return persistent;
        }

        public boolean isTemporary() {
            return temporary;
        }

        /**
         * @return names of projects whose Build Configurations may be built persistently; empty if not restricted
         */
        public Set<String> getAllowedProjects() {
            return allowedProjects;
        }

        @Override
        public String toString() {
            return "CategoryConfig [persistent=" + persistent + ", temporary=" + temporary + ", allowedProjects="
                    + allowedProjects + "]";
        }
    }
}
