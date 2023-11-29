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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * The distribution of the {@link DeliverableArtifact}.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Entity
public class DeliverableAnalyzerDistribution implements GenericEntity<Base32LongID> {

    @EmbeddedId
    private Base32LongID id;

    /**
     * The url of the distribution analyzed
     */
    @Type(type = "org.hibernate.type.TextType")
    private String distributionUrl;

    @NotNull
    @Column(columnDefinition = "timestamp with time zone", updatable = false)
    private Date creationTime;

    @OneToMany(mappedBy = "distribution", cascade = CascadeType.PERSIST)
    private Set<DeliverableArtifact> artifacts;

    @PrePersist
    private void initCreationTime() {
        this.creationTime = Date.from(Instant.now());
    }

    public void addDeliverableArtifact(DeliverableArtifact deliverableArtifact) {
        if (artifacts == null) {
            artifacts = new HashSet<DeliverableArtifact>();
        }
        artifacts.add(deliverableArtifact);
        deliverableArtifact.setDistribution(this);
    }

    public void removeDeliverableArtifact(DeliverableArtifact deliverableArtifact) {
        if (artifacts != null) {
            artifacts.remove(deliverableArtifact);
        }
        deliverableArtifact.setDistribution(null);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeliverableAnalyzerDistribution)) {
            return false;
        }
        DeliverableAnalyzerDistribution distributionUrl = (DeliverableAnalyzerDistribution) o;
        return id != null && id.equals(distributionUrl.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "DeliverableAnalyzerDistribution{" + "id=" + id + ", distributionUrl=" + distributionUrl
                + ", creationTime=" + creationTime + ", artifacts=" + artifacts + '}';
    }
}
