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

import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
public class Product extends ProductRef {

    private final List<ProductVersionRef> productVersionRefs;

    @lombok.Builder(builderClassName = "Builder")
    public Product(Integer id, String name, String description, String abbreviation, String productCode, String pgmSystemName, List<ProductVersionRef> productVersionRefs) {
        super(id, name, description, abbreviation, productCode, pgmSystemName);
        this.productVersionRefs = productVersionRefs;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
