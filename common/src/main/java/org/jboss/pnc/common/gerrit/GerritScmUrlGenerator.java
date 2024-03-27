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

import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

public class GerritScmUrlGenerator implements ScmUrlGenerator {

    private static final String GERRIT_DOWNLOAD_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=snapshot;h={2};sf=tgz";
    private static final String GERRIT_GITWEB_SHORTLOG_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=shortlog;h={2}";
    private static final String GERRIT_GITWEB_COMMIT_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=commit;h={2}";
    private static final String GERRIT_GITWEB_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=summary";

    @Override
    public String generateDownloadUrlWithGitweb(@NotNull String scmUrl, @NotNull String ref) throws ScmException {

        return MessageFormat.format(GERRIT_DOWNLOAD_URL_TEMPLATE, getHost(scmUrl), getGerritProject(scmUrl), ref);
    }

    @Override
    public String generateGitwebLogUrl(@NotNull String scmUrl, String ref) throws ScmException {

        String host = getHost(scmUrl);
        String project = getGerritProject(scmUrl);

        if (ref == null || ref.isEmpty()) {
            return MessageFormat.format(GERRIT_GITWEB_URL_TEMPLATE, host, project);
        } else {
            return MessageFormat.format(GERRIT_GITWEB_SHORTLOG_URL_TEMPLATE, host, project, ref);
        }
    }

    @Override
    public String generateGitwebCommitUrl(@NotNull String scmUrl, @NotNull String ref) throws ScmException {

        return MessageFormat.format(GERRIT_GITWEB_COMMIT_URL_TEMPLATE, getHost(scmUrl), getGerritProject(scmUrl), ref);
    }

    private URI getURI(String scmUrl) throws ScmException {

        try {
            return new URI(scmUrl);
        } catch (URISyntaxException e) {
            throw new ScmException("URL: " + scmUrl + " cannot be parsed as a url!", e);
        }
    }

    private String getHost(String url) throws ScmException {
        return getURI(url).getHost();
    }

    private String getGerritProject(String url) throws ScmException {

        URI uri = getURI(url);
        String project = uri.getPath();

        // remove the gerrit part in the path
        project = project.replaceFirst("/gerrit", "");

        validatePathNotEmpty(project, "The project is not specified in the Gerrit Url: " + url);

        // remove leading slash in path
        project = project.substring(1);

        // add the '.git' in project if missing
        if (!project.endsWith(".git")) {
            project = project + ".git";
        }

        return project;
    }

    private void validatePathNotEmpty(String toValidate, String reasonFailure) throws ScmException {

        if (toValidate == null || toValidate.isEmpty() || toValidate.equals("/")) {
            throw new ScmException(reasonFailure);
        }
    }
}
