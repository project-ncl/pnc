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
package org.jboss.pnc.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * The composite primary key of the {@link DeliverableArtifact} table.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class DeliverableArtifactPK implements Serializable {

    /**
     * The id of the report.
     */
    private DeliverableAnalyzerReport report;

    /**
     * The id of the artifact.
     */
    private Artifact artifact;

    /**
     * The id of distribution
     */
    private DeliverableAnalyzerDistribution distribution;
}
