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
package org.jboss.pnc.common.test.util;

import org.jboss.pnc.common.util.UrlUtils;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UrlUtilsTest {

    @Test
    public void shouldGetHostAndPathOnly() throws MalformedURLException {
        String url = "https://github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "https://github.com:80/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "https://github.com:85/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "ssh://git@github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git+ssh://git@github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git+ssh://git@github.com:22/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git+ssh://github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "ssh://github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git://github.com/project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git@github.com:project-ncl/pnc.git";
        Assert.assertEquals("github.com/project-ncl/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "git@github.com:project-ncl/subgroup/pnc.git";
        Assert.assertEquals("github.com/project-ncl/subgroup/pnc.git", UrlUtils.keepHostAndPathOnly(url));

        url = "project-ncl";
        Assert.assertEquals("project-ncl", UrlUtils.keepHostAndPathOnly(url));

        url = "gitserver.host.com:80/productization/github.com/jboss-modules.git";
        Assert.assertEquals(
                "gitserver.host.com/productization/github.com/jboss-modules.git",
                UrlUtils.keepHostAndPathOnly(url));
    }

    @Test
    public void shouldReplaceHostInUrl() throws MalformedURLException {
        String url = "http://example.com/test?here=no&hi=yes";
        String host = "https://localhost:8080";
        Assert.assertEquals("https://localhost:8080/test?here=no&hi=yes", UrlUtils.replaceHostInUrl(url, host));

        url = "https://test.mu";
        host = "http://localhost:1234";
        Assert.assertEquals("http://localhost:1234", UrlUtils.replaceHostInUrl(url, host));

        url = "https://test.mu/hi";
        host = "http://localhost:1234";
        Assert.assertEquals("http://localhost:1234/hi", UrlUtils.replaceHostInUrl(url, host));

        url = "https://test.mu:1234/hi";
        host = "http://localhost:81";
        Assert.assertEquals("http://localhost:81/hi", UrlUtils.replaceHostInUrl(url, host));
    }

    @Test
    public void shouldReplaceHostInUrlWithDefaultPort() throws MalformedURLException {
        String url = "http://example.com/test?here=no&hi=yes";
        String host = "https://localhost";
        Assert.assertEquals("https://localhost/test?here=no&hi=yes", UrlUtils.replaceHostInUrl(url, host));

        host = "http://localhost";
        Assert.assertEquals("http://localhost/test?here=no&hi=yes", UrlUtils.replaceHostInUrl(url, host));

        url = "https://test.mu:1234/hi";
        host = "http://localhost";
        Assert.assertEquals("http://localhost/hi", UrlUtils.replaceHostInUrl(url, host));
    }

    @Test
    public void shouldThrowExceptionReplaceMalformedHostInUrl() {
        final String url = "not-a-valid-url";
        final String host = "https://localhost";

        Assert.assertThrows(MalformedURLException.class, () -> UrlUtils.replaceHostInUrl(url, host));

        final String url2 = "http://example.com:123";
        final String host2 = "not-a-valid-url";
        Assert.assertThrows(MalformedURLException.class, () -> UrlUtils.replaceHostInUrl(url2, host2));
    }

}
