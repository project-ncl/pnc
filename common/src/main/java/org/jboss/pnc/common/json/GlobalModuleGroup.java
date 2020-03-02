/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.common.json;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "global")
public class GlobalModuleGroup extends AbstractModuleGroup {
    private String aproxUrl;
    private String bpmUrl;
    private String pncUrl;
    private String repourUrl;
    private String daUrl;

    public String getAproxUrl() {
        return aproxUrl;
    }

    public void setAproxUrl(String aproxUrl) {
        this.aproxUrl = aproxUrl;
    }

    public String getBpmUrl() {
        return bpmUrl;
    }

    public void setBpmUrl(String bpmUrl) {
        this.bpmUrl = bpmUrl;
    }

    public String getPncUrl() {
        return pncUrl;
    }

    public void setPncUrl(String pncUrl) {
        this.pncUrl = pncUrl;
    }

    public String getRepourUrl() {
        return repourUrl;
    }

    public void setRepourUrl(String repourUrl) {
        this.repourUrl = repourUrl;
    }

    public String getDaUrl() {
        return daUrl;
    }

    public void setDaUrl(String daUrl) {
        this.daUrl = daUrl;
    }

}
