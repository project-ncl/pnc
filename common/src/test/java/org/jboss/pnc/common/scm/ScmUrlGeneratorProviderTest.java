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
package org.jboss.pnc.common.scm;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.SCMProvider.GERRIT;
import static org.jboss.pnc.common.scm.ScmUrlGeneratorProvider.SCMProvider.GITLAB;

public class ScmUrlGeneratorProviderTest {

    @Test
    public void shouldReturnGITLAB() throws ScmException {
        String scmUrl;
        String internalUrl;

        // scmUrl contains gitlab
        scmUrl = "https://gitlab.com/workspace/project/repository.git";
        internalUrl = "git+ssh://git@localhost.com/workspace/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GITLAB);

        // internalUrl contains gitlab
        scmUrl = "https://localhost.com/workspace/project/repository.git";
        internalUrl = "git+ssh://git@gitlab.com/workspace/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GITLAB);

        // internal is scp-like
        scmUrl = "https://localhost.com/workspace/project/repository.git";
        internalUrl = "git@localhost.com:workspace/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GITLAB);
    }

    @Test
    public void shouldReturnGERRIT() throws ScmException {
        String scmUrl;
        String internalUrl;

        // scmUrl contains gerrit
        scmUrl = "https://gerrit.com/workspace/project/repository.git";
        internalUrl = "git+ssh://git@localhost.com/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GERRIT);

        // scmUrl contains gerrit path
        scmUrl = "https://localhost.com/gerrit/project/repository.git";
        internalUrl = "git+ssh://git@localhost.com/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GERRIT);

        // internalUrl contains gerrit
        scmUrl = "https://localhost.com/workspace/project/repository.git";
        internalUrl = "git+ssh://git@gerrit.com/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GERRIT);

        // internal is scp-like but contains gerrit
        scmUrl = "https://localhost.com/workspace/project/repository.git";
        internalUrl = "git@gerrit.com:workspace/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GERRIT);

        // internal is non-scp + no other indicator should default to GERRIT
        scmUrl = "https://localhost.com/workspace/project/repository.git";
        internalUrl = "git+ssh://git@localhost.com/workspace/project/repository.git";

        assertThat(ScmUrlGeneratorProvider.determineScmProvider(scmUrl, internalUrl)).isEqualTo(GERRIT);
    }

    @Test
    public void testInternalScmProviderMethod() throws Exception {

        String internalUrl = "git+ssh://git@gitlab.com/workspace/project/repository.git";
        assertThat(ScmUrlGeneratorProvider.determineInternalScmProvider(internalUrl)).isEqualTo(GITLAB);

        internalUrl = "git@gitlab.com:workspace/project/repository.git";
        assertThat(ScmUrlGeneratorProvider.determineInternalScmProvider(internalUrl)).isEqualTo(GITLAB);

        internalUrl = "git+ssh://git@gerrit.com/project/repository.git";
        assertThat(ScmUrlGeneratorProvider.determineInternalScmProvider(internalUrl)).isEqualTo(GERRIT);

        internalUrl = "git@gerrit.com:workspace/project/repository.git";
        assertThat(ScmUrlGeneratorProvider.determineInternalScmProvider(internalUrl)).isEqualTo(GERRIT);
    }
}
