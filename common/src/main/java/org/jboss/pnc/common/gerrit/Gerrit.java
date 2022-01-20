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

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Class to deal with Gerrit information
 */
@ApplicationScoped
public class Gerrit {

    private static final String GERRIT_DOWNLOAD_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=snapshot;h={2};sf=tgz";
    private static final String GERRIT_GITWEB_SHORTLOG_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=shortlog;h={2}";
    private static final String GERRIT_GITWEB_COMMIT_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=commit;h={2}";
    private static final String GERRIT_GITWEB_URL_TEMPLATE = "https://{0}/gerrit/gitweb?p={1};a=summary";

    /**
     * Generate a download url for a Gerrit snapshot using the gerrit url of the project and the ref
     *
     * @param gerritUrl URL has to be the 'git clone' link (either http or git+ssh)
     * @param ref The ref to generate a snapshot. It can be a sha, branch, tag. If left empty, master is used
     *
     * @return Download url
     * @throws GerritException thrown if the Gerrit Url is not valid
     */
    public String generateDownloadUrlWithGerritGitweb(@NotNull String gerritUrl, @NotNull String ref)
            throws GerritException {

        return MessageFormat.format(GERRIT_DOWNLOAD_URL_TEMPLATE, getHost(gerritUrl), getProject(gerritUrl), ref);
    }

    /**
     * Generate an https URL generated from the 'git clone' Gerrit url. If ref is specified, URL will point to it The
     * url generated points to the git log based on the ref
     *
     * Useful to list the repository's Gerrit gitweb if we don't know the ref
     *
     * @param gerritUrl 'git clone' gerrit url. Can be http or git+ssh
     *
     * @param ref if not null or empty, this is used to generate a Gerrit gitweb url to the ref
     *
     * @return Gerrit Gitweb URL
     */
    public String generateGerritGitwebLogUrl(@NotNull String gerritUrl, String ref) throws GerritException {

        String host = getHost(gerritUrl);
        String project = getProject(gerritUrl);

        if (ref == null || ref.isEmpty()) {
            return MessageFormat.format(GERRIT_GITWEB_URL_TEMPLATE, host, project);
        } else {
            return MessageFormat.format(GERRIT_GITWEB_SHORTLOG_URL_TEMPLATE, host, project, ref);
        }
    }

    /**
     * Generate an https URL generated from the 'git clone' Gerrit url. The url generated points to the specific commit
     * based on the ref. If ref is a branch, it'll point to the latest commit
     *
     * @param gerritUrl 'git clone' gerrit url. Can be http or git+ssh
     *
     * @param ref this is used to generate a Gerrit gitweb url to the ref
     *
     * @return Gerrit Gitweb URL
     */
    public String generateGerritGitwebCommitUrl(@NotNull String gerritUrl, @NotNull String ref) throws GerritException {

        return MessageFormat.format(GERRIT_GITWEB_COMMIT_URL_TEMPLATE, getHost(gerritUrl), getProject(gerritUrl), ref);
    }

    private URI getURI(String gerritUrl) throws GerritException {

        try {
            return new URI(gerritUrl);
        } catch (URISyntaxException e) {
            throw new GerritException("Gerrit URL: " + gerritUrl + " cannot be parsed as a url!", e);
        }
    }

    private String getHost(String gerritUrl) throws GerritException {
        return getURI(gerritUrl).getHost();
    }

    private String getProject(String gerritUrl) throws GerritException {

        URI uri = getURI(gerritUrl);
        String project = uri.getPath();

        // remove the gerrit part in the path
        project = project.replaceFirst("/gerrit", "");

        validatePathNotEmpty(project, "The project is not specified in the Gerrit Url: " + gerritUrl);

        // remove leading slash in path
        project = project.substring(1);

        // add the '.git' in project if missing
        if (!project.endsWith(".git")) {
            project = project + ".git";
        }

        return project;
    }

    private void validatePathNotEmpty(String toValidate, String reasonFailure) throws GerritException {

        if (toValidate == null || toValidate.isEmpty() || toValidate.equals("/")) {
            throw new GerritException(reasonFailure);
        }
    }
}
