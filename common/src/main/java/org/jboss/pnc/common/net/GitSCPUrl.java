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

import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used only for parsing git urls with scp-like syntax.
 *
 * e.g. git@github.com:project-ncl/pnc.git
 */
public class GitSCPUrl {
    private String user;
    private String host;
    private String path;
    private String owner;
    private String repositoryName;

    private static Pattern scpPattern = Pattern.compile(
            "^((?<user>\\w+)@)?(?<host>[-.\\w]+)[:/]{1,2}(?<path>((?<owner>[-\\w]+)/)*((?<reponame>[-\\w]+)(.git)?))[/]?$");

    private GitSCPUrl(String user, String host, String path, String owner, String repositoryName) {
        this.user = user;
        this.host = host;
        this.path = path;
        this.owner = owner;
        this.repositoryName = repositoryName;
    }

    public static GitSCPUrl parse(String url) throws MalformedURLException {
        if (url == null) {
            throw new IllegalArgumentException("Supplied URL cannot be null");
        }

        Matcher matcher = scpPattern.matcher(url);
        if (!matcher.matches()) {
            throw new MalformedURLException("Supplied URL is in incompatible format");
        }

        return new GitSCPUrl(
                matcher.group("user"),
                matcher.group("host"),
                matcher.group("path"),
                matcher.group("owner"),
                matcher.group("reponame"));
    }

    public String getUser() {
        return user;
    }

    public String getHost() {
        return host;
    }

    public String getPath() {
        return path;
    }

    public String getOwner() {
        return owner;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getHostWithPath() {
        return host + "/" + path;
    }
}
