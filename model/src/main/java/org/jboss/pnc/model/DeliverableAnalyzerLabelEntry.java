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
import org.jboss.pnc.api.enums.LabelOperation;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;

/**
 * The label entry is used for tracking history of changes done with {@link DeliverableAnalyzerReport}.
 *
 * @author Adam Kridl &lt;akridl@redhat.com&gt;
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_delanlabelentry_id_report",
                columnNames = { "id", "report_id" }))
public class DeliverableAnalyzerLabelEntry implements GenericEntity<Base32LongID> {

    @EmbeddedId
    @Column(name = "id")
    private Base32LongID id;

    /**
     * The report associated to this entry.
     */
    @ManyToOne
    @JoinColumn(name = "report_id", foreignKey = @ForeignKey(name = "fk_delanlabelentry_report"))
    private DeliverableAnalyzerReport report;

    /**
     * Represents the modification order of this entry. The modification order is used together with the {@code report}
     * attribute to create unique constraint in order to prevent 2 threads to modify the same report in the same time
     * and bring it to the inconsistent state.
     */
    @Column(name = "change_order")
    private Integer changeOrder;

    /**
     * The date of the change.
     */
    private Date entryTime;

    /**
     * The user who triggered the change.
     */
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_delanlabelentry_user"))
    private User user;

    /**
     * The reason of the change.
     */
    private String reason;

    /**
     * The label which is added (removed) to (from) this entry.
     */
    @Enumerated(EnumType.STRING)
    private DeliverableAnalyzerReportLabel label;

    /**
     * Holds the information whether the {@link DeliverableAnalyzerReportLabel} is added (removed) to (from) this entry.
     */
    @Enumerated(EnumType.STRING)
    private LabelOperation change;
}
