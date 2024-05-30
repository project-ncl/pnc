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
package org.jboss.pnc.common.net;

import org.junit.Test;

import java.net.MalformedURLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class GitSCPUrlTest {

    @Test
    public void shouldCorrectlyParse() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:project-ncl/pnc.git");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/pnc.git");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/project-ncl/pnc.git");
    }

    @Test
    public void shouldParseWithoutGitSuffix() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:project-ncl/pnc");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/pnc");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/project-ncl/pnc");
    }

    @Test
    public void shouldParseWithMultipleDomains() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@some.other.github.instance.com:project-ncl/pnc.git");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("some.other.github.instance.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/pnc.git");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("some.other.github.instance.com/project-ncl/pnc.git");
    }

    @Test
    public void shouldParseWithTrailingSlash() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:project-ncl/pnc/");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/pnc");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/project-ncl/pnc");
    }

    @Test
    public void shouldParseWithoutUser() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("github.com:project-ncl/pnc.git");

        assertThat(url.getUser()).isNull();
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/pnc.git");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/project-ncl/pnc.git");
    }

    @Test
    public void shouldParseWithHyphensInRepositoryName() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("github.com:project-ncl/build-finder.git");

        assertThat(url.getUser()).isNull();
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("project-ncl/build-finder.git");
        assertThat(url.getOwner()).isEqualTo("project-ncl");
        assertThat(url.getRepositoryName()).isEqualTo("build-finder");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/project-ncl/build-finder.git");
    }

    @Test
    public void shouldParseWithoutOwner() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:/pnc.git");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("pnc.git");
        assertThat(url.getOwner()).isNull();
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/pnc.git");
    }

    @Test
    public void shouldParseWithoutOwnerAndSlash() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:pnc.git");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("pnc.git");
        assertThat(url.getOwner()).isNull();
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/pnc.git");
    }

    @Test
    public void shouldParseWithoutOwnerAndSlashAndGitSuffix() throws Exception {
        GitSCPUrl url = GitSCPUrl.parse("git@github.com:pnc");

        assertThat(url.getUser()).isEqualTo("git");
        assertThat(url.getHost()).isEqualTo("github.com");
        assertThat(url.getPath()).isEqualTo("pnc");
        assertThat(url.getOwner()).isNull();
        assertThat(url.getRepositoryName()).isEqualTo("pnc");
        assertThat(url.getHostWithPath()).isEqualTo("github.com/pnc");
    }

    @Test
    public void shouldFailOnNull() throws Exception {
        assertThatThrownBy(() -> GitSCPUrl.parse(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void shouldNotParseInvalidFormat() throws Exception {
        assertThatThrownBy(() -> GitSCPUrl.parse("git@:project-ncl/pnc")).isInstanceOf(MalformedURLException.class);
        assertThatThrownBy(() -> GitSCPUrl.parse(":project-ncl/pnc/")).isInstanceOf(MalformedURLException.class);
        assertThatThrownBy(() -> GitSCPUrl.parse("")).isInstanceOf(MalformedURLException.class);
    }

    @Test
    public void shouldNotParseNonSCPFormat() throws Exception {
        assertThatThrownBy(() -> GitSCPUrl.parse("https://github.com/project-ncl/pnc.git"))
                .isInstanceOf(MalformedURLException.class);
        assertThatThrownBy(() -> GitSCPUrl.parse("git://lots.of.domains.github.com/gerrit/c3p0.git"))
                .isInstanceOf(MalformedURLException.class);
    }

    /**
     * Gitlab naming convention:
     *
     * Must start with a lowercase or uppercase letter, digit, emoji, or underscore. Can also contain dots, pluses,
     * dashes, or spaces.
     */
    @Test
    public void testSpecialCases() throws Exception {

        GitSCPUrl url = GitSCPUrl.parse("git@gitlab:pnc-here/Ecli+p_se/vert5.x.git");
        assertThat(url.getOwner()).isEqualTo("Ecli+p_se");
        assertThat(url.getRepositoryName()).isEqualTo("vert5.x");
    }
}
