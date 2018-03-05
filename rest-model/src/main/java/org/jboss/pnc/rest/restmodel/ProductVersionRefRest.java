/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jboss.pnc.model.ProductVersion;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Jakub Bartecek
 */
@XmlRootElement(name = "ProductVersionRefRest")
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ProductVersionRefRest implements GenericRestEntity<Integer> {

    private Integer id;

    @Getter
    @Setter
    private String version;

    @Getter
    @Setter
    private String productName;

    @Getter
    @Setter
    private Integer currentMilestoneId;

    public ProductVersionRefRest(ProductVersion productVersion) {
        this.id = productVersion.getId();
        this.version = productVersion.getVersion();
        this.productName = productVersion.getProduct().getName();
        this.currentMilestoneId = productVersion.getCurrentProductMilestone().getId();
    }

    /**
     * Gets ID.
     *
     * @return ID.
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Sets ID.
     *
     * @param id ID.
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }
}
