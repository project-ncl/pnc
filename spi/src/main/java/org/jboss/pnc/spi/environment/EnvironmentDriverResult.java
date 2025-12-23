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
package org.jboss.pnc.spi.environment;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.jboss.pnc.spi.SshCredentials;
import org.jboss.pnc.spi.coordinator.CompletionStatus;

import java.io.Serializable;
import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
@JsonDeserialize(builder = EnvironmentDriverResult.EnvironmentDriverResultBuilder.class)
@Builder
@AllArgsConstructor
public class EnvironmentDriverResult implements Serializable {

    @Getter
    private final CompletionStatus completionStatus;

    @Getter
    private final Optional<SshCredentials> sshCredentials;

    @JsonPOJOBuilder(withPrefix = "")
    public static final class EnvironmentDriverResultBuilder {
    }

    @Override
    public String toString() {
        return toStringLimited();
    }

    public String toStringLimited() {
        return "EnvironmentDriverResult{" + "completionStatus=" + completionStatus + ", sshCredentials="
                + sshCredentials + '}';
    }
}
