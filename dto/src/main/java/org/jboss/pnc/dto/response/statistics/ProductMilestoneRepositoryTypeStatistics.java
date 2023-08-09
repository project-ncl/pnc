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
package org.jboss.pnc.dto.response.statistics;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import org.jboss.pnc.dto.ProductMilestoneRef;
import org.jboss.pnc.enums.RepositoryType;

import java.util.EnumMap;

/**
 * Statistics about proportion of repository type of delivered artifacts.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Value
@Builder(builderClassName = "Builder")
@JsonIgnoreProperties(ignoreUnknown = true)
@Jacksonized
public class ProductMilestoneRepositoryTypeStatistics {

    /**
     * Identification of {@link org.jboss.pnc.dto.ProductMilestone} to which the proportion below links to.
     */
    ProductMilestoneRef productMilestone;

    /**
     * Proportion of repository type of Delivered Artifacts.
     */
    EnumMap<RepositoryType, Long> repositoryType;
}
