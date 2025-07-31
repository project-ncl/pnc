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

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Type;
import org.jboss.pnc.api.enums.LicenseSource;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderClassName = "Builder", toBuilder = true)
@Entity
public class DeliverableArtifactLicenseInfo implements GenericEntity<Base32LongID> {

    @EmbeddedId
    @Column(name = "id")
    private Base32LongID id;

    /**
     * The comments provided in the license information
     */
    @Type(type = "org.hibernate.type.TextType")
    private String comments;

    /**
     * The distribution (in case of Maven) provided in the license information
     */
    private String distribution;

    /**
     * The name provided for the license
     */
    @Type(type = "org.hibernate.type.TextType")
    private String name;

    /**
     * The url provided for the license
     */
    @Type(type = "org.hibernate.type.TextType")
    private String url;

    /**
     * The computed spdx license identifier associated with this license
     */
    @NotNull
    @Column(nullable = false)
    private String spdxLicenseId;

    /**
     * The type of source analyzed for the license information
     */
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Deprecated(since = "3.1.3", forRemoval = true)
    private LicenseSource source = LicenseSource.UNKNOWN;

    /**
     * The relative url of the source analyzed for the license information
     */
    @NotNull
    @Column(nullable = false)
    @Type(type = "org.hibernate.type.TextType")
    private String sourceUrl;

    /**
     * The delivered artifact associated to this license information.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
            @JoinColumn(
                    name = "delartifact_report_id",
                    referencedColumnName = "report_id",
                    foreignKey = @ForeignKey(name = "fk_delartifact"),
                    nullable = false),
            @JoinColumn(
                    name = "delartifact_artifact_id",
                    referencedColumnName = "artifact_id",
                    foreignKey = @ForeignKey(name = "fk_delartifact"),
                    nullable = false),
            @JoinColumn(
                    name = "delartifact_distribution_id",
                    referencedColumnName = "distribution_id",
                    foreignKey = @ForeignKey(name = "fk_delartifact"),
                    nullable = false) })
    private DeliverableArtifact artifact;

}
