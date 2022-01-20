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
import org.jboss.pnc.common.json.AbstractModuleConfig;

import java.util.ArrayList;
import java.util.List;

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
