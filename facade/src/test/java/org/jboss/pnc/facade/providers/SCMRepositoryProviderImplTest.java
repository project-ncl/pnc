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
package org.jboss.pnc.facade.providers;

import java.util.List;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

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