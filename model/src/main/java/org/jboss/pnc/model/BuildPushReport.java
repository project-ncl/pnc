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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import java.util.Objects;

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
