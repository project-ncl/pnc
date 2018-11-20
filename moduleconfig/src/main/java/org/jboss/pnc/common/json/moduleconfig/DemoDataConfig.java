/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

public class DemoDataConfig extends AbstractModuleConfig{

    public static String MODULE_NAME = "demo-data-config";

    /**
     * Import initial data on application boot
     */
    private Boolean importDemoData;
    private String internalRepo1;
    private String internalRepo2;
    private String internalRepo3;
    private String internalRepo4;
    private String internalRepo5;

    public DemoDataConfig(@JsonProperty("importDemoData") Boolean importDemoData,
            @JsonProperty(value = "internalRepo1") String internalRepo1,
            @JsonProperty(value = "internalRepo2") String internalRepo2,
            @JsonProperty(value = "internalRepo3") String internalRepo3,
            @JsonProperty(value = "internalRepo4") String internalRepo4,
            @JsonProperty(value = "internalRepo5") String internalRepo5) {
        super();
        this.importDemoData = importDemoData;
        this.internalRepo1 = internalRepo1 == null ? "ssh://git@github.com:22/project-ncl/pnc.git" : internalRepo1;
        this.internalRepo2 = internalRepo2 == null ? "ssh://git@github.com:22/project-ncl/termd.git" : internalRepo2;
        this.internalRepo3 = internalRepo3 == null ? "ssh://git@github.com:22/project-ncl/pnc-build-agent.git" : internalRepo3;
        this.internalRepo4 = internalRepo4 == null ? "ssh://git@github.com:22/project-ncl/dependency-analysis.git" : internalRepo4;
        this.internalRepo5 = internalRepo5 == null ? "ssh://git@github.com:22/project-ncl/causeway.git" : internalRepo5;
    }

    public void setImportDemoData(Boolean importDemoData) {
        this.importDemoData = importDemoData;
    }

    public Boolean getImportDemoData() {
        return importDemoData;
    }

    public String getInternalRepo1() {
        return internalRepo1;
    }

    public void setInternalRepo1(String internalRepo1) {
        this.internalRepo1 = internalRepo1;
    }

    public String getInternalRepo2() {
        return internalRepo2;
    }

    public void setInternalRepo2(String internalRepo2) {
        this.internalRepo2 = internalRepo2;
    }

    public String getInternalRepo3() {
        return internalRepo3;
    }

    public void setInternalRepo3(String internalRepo3) {
        this.internalRepo3 = internalRepo3;
    }

    public String getInternalRepo4() {
        return internalRepo4;
    }

    public void setInternalRepo4(String internalRepo4) {
        this.internalRepo4 = internalRepo4;
    }

    public String getInternalRepo5() {
        return internalRepo5;
    }

    public void setInternalRepo5(String internalRepo5) {
        this.internalRepo5 = internalRepo5;
    }

    @Override
    public String toString() {
        return "DemoDataConfig [importDemoData=" + importDemoData + "]";
    }
}
