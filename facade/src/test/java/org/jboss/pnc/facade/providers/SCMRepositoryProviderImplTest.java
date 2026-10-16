/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class SCMRepositoryProviderImplTest {

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