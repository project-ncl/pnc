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

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class SCMRepositoryProviderImplTest {

    @Test
    public void testUsingGitlabButNotScpFormat() throws Exception {

        String internalUrl = "git+ssh://git@gitlab.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGitlabButNotScpFormat(internalUrl)).isTrue();

        internalUrl = "git@gitlab.com:workspace/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGitlabButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "git+ssh://git@gerrit.com/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGitlabButNotScpFormat(internalUrl)).isFalse();

        internalUrl = "git@gerrit.com:workspace/project/repository.git";
        assertThat(SCMRepositoryProviderImpl.usingGitlabButNotScpFormat(internalUrl)).isFalse();
    }

}