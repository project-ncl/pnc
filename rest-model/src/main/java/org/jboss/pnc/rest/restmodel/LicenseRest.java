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
package org.jboss.pnc.rest.restmodel;

import org.jboss.pnc.model.License;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "License")
public class LicenseRest implements GenericRestEntity<Integer> {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    private String fullName;

    private String fullContent;

    private String refUrl;

    private String shortName;

    public LicenseRest() {

    }

    public LicenseRest(License license) {
        this.id = license.getId();
        this.fullName = license.getFullName();
        this.fullContent = license.getFullContent();
        this.refUrl = license.getRefUrl();
        this.shortName = license.getShortName();
    }

    /**
     * @return the id
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the fullName
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * @param fullName the fullName to set
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * @return the fullContent
     */
    public String getFullContent() {
        return fullContent;
    }

    /**
     * @param fullContent the fullContent to set
     */
    public void setFullContent(String fullContent) {
        this.fullContent = fullContent;
    }

    /**
     * @return the refUrl
     */
    public String getRefUrl() {
        return refUrl;
    }

    /**
     * @param refUrl the refUrl to set
     */
    public void setRefUrl(String refUrl) {
        this.refUrl = refUrl;
    }

    /**
     * @return the shortName
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * @param shortName the shortName to set
     */
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }


    public License.Builder toDBEntityBuilder() {
        License.Builder builder = License.Builder.newBuilder()
            .id(id).fullName(fullName)
            .fullContent(fullContent).refUrl(refUrl)
            .shortName(shortName);

        return builder;
    }

}
