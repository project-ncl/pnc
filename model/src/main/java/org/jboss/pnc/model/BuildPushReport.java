/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.model;

import java.util.Objects;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Entity
public class BuildPushReport implements GenericEntity<Base32LongID> {
    private static final long serialVersionUID = 3204811054462723774L;

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
    private BuildPushOperation operation;

    /**
     * build id assigned by brew
     */
    private int brewBuildId;

    /**
     * link to brew
     */
    private String brewBuildUrl;

    public static Builder newBuilder() {
        return new Builder();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliverableAnalyzerReport)) {
            return false;
        }
        DeliverableAnalyzerReport report = (DeliverableAnalyzerReport) o;
        return id != null && id.equals(report.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

}
