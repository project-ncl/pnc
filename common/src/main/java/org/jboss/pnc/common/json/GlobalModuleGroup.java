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
package org.jboss.pnc.common.json;

import com.fasterxml.jackson.annotation.JsonTypeName;

@JsonTypeName(value = "global")
public class GlobalModuleGroup extends AbstractModuleGroup {

    private String bpmUrl;
    private String cartographerUrl;
    private String daUrl;
    private String indyUrl;
    private String pncUrl;
    private String repourUrl;
    private String delAnalUrl;

    private String externalBifrostUrl;
    private String externalDaUrl;
    private String externalCausewayUrl;
    private String externalIndyUrl;
    private String externalKafkaStoreUrl;
    private String externalPncUrl;
    private String externalRepourUrl;
    private String externalUiLoggerUrl;

    public String getBpmUrl() {
        return bpmUrl;
    }

    public void setBpmUrl(String bpmUrl) {
        this.bpmUrl = bpmUrl;
    }

    public String getCartographerUrl() {
        return cartographerUrl;
    }

    public void setCartographerUrl(String cartographerUrl) {
        this.cartographerUrl = cartographerUrl;
    }

    public String getDaUrl() {
        return daUrl;
    }

    public void setDaUrl(String daUrl) {
        this.daUrl = daUrl;
    }

    public String getIndyUrl() {
        return indyUrl;
    }

    public void setIndyUrl(String indyUrl) {
        this.indyUrl = indyUrl;
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

    public String getExternalBifrostUrl() {
        return externalBifrostUrl;
    }

    public void setExternalBifrostUrl(String externalBifrostUrl) {
        this.externalBifrostUrl = externalBifrostUrl;
    }

    public String getExternalDaUrl() {
        return externalDaUrl;
    }

    public void setExternalDaUrl(String externalDaUrl) {
        this.externalDaUrl = externalDaUrl;
    }

    public String getExternalCausewayUrl() {
        return externalCausewayUrl;
    }

    public void setExternalCausewayUrl(String externalCausewayUrl) {
        this.externalCausewayUrl = externalCausewayUrl;
    }

    public String getExternalIndyUrl() {
        return externalIndyUrl;
    }

    public void setExternalIndyUrl(String externalIndyUrl) {
        this.externalIndyUrl = externalIndyUrl;
    }

    public String getExternalKafkaStoreUrl() {
        return externalKafkaStoreUrl;
    }

    public void setExternalKafkaStoreUrl(String externalKafkaStoreUrl) {
        this.externalKafkaStoreUrl = externalKafkaStoreUrl;
    }

    public String getExternalPncUrl() {
        return externalPncUrl;
    }

    public void setExternalPncUrl(String externalPncUrl) {
        this.externalPncUrl = externalPncUrl;
    }

    public String getExternalRepourUrl() {
        return externalRepourUrl;
    }

    public void setExternalRepourUrl(String externalRepourUrl) {
        this.externalRepourUrl = externalRepourUrl;
    }

    public String getDelAnalUrl() {
        return delAnalUrl;
    }

    public void setDelAnalUrl(String delAnalUrl) {
        this.delAnalUrl = delAnalUrl;
    }

    public String getExternalUiLoggerUrl() {
        return externalUiLoggerUrl;
    }

    public void setExternalUiLoggerUrl(String externalUiLoggerUrl) {
        this.externalUiLoggerUrl = externalUiLoggerUrl;
    }
}
