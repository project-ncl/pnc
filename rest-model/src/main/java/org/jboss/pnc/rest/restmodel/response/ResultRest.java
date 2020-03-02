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
package org.jboss.pnc.rest.restmodel.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@Getter
public class ResultRest {

    private final String id;

    private final String name;

    private final Status status;

    private final String message;

    public ResultRest(String id, String name, Status status) {
        this.id = id;
        this.name = name;
        this.status = status;
        message = "";
    }

    @JsonCreator
    public ResultRest(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") Status status,
            @JsonProperty("message") String message) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.message = message;
    }

    @JsonIgnore
    public boolean isSuccess() {
        return status.isSuccess();
    }

    public enum Status {
        ACCEPTED(true), SUCCESS(true), REJECTED(false), FAILED(false), SYSTEM_ERROR(false);

        private boolean success;

        Status(boolean success) {
            this.success = success;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
