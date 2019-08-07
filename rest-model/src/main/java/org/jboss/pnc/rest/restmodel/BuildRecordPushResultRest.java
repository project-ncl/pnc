/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.rest.restmodel;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.enums.BuildPushStatus;
import org.jboss.pnc.model.BuildRecord;
import org.jboss.pnc.model.BuildRecordPushResult;
import org.jboss.pnc.bpm.model.causeway.ArtifactImportError;
import org.jboss.pnc.rest.validation.groups.WhenCreatingNew;
import org.jboss.pnc.rest.validation.groups.WhenUpdating;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = BuildRecordPushResultRest.BuildRecordPushResultRestBuilder.class)
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Setter(onMethod=@__({@Deprecated}))
public class BuildRecordPushResultRest implements GenericRestEntity<Integer> {

    @Getter
    @Setter(onMethod=@__({@Deprecated}))
    @NotNull(groups = WhenUpdating.class)
    @Null(groups = WhenCreatingNew.class)
    private Integer id;

    @NotNull
    @Getter
    private Integer buildRecordId;

    @NotNull
    @Getter
    private BuildPushStatus status;

    @NotNull
    @Getter
    private String log;

    /**
     * list of errors for artifact imports
     */
    @Getter
    private List<ArtifactImportError> artifactImportErrors;

    /**
     * build id assigned by brew
     */
    @Getter
    private Integer brewBuildId;

    /**
     * link to brew
     */
    @Getter
    private String brewBuildUrl;

    public BuildRecordPushResultRest(BuildRecordPushResult buildRecordPushResult) {
        this.id = buildRecordPushResult.getId();
        this.buildRecordId = buildRecordPushResult.getBuildRecord().getId();
        this.status = buildRecordPushResult.getStatus();
        this.log = buildRecordPushResult.getLog();
        this.brewBuildId = buildRecordPushResult.getBrewBuildId();
        this.brewBuildUrl = buildRecordPushResult.getBrewBuildUrl();
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildRecordPushResultRestBuilder {
    }

    @Override
    public String toString() {
        return JsonOutputConverterMapper.apply(this);
    }

    public BuildRecordPushResult.Builder toDBEntityBuilder() {
        BuildRecord buildRecord = BuildRecord.Builder.newBuilder().id(buildRecordId).build();
        BuildRecordPushResult.Builder builder = BuildRecordPushResult.newBuilder()
                .id(id)
                .status(status)
                .log(combineLog())
                .buildRecord(buildRecord)
                .brewBuildId(brewBuildId)
                .brewBuildUrl(brewBuildUrl);

        return builder;
    }

    private String combineLog() {
        return ArtifactImportError.combineMessages(log, artifactImportErrors);
    }
}
