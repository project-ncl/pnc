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

package org.jboss.pnc.mock.builddriver;

import org.jboss.pnc.enums.BuildStatus;
import org.jboss.pnc.spi.builddriver.BuildDriverResult;

import java.util.Optional;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildDriverResultMock {

    public static final String BUILD_LOG = "The quick brown fox jumps over the lazy dog.\nFinished: SUCCESS";

    public static BuildDriverResult mockResult(BuildStatus status) {
        return new BuildDriverResult() {
            @Override
            public String getBuildLog() {
                return BUILD_LOG;
            }

            @Override
            public BuildStatus getBuildStatus() {
                return status;
            }

            @Override
            public Optional<String> getOutputChecksum() {
                return Optional.of("4678bbe366b11f7216bd03ad33f583d9");
            }
        };
    }

}
