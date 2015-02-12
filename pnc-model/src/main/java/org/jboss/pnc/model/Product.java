/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.pnc.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author avibelli
 *
 */
@Entity
public class Product implements Serializable {

    private static final long serialVersionUID = -9022966336791211855L;
    
    public static final String DEFAULT_SORTING_FIELD = "name";

    @Id
    @GeneratedValue
    private Integer id;

    private String name;

    private String description;

    private String abbreviation;

    private String productCode;

    private String pgmSystemName;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private Set<ProductVersion> productVersions;

    /**
     * Instantiates a new product.
     */
    public Product() {
        productVersions = new HashSet<>();
    }

    /**
     * @param name
     * @param description
     * @param version
     */
    public Product(String name, String description) {
        this();
        this.name = name;
        this.description = description;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Shortened informal name of the product
     */
    public String getAbbreviation() {
		return abbreviation;
	}

	public void setAbbreviation(String abbreviation) {
		this.abbreviation = abbreviation;
	}

	/**
     * Product code in internal systems
     */
	public String getProductCode() {
		return productCode;
	}

	public void setProductCode(String productCode) {
		this.productCode = productCode;
	}

    /**
     * Name of the product used by program management planning system
     */
	public String getPgmSystemName() {
		return pgmSystemName;
	}

	public void setPgmSystemName(String pgmSystemName) {
		this.pgmSystemName = pgmSystemName;
	}

    /**
     * @return the productVersions
     */
    public Set<ProductVersion> getProductVersions() {
        return productVersions;
    }

    /**
     * @param productVersions the productVersions to set
     */
    public void setProductVersions(Set<ProductVersion> productVersions) {
        this.productVersions = productVersions;
    }

    /**
     * Add a version for the Product
     *
     * @param version
     * @return
     */
    public Set<ProductVersion> addVersion(ProductVersion version) {
        productVersions.add(version);

        return productVersions;
    }

}
