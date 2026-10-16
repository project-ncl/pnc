/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig.slsa;

import java.util.List;
import java.util.Optional;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BuilderConfig extends AbstractModuleConfig {

    public static final String MODULE_NAME = "builder-config";

    public static final String REPLACE_TOKEN = "${buildId}";

    /**
     * Defines the strategy used to resolve a provenance entry value.
     *
     * <p>
     * A {@code ResolverMethod} determines how a value should be produced:
     * </p>
     * <ul>
     * <li>{@link #INVOKE} – The value is obtained by invoking an external endpoint (for example, performing an HTTP
     * request).</li>
     * <li>{@link #REPLACE} – The value is produced by performing a string replacement (for example, substituting
     * placeholders with build-specific values).</li>
     * </ul>
     *
     */
    public enum ResolverMethod {
        INVOKE, REPLACE;

        public static ResolverMethod fromName(String origin) {
            return ResolverMethod.valueOf(origin.toUpperCase());
        }

        public String toName() {
            return this.name().toLowerCase();
        }
    }

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
