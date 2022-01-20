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
package org.jboss.pnc.common.gerrit;

import org.junit.Test;

import static org.junit.Assert.*;

public class GerritTest {

    private Gerrit gerrit = new Gerrit();

    @Test
    public void downloadUrlShouldHandleHttpCloneLink() throws GerritException {
        String url = "https://localhost/gerrit/project/repository.git";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=abcde;sf=tgz";

        assertEquals(gerrit.generateDownloadUrlWithGerritGitweb(url, "abcde"), downloadUrl);
    }

    @Test
    public void downloadUrlShouldHandleSshCloneLink() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=master;sf=tgz";

        assertEquals(gerrit.generateDownloadUrlWithGerritGitweb(url, "master"), downloadUrl);
    }

    @Test
    public void downloadUrlShouldHandleCloneLinkWithoutDotGit() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository";
        String downloadUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=snapshot;h=master;sf=tgz";

        assertEquals(gerrit.generateDownloadUrlWithGerritGitweb(url, "master"), downloadUrl);
    }

    @Test(expected = GerritException.class)
    public void downloadUrlShouldThrowGerritExceptionOnEmptyProject() throws GerritException {

        gerrit.generateDownloadUrlWithGerritGitweb("http://localhost", "master");
    }

    @Test(expected = GerritException.class)
    public void downloadUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws GerritException {

        gerrit.generateDownloadUrlWithGerritGitweb("", "master");
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleHttpCloneLink() throws GerritException {

        String url = "https://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(gerrit.generateGerritGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleSshCloneLink() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(gerrit.generateGerritGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleEmptyRef() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=summary";

        assertEquals(gerrit.generateGerritGitwebLogUrl(url, null), gitwebUrl);
        assertEquals(gerrit.generateGerritGitwebLogUrl(url, ""), gitwebUrl);
    }

    @Test
    public void gerritGitwebLogUrlShouldHandleCloneLinkWithoutDotGit() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=shortlog;h=master";

        assertEquals(gerrit.generateGerritGitwebLogUrl(url, "master"), gitwebUrl);
    }

    @Test(expected = GerritException.class)
    public void gerritGitwebLogUrlShouldThrowGerritExceptionOnEmptyProject() throws GerritException {

        gerrit.generateGerritGitwebLogUrl("http://localhost", "master");
    }

    @Test(expected = GerritException.class)
    public void gerritGitwebLogUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws GerritException {

        gerrit.generateGerritGitwebLogUrl("", "master");
    }

    @Test
    public void gerritGitwebCommitUrlShouldHandleHttpCloneLink() throws GerritException {

        String url = "https://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=commit;h=master";

        assertEquals(gerrit.generateGerritGitwebCommitUrl(url, "master"), gitwebUrl);
    }

    @Test
    public void gerritGitwebCommitUrlShouldHandleSshCloneLink() throws GerritException {

        String url = "git+ssh://localhost/gerrit/project/repository.git";
        String gitwebUrl = "https://localhost/gerrit/gitweb?p=project/repository.git;a=commit;h=master";

        assertEquals(gerrit.generateGerritGitwebCommitUrl(url, "master"), gitwebUrl);
    }

    @Test(expected = GerritException.class)
    public void gerritGitwebCommitUrlShouldThrowGerritExceptionOnEmptyProject() throws GerritException {

        gerrit.generateGerritGitwebCommitUrl("http://localhost", "master");
    }

    @Test(expected = GerritException.class)
    public void gerritGitwebCommitUrlShouldThrowGerritExceptionOnEmptyGerritUrl() throws GerritException {

        gerrit.generateGerritGitwebCommitUrl("", "master");
    }
}