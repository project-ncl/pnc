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
package org.jboss.pnc.enums;

/**
 * BuildType is used to define pre-build operations and to set proper repository.
 *
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 * @deprecated use pnc-api
 */
@Deprecated
public enum BuildType {
    /**
     * Build using Maven as its build tool. Uses POM Manipulation Extension in pre-build oprations and
     * {@link RepositoryType#MAVEN} repository.
     */
    MVN(RepositoryType.MAVEN),
    /**
     * Build using NPM as its build tool. Uses project-manipulator in pre-build oprations and {@link RepositoryType#NPM}
     * repository.
     */
    NPM(RepositoryType.NPM),
    /**
     * Build using Gradle as its build tool. Uses Gradle Manipulator in pre-build oprations and
     * {@link RepositoryType#MAVEN} repository.
     */
    GRADLE(RepositoryType.MAVEN),

    /**
     * Build using SBT (Scala Build Tool) as its build tool. Uses project-manipulator in pre-build oprations and
     * {@link RepositoryType#MAVEN} repository.
     */
    SBT(RepositoryType.MAVEN),

    /**
     * Build wrapperRpms using modified pom file, rpm maven plugin and PME for alignment. The artifacts are published as
     * maven artifacts to {@link RepositoryType#MAVEN} repository.
     */
    MVN_RPM(RepositoryType.MAVEN);

    private final RepositoryType repoType;

    private BuildType(RepositoryType repoType) {
        this.repoType = repoType;
    }

    /**
     * Gets repository type assigned with this build type.
     *
     * @return the repository type
     */
    public RepositoryType getRepoType() {
        return repoType;
    }
}
