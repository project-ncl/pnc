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

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

public class GitlabScmUrlGenerator implements ScmUrlGenerator {

    private static final String GITLAB_DOWNLOAD_URL_TEMPLATE = "https://{0}/-/archive/{1}/sources.tar.gz";

    @Override
    public String generateDownloadUrlWithGitweb(@NotNull String scmUrl, @NotNull String ref) throws ScmException {
        return MessageFormat.format(GITLAB_DOWNLOAD_URL_TEMPLATE, preProcess(scmUrl), ref);
    }

    private String preProcess(String scmUrl) throws ScmException {
        String path;
        String host;
        try {
            URI uri = new URI(scmUrl);

            host = uri.getHost();
            path = getPathWithoutSuffix(uri);
        } catch (URISyntaxException e) {
            throw new ScmException("URL: " + scmUrl + " cannot be parsed as a url!", e);
        }

        return host + path;
    }

    private static String getPathWithoutSuffix(URI uri) {
        if (uri.getPath().endsWith(".git")) {
            return uri.getPath().replace(".git", "");
        }

        return uri.getPath();
    }

    @Override
    public String generateGitwebLogUrl(String scmUrl, String ref) throws ScmException {
        // TODO do we need to even implement this?
        throw new UnsupportedOperationException("Log gitweb url is not yet implemented for GITLAB scm type.");
    }

    @Override
    public String generateGitwebCommitUrl(String scmUrl, String ref) throws ScmException {
        // TODO do we need to even implement this?
        throw new UnsupportedOperationException("Commit gitweb url is not yet implemented for GITLAB scm type.");
    }
}
