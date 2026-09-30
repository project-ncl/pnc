/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.common.json.moduleconfig;

import java.util.ArrayList;
import java.util.List;

import org.jboss.pnc.common.json.AbstractModuleConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DemoDataConfig extends AbstractModuleConfig {

    public static String MODULE_NAME = "demo-data-config";

    /**
     * Import initial data on application boot
     */
    private Boolean importDemoData;

    private List<String> internalRepos;

    public DemoDataConfig(
            @JsonProperty("importDemoData") Boolean importDemoData,
            @JsonProperty(value = "internalRepos") List<String> internalRepos) {
        super();
        this.importDemoData = importDemoData;
        this.internalRepos = internalRepos == null ? new ArrayList<>() : internalRepos;
    }

    public void setImportDemoData(Boolean importDemoData) {
        this.importDemoData = importDemoData;
    }

    public Boolean getImportDemoData() {
        return importDemoData;
    }

    public String getInternalRepo(int index) {
        if (index >= 0 && index < internalRepos.size()) {
            return internalRepos.get(index);
        }
        throw new IllegalArgumentException(
                "Invalid pnc-config in module " + MODULE_NAME + " : Internal repo with index " + index
                        + " doesn't exist");
    }

    public List<String> getInternalRepos() {
        return internalRepos;
    }

    @Override
    public String toString() {
        return "DemoDataConfig [importDemoData=" + importDemoData + "]";
    }
}
