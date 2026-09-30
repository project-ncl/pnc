/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
    MVN_RPM(RepositoryType.MAVEN),

    /**
     * Build RPMs using Mock tool and spec file. The artifacts are published into an {@link RepositoryType#RPM}
     * repository. There's no alignment process present currently.
     */
    RPM(RepositoryType.RPM);

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
