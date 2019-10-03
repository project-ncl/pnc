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
import lombok.Builder;
import lombok.Data;
import org.jboss.pnc.dto.validation.constraints.SCMUrl;
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;
import org.jboss.pnc.processor.annotation.PatchSupport;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;

import static org.jboss.pnc.processor.annotation.PatchSupport.Operation.REPLACE;

/**
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@PatchSupport
@Data
@Builder(builderClassName = "Builder", toBuilder = true)
@JsonDeserialize(builder = SCMRepository.Builder.class)
public class SCMRepository implements DTOEntity {

    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final String id;

    @NotBlank(groups = {WhenUpdating.class, WhenCreatingNew.class})
    @SCMUrl(groups = {WhenUpdating.class, WhenCreatingNew.class})
    protected final String internalUrl;

    @PatchSupport({REPLACE})
    @SCMUrl(groups = {WhenUpdating.class, WhenCreatingNew.class})
    protected final String externalUrl;

    @PatchSupport({REPLACE})
    protected final Boolean preBuildSyncEnabled;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
