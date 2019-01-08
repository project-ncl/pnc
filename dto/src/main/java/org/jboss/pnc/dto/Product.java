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
package org.jboss.pnc.dto;

import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = Product.Builder.class)
public class Product extends ProductRef {

    @Getter
    @Setter
    private final Set<ProductVersionRef> productVersions;

    @lombok.Builder(builderClassName = "Builder")
    private Product(Set<ProductVersionRef> productVersions, Integer id, String name, String description, String abbreviation, String productCode, String pgmSystemName) {
        super(id, name, description, abbreviation, productCode, pgmSystemName);
        this.productVersions = productVersions;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
