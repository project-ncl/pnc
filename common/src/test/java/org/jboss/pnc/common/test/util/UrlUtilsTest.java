/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

        url = "project-ncl";
        Assert.assertEquals("project-ncl", UrlUtils.keepHostAndPathOnly(url));

        url = "gitserver.host.com:80/productization/github.com/jboss-modules.git";
        Assert.assertEquals(
                "gitserver.host.com/productization/github.com/jboss-modules.git",
                UrlUtils.keepHostAndPathOnly(url));
    }
}
