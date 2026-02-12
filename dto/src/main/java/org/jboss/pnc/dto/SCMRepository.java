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
package org.jboss.pnc.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.dto.validation.constraints.NoHtml;
import org.jboss.pnc.dto.validation.constraints.SCMUrl;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 * Configuration of the SCM repository.
 * 
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@PatchSupport
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = SCMRepository.Builder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SCMRepository implements DTOEntity {

    /**
     * ID of the SCM Repository.
     */
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String id;

    /**
     * URL to the internal SCM repository, which is the main repository used for the builds. New commits can be added to
     * this repository, during the pre-build steps of the build process.
     */
    @NotBlank(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @SCMUrl(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String internalUrl;

    /**
     * URL to the upstream SCM repository.
     */
    @PatchSupport({ REPLACE })
    @SCMUrl(groups = { WhenUpdating.class, WhenCreatingNew.class })
    @NoHtml(groups = { WhenCreatingNew.class, WhenUpdating.class })
    protected final String externalUrl;

    /**
     * Declares whether the pre-build repository synchronization from external repository should happen or not.
     */
    @PatchSupport({ REPLACE })
    protected final Boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Builder {
    }
}
