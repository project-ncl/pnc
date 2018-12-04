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
package org.jboss.pnc.messaging.spi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Getter;
import org.jboss.pnc.common.json.JsonOutputConverterMapper;
import org.jboss.pnc.spi.dto.Build;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
//@Builder()
@Getter
@JsonDeserialize(builder = BuildStatusChanged.BuildStatusChangedBuilder.class)
//TODO 2.0 unify with BuildChangedPayload
public class BuildStatusChanged implements Message {

    private final String attribute = "state";

    private final String oldStatus;

    /**
     * Will be removed in 2.0
     * @deprecated use build.status
     */
    @Deprecated
    private final String newStatus;

    /**
     * Will be removed in 2.0
     * @deprecated use build.id
     */
    @Deprecated
    private final String buildRecordId;

    private final Build build;

    @Deprecated
    public BuildStatusChanged(String oldStatus, String newStatus, String buildRecordId) {
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.buildRecordId = buildRecordId;
        build = null;
    }

    public BuildStatusChanged(String oldStatus, Build build) {
        this.oldStatus = oldStatus;
        this.newStatus = build.getStatus().toString();
        this.buildRecordId = build.getId().toString();
        this.build = build;
    }



    @Override
    public String toJson() {
        return JsonOutputConverterMapper.apply(this);
    }

//    @JsonPOJOBuilder(withPrefix = "")
//    public static final class BuildStatusChangedBuilder {
//    }

    @JsonPOJOBuilder(withPrefix = "")
    public static final class BuildStatusChangedBuilder {
        private String oldStatus;
        private String newStatus;
        private String buildRecordId;
        private Build build;

        BuildStatusChangedBuilder() {
        }

        public BuildStatusChanged.BuildStatusChangedBuilder oldStatus(String oldStatus) {
            this.oldStatus = oldStatus;
            return this;
        }

        /** @deprecated */
        @Deprecated
        public BuildStatusChanged.BuildStatusChangedBuilder newStatus(String newStatus) {
            this.newStatus = newStatus;
            return this;
        }

        /** @deprecated */
        @Deprecated
        public BuildStatusChanged.BuildStatusChangedBuilder buildRecordId(String buildRecordId) {
            this.buildRecordId = buildRecordId;
            return this;
        }

        public BuildStatusChanged.BuildStatusChangedBuilder build(Build build) {
            this.build = build;
            return this;
        }

        public BuildStatusChanged build() {
            return new BuildStatusChanged(
              oldStatus,build
            );
        }

        public String toString() {
            return "BuildStatusChanged.BuildStatusChangedBuilder(oldStatus=" + this.oldStatus + ", newStatus=" + this.newStatus + ", buildRecordId=" + this.buildRecordId + ", build=" + this.build + ")";
        }
    }
}
