/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

import java.util.List;

import lombok.Builder;
import lombok.Value;

/**
 * Where the sources and the alignment diff of a build are in the internal SCM: candidates for each, most reliable
 * first.
 */
@Value
public class ScmContentSource {

    /** The internal repository URL used by the build, then the repository's current internal URL if different. */
    List<String> urls;

    /** Revisions of the built sources (archive). */
    List<String> revisions;

    /** Pre-alignment revisions (diff base). */
    List<String> bases;

    /** Post-alignment revisions (diff head). */
    List<String> heads;

    @Builder
    private ScmContentSource(List<String> urls, List<String> revisions, List<String> bases, List<String> heads) {
        this.urls = copy(urls);
        this.revisions = copy(revisions);
        this.bases = copy(bases);
        this.heads = copy(heads);
    }

    private static List<String> copy(List<String> values) {
        return values == null ? List.of() : List.copyOf(values);
    }
}
