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
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Join table between {@link DeliverableAnalyzerReport} and {@link Artifact} with some additional information.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@IdClass(DeliverableArtifactPK.class)
public class DeliverableArtifact implements GenericEntity<DeliverableArtifactPK> {

    @Id
    @ManyToOne
    @JoinColumn(name = "report_id", foreignKey = @ForeignKey(name = "fk_deliverableartifact_delanreport"))
    private DeliverableAnalyzerReport report;

    @Id
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_deliverableartifact_artifact"))
    private Artifact artifact;

    /**
     * Boolean flag denoting whether the artifact was built in a build-system (like PNC or Brew).
     */
    private boolean builtFromSource;

    /**
     * The id of the Brew build in case this artifact was built in Brew.
     */
    private Integer brewBuildId;

    public DeliverableArtifactPK getId() {
        return new DeliverableArtifactPK(report, artifact);
    }

    public void setId(DeliverableArtifactPK id) {
        report = id.getReport();
        artifact = id.getArtifact();
    }
}
