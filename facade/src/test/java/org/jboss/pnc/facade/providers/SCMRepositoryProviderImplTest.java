/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SCMRepositoryProviderImplTest {

    @Test
    public void testIsWhitelistedScmGroup() {
        List<String> allowed = List.of("gitlab.example.com/lw/lw-builds", "gitlab.com/some/allowed-group");

        // under a whitelisted group (segment-aware) -> allowed, across URL forms
        assertThat(
                SCMRepositoryProviderImpl
                        .isWhitelistedScmGroup("https://gitlab.example.com/lw/lw-builds/foo.git", allowed)).isTrue();
        assertThat(
                SCMRepositoryProviderImpl
                        .isWhitelistedScmGroup("https://gitlab.example.com/lw/lw-builds/sub/bar.git", allowed))
                                .isTrue();
        assertThat(
                SCMRepositoryProviderImpl.isWhitelistedScmGroup("git@gitlab.example.com:lw/lw-builds/foo.git", allowed))
                        .isTrue();
        // the group path itself matches
        assertThat(SCMRepositoryProviderImpl.isWhitelistedScmGroup("https://gitlab.example.com/lw/lw-builds", allowed))
                .isTrue();

        // segment-aware: a sibling with the group as a mere string prefix is NOT matched
        assertThat(
                SCMRepositoryProviderImpl
                        .isWhitelistedScmGroup("https://gitlab.example.com/lw/lw-buildsX/foo.git", allowed)).isFalse();
        // different group under the same host -> not matched
        assertThat(SCMRepositoryProviderImpl.isWhitelistedScmGroup("https://gitlab.example.com/other/foo.git", allowed))
                .isFalse();
        // empty/absent whitelist -> never matched
        assertThat(SCMRepositoryProviderImpl.isWhitelistedScmGroup("https://gitlab.com/some/allowed-group/x.git", null))
                .isFalse();
        assertThat(
                SCMRepositoryProviderImpl.isWhitelistedScmGroup("https://gitlab.com/some/allowed-group/x.git", allowed))
                        .isTrue();
    }

    @Test
    public void testUsingGithubButNotScpFormat() throws Exception {

        String internalUrl = "git+ssh://git@github.company.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isTrue();

        internalUrl = "git@github.company.com:project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "https://github.company.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isTrue();

        internalUrl = "git+ssh://git@gitlab.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "git@gitlab.com:workspace/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "git+ssh://git@gerrit.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "git@gerrit.com:workspace/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGithubButNotScpFormat(internalUrl)).isFalse();
    }

}