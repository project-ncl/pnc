/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.mock.spi;

import org.jboss.pnc.enums.BuildType;
import org.jboss.pnc.enums.SystemImageType;
import org.jboss.pnc.spi.executor.BuildExecutionConfiguration;

import java.util.HashMap;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class BuildExecutionConfigurationMock {

    public static final String DEFAULT_SYSTEM_IMAGE_ID = "abcd1234";

    public static BuildExecutionConfiguration mock() {
        return BuildExecutionConfiguration.build(
                "1",
                "condent-id",
                "1",
                "mvn clean install",
                "configuration name",
                "https://pathToRepo.git",
                "f18de64523d5054395d82e24d4e28473a05a3880",
                "1.0.0.redhat-1",
                "https://pathToOriginRepo.git",
                false,
                DEFAULT_SYSTEM_IMAGE_ID,
                "image.repo.url/repo",
                SystemImageType.DOCKER_IMAGE,
                BuildType.MVN,
                false,
                null,
                new HashMap<>(),
                false,
                null,
                false,
                "-DdependencySource=REST -DrepoRemovalBackup=repositories-backup.xml -DversionSuffixStrip= -DreportNonAligned=true");
    }
}
