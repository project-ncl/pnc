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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.processor.annotation.PatchSupport;

import java.util.List;
import java.util.Map;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.ADD;
import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@PatchSupport
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@JsonDeserialize(builder = ProductVersion.Builder.class)
public class ProductVersion extends ProductVersionRef {

    @PatchSupport({REPLACE})
    @RefHasId(groups = {WhenCreatingNew.class})
    private final ProductRef product;

    @PatchSupport({REPLACE})
    private final ProductMilestoneRef currentProductMilestone;

    @PatchSupport({ADD, REPLACE})
    private final List<ProductMilestoneRef> productMilestones;

    @PatchSupport({ADD, REPLACE})
    private final List<ProductReleaseRef> productReleases;

    @PatchSupport({ADD, REPLACE})
    private final List<GroupConfigurationRef> groupConfigurations;

    @PatchSupport({ADD, REPLACE})
    private final List<BuildConfigurationRef> buildConfigurations;

    @lombok.Builder(builderClassName = "Builder", toBuilder = true)
    private ProductVersion(ProductRef product, ProductMilestoneRef currentProductMilestone, List<ProductMilestoneRef> productMilestones, List<ProductReleaseRef> productReleases, List<GroupConfigurationRef> groupConfigurations, List<BuildConfigurationRef> buildConfigurations, String id, String version, Map<String, String> attributes) {
        super(id, version, attributes);
        this.product = product;
        this.currentProductMilestone = currentProductMilestone;
        this.productMilestones = productMilestones;
        this.productReleases = productReleases;
        this.groupConfigurations = groupConfigurations;
        this.buildConfigurations = buildConfigurations;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
