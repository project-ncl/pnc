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

import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.validator.constraints.URL;
import org.jboss.pnc.api.enums.AttachmentType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(
        indexes = { @Index(name = "idx_attachment_name", columnList = "name"),
                @Index(name = "idx_attachment_url", columnList = "url"),
                @Index(name = "idx_attachment_creationtime", columnList = "creationtime"),
                @Index(name = "idx_attachment_type", columnList = "type"),
                @Index(name = "idx_attachment_buildrecord", columnList = "buildrecord_id") },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_attachment_recordid_name", columnNames = { "buildrecord_id", "name" }),
                @UniqueConstraint(name = "uk_attachment_url", columnNames = { "url" }) })
@ToString
public class Attachment implements GenericEntity<Integer> {

    public static final String SEQUENCE_NAME = "attachment_id_seq";

    @Id
    @SequenceGenerator(name = SEQUENCE_NAME, sequenceName = SEQUENCE_NAME, initialValue = 100, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = SEQUENCE_NAME)
    private Integer id;

    @Size(max = 1024)
    @Type(type = "org.hibernate.type.TextType")
    @Column(nullable = false)
    private String name;

    @Size(max = 1024)
    @Type(type = "org.hibernate.type.TextType")
    private String description;

    @Size(max = 1024)
    @Column(nullable = false, unique = true, length = 1024)
    @URL(regexp = "^(http|https).*")
    private String url;

    private Date creationTime;

    @NotNull
    @Size(max = 32)
    private String md5;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AttachmentType type;

    /**
     * The record that this artifact is supplement to. It can be a log, a result of a post-build analysis or some other
     * file that gives additional information about the build, but it is not a direct output of the build.
     */
    @ManyToOne
    @JoinColumn(foreignKey = @ForeignKey(name = "fk_artifact_buildrecord"))
    @ToString.Exclude
    private BuildRecord buildRecord;

    public Attachment() {
        this.creationTime = Date.from(Instant.now());
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public BuildRecord getBuildRecord() {
        return buildRecord;
    }

    public void setBuildRecord(BuildRecord buildRecord) {
        if (this.buildRecord != null) {
            this.buildRecord.getAttachments().remove(this);
        }
        if (buildRecord != null) {
            buildRecord.getAttachments().add(this);
        }
        this.buildRecord = buildRecord;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Attachment))
            return false;
        Attachment that = (Attachment) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        // Because the id is generated when the entity is stored to DB, we need to have constant hash code to achieve
        // equals+hashCode consistency across all JPA object states
        return 31;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Integer id;
        private String name;
        private String description;
        private String url;
        private Date creationTime;
        private String md5;
        private AttachmentType type;
        private BuildRecord buildRecord;

        Builder() {
        }

        public Builder id(Integer id) {
            this.id = id;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder md5(String md5) {
            this.md5 = md5;
            return this;
        }

        public Builder type(AttachmentType type) {
            this.type = type;
            return this;
        }

        public Builder buildRecord(BuildRecord buildRecord) {
            this.buildRecord = buildRecord;
            return this;
        }

        public Attachment build() {
            Attachment attachment = new Attachment();
            attachment.setId(id);
            attachment.setName(name);
            attachment.setDescription(description);
            attachment.setUrl(url);
            attachment.setMd5(md5);
            attachment.setType(type);
            attachment.setBuildRecord(buildRecord);

            return attachment;
        }
    }

}
