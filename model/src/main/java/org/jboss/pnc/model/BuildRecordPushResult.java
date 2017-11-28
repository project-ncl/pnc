/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@ToString
@Builder
@Entity
@Table(indexes = {@Index(name = "idx_buildrecordpushresult_buildrecord", columnList = "buildRecord_id")})
public class BuildRecordPushResult implements GenericEntity<Integer> {

    @Id
    @Getter
    @Setter
    private Integer id;

    @JoinColumn(foreignKey = @ForeignKey(name = "fk_buildrecordpushresult_buildrecord"))
    @ManyToOne
    @Getter
    @Setter
    private BuildRecord buildRecord;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    private BuildRecordPushResult.Status status;

    @Getter
    @Setter
    private String log;

    /**
     * build id assigned by brew
     */
    @Getter
    @Setter
    private Integer brewBuildId;

    /**
     * link to brew
     */
    @Getter
    @Setter
    private String brewBuildUrl;

    public enum Status {
        SUCCESS, FAILED, SYSTEM_ERROR, CANCELED;
    }
}
