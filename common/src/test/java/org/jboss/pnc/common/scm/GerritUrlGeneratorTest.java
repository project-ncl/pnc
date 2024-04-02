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

import static org.junit.Assert.*;

public class GerritUrlGeneratorTest {

    private final GerritScmUrlGenerator scmUrlGenerator = new GerritScmUrlGenerator();

    @Test
    public void downloadUrlShouldHandleHttpCloneLink() throws ScmException {
        String url = "https://localhost/gerrit/project/repository.git";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=abcde;sf=tgz";

        assertEquals(scmUrlGenerator.generateDownloadUrlWithGitweb(url, "abcde"), downloadUrl);
    }

    @Test
    public void downloadUrlShouldHandleSshCloneLink() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=master;sf=tgz";

        assertEquals(scmUrlGenerator.generateDownloadUrlWithGitweb(url, "master"), downloadUrl);
    }

    @Test
    public void downloadUrlShouldHandleCloneLinkWithoutDotGit() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=master;sf=tgz";

        assertEquals(scmUrlGenerator.generateDownloadUrlWithGitweb(url, "master"), downloadUrl);
    }

    @Test(expected = ScmException.class)
    public void downloadUrlShouldThrowGerritExceptionOnEmptyProject() throws ScmException {

        scmUrlGenerator.generateDownloadUrlWithGitweb("http://localhost", "master");
    }

    @Test(expected = ScmException.class)
    public void downloadUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws ScmException {

        scmUrlGenerator.generateDownloadUrlWithGitweb("", "master");
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleHttpCloneLink() throws ScmException {

        String url = "https://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(scmUrlGenerator.generateGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleSshCloneLink() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(scmUrlGenerator.generateGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleEmptyRef() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=summary";

        assertEquals(scmUrlGenerator.generateGitwebLogUrl(url, null), gitwebUrl);
        assertEquals(scmUrlGenerator.generateGitwebLogUrl(url, ""), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleCloneLinkWithoutDotGit() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(scmUrlGenerator.generateGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test(expected = ScmException.class)
    public void gerritGitwebLogUrlShouldThrowGerritExceptionOnEmptyProject() throws ScmException {

        scmUrlGenerator.generateGitwebLogUrl("http://localhost", "master");
    }

    @Test(expected = ScmException.class)
    public void gerritGitwebLogUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws ScmException {

        scmUrlGenerator.generateGitwebLogUrl("", "master");
    }

    @Test
    public void gerritGitwebCommitUrlShouldHandleHttpCloneLink() throws ScmException {

        String url = "https://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=commit;h=master";

        assertEquals(scmUrlGenerator.generateGitwebCommitUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebCommitUrlShouldHandleSshCloneLink() throws ScmException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=commit;h=master";

        assertEquals(scmUrlGenerator.generateGitwebCommitUrl(url, "master"), gitwebUrl);
    }

    @Test(expected = ScmException.class)
    public void gerritGitwebCommitUrlShouldThrowGerritExceptionOnEmptyProject() throws ScmException {

        scmUrlGenerator.generateGitwebCommitUrl("http://localhost", "master");
    }

    @Test(expected = ScmException.class)
    public void gerritGitwebCommitUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws ScmException {

        scmUrlGenerator.generateGitwebCommitUrl("", "master");
    }
}