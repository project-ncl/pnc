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

import org.jboss.pnc.dto.validation.constraints.RefHasId;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Data;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 */
@Data
@JsonDeserialize(builder = GroupConfiguration.Builder.class)
public class GroupConfiguration extends GroupConfigurationRef {

    @RefHasId(groups = {WhenCreatingNew.class, WhenUpdating.class}, optional = true)
    private final ProductVersionRef productVersion;

    private final List<BuildConfigurationRef> buildConfigurations;

    @lombok.Builder(builderClassName = "Builder")
    GroupConfiguration(ProductVersionRef productVersion, List<BuildConfigurationRef> buildConfigurations, Integer id, String name) {
        super(id, name);
        this.productVersion = productVersion;
        this.buildConfigurations = buildConfigurations;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
