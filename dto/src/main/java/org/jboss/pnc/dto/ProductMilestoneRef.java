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
import org.jboss.pnc.dto.validation.groups.WhenCreatingNew;
import org.jboss.pnc.dto.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.Date;

/**
 *
 * @author Jakub Bartecek &lt;jbartece@redhat.com&gt;
 */
@Data
@Builder(builderClassName = "Builder")
public class ProductMilestoneRef implements DTOEntity {
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    protected final Integer id;

    /**
     * Version check to be correspond with the DB pattern in ProductMilestone.version. Version examples: 1.2.3.ER1, 1.2.10.CR1, 1.2.CD1
     */
    @NotNull(groups = {WhenCreatingNew.class, WhenUpdating.class})
    @Pattern(groups = {WhenCreatingNew.class, WhenUpdating.class}, regexp = "^[0-9]+\\.[0-9]+(\\.[0-9]+)?\\.[\\w]+$", message = "Version doesn't match the required pattern ^[0-9]+\\.[0-9]+(\\.[0-9]+)?\\.[\\w]+$")
    protected final String version;

    protected final Date endDate;

    protected final Date startingDate;

    protected final Date plannedEndDate;

    protected final String downloadUrl;

    protected final String issueTrackerUrl;

    @Override
    public Integer getId() {
        return id;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class Builder {
    }
}
