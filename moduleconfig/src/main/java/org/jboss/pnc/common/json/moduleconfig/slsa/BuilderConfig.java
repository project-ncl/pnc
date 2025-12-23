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
package org.jboss.pnc.common.json.moduleconfig.slsa;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.List;
import java.util.Optional;

public class BuilderConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "builder-config";

    public static final String REPLACE_TOKEN = "${buildId}";

    /**
     * Builder component id
     */
    private ProvenanceEntry id;

    /**
     * List of the component versions
     */
    private List<ProvenanceEntry> componentVersions;

    /**
     * Lits of by products
     */
    private List<ProvenanceEntry> byProducts;

    public BuilderConfig(
            @JsonProperty("id") ProvenanceEntry id,
            @JsonProperty("componentVersions") List<ProvenanceEntry> componentVersions,
            @JsonProperty("byProducts") List<ProvenanceEntry> byProducts) {
        this.id = id;
        this.componentVersions = componentVersions;
        this.byProducts = byProducts;
    }

    public ProvenanceEntry getId() {
        return id;
    }

    public List<ProvenanceEntry> getComponentVersions() {
        return componentVersions;
    }

    public List<ProvenanceEntry> getByProducts() {
        return byProducts;
    }

    public static Optional<ProvenanceEntry> findByName(List<ProvenanceEntry> components, String name) {

        if (components == null || name == null) {
            return Optional.empty();
        }

        return components.stream().filter(c -> name.equals(c.getProvenanceEntryName())).findFirst();
    }
}
