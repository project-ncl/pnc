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
import org.jboss.pnc.api.enums.DeliverableAnalyzerReportLabel;
import org.jboss.pnc.model.utils.DeliverableAnalyzerReportLabelToStringConverter;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * The report of the {@link DeliverableAnalyzerOperation}.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Entity
public class DeliverableAnalyzerReport implements GenericEntity<Base32LongID> {

    /**
     * This primary key is in fact the foreign key to the table associated with the {@link DeliverableAnalyzerOperation}
     * entity class. It is guaranteed because of {@code @MapsId} annotation used with {@code operation} attribute, see
     * below.
     */
    @EmbeddedId
    @Column(name = "operation_id")
    private Base32LongID id;

    /**
     * This foreign key is in the database table mapped to the same column as {@code id} attribute ({@code @MapsId}
     * annotation). Hence, guarantying the required property of primary key of this table being foreign key to the table
     * associated with the {@link DeliverableAnalyzerOperation} entity.
     */
    @MapsId
    @OneToOne
    private DeliverableAnalyzerOperation operation;

    /**
     * Active labels of the report.
     */
    @Convert(converter = DeliverableAnalyzerReportLabelToStringConverter.class)
    private EnumSet<DeliverableAnalyzerReportLabel> labels;

    /**
     * Tracked history of what was done with this report.
     */
    @OneToMany(mappedBy = "report")
    private List<DeliverableAnalyzerLabelEntry> labelHistory;

    /**
     * The collection of artifacts, which are the output of the analysis corresponding to this report.
     */
    @OneToMany(mappedBy = "report", cascade = CascadeType.PERSIST)
    private Set<DeliverableArtifact> artifacts;
}
