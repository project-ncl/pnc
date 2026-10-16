/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.mock.model.builders;

import java.time.Instant;
import java.util.Date;

import org.jboss.pnc.constants.ReposiotryIdentifier;
import org.jboss.pnc.enums.RepositoryType;
import org.jboss.pnc.model.Artifact;
import org.jboss.pnc.model.TargetRepository;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class ArtifactBuilder {

    public static final String IDENTIFIER_PREFIX = "org.jboss.pnc:mock.artifact";

    private static Artifact.Builder getArtifactBuilder(int id) {
        return Artifact.Builder.newBuilder()
                .id(id)
                .identifier(IDENTIFIER_PREFIX + ":" + id)
                .md5("md-fake-ABCDABCD" + id)
                .sha1("sha1-fake-ABCDABCD" + id)
                .sha256("sha256-fake-ABCDABCD" + id)
                .size(12342L)
                .deployPath("http://myrepo.com/org/jboss/mock/artifactFile" + id + ".jar")
                .targetRepository(mockTargetRepository("builds-untested-" + id))
                .filename("artifactFile" + id + ".jar");
    }

    /**
     * Create a generic mock artifact with no associated build record or import URL
     */
    public static Artifact mockArtifact(int id) {
        return getArtifactBuilder(id).build();
    }

    /**
     * Create an artifact with an import date and origin url
     */
    public static Artifact mockImportedArtifact(int id) {
        return getArtifactBuilder(id).importDate(Date.from(Instant.now()))
                .originUrl("http://central.maven.org/org/jboss/mock/artifactFile" + id + ".jar")
                .build();
    }

    public static TargetRepository mockTargetRepository(String path) {
        return TargetRepository.newBuilder()
                .identifier(ReposiotryIdentifier.INDY_MAVEN)
                .repositoryPath(path)
                .repositoryType(RepositoryType.MAVEN)
                .temporaryRepo(false)
                .build();
    }

}
