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

import org.jboss.pnc.common.net.GitSCPUrl;

import javax.enterprise.context.ApplicationScoped;
import javax.validation.constraints.NotNull;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Class to deal with Gerrit/Gitlab information
 */
@ApplicationScoped
public class ScmUrlGeneratorProvider {

    public enum SCMProvider {
        GERRIT, GITLAB
    }

    private static final Map<SCMProvider, ScmUrlGenerator> scmUrlProviders = Map
            .of(SCMProvider.GERRIT, new GerritScmUrlGenerator(), SCMProvider.GITLAB, new GitlabScmUrlGenerator());

    /**
     * Returns an SCM url generator specific to a type of SCM
     *
     * @param provider type of SCM provider (Gerrit, Gitlab)
     * @return generator of urls specific to the type of provider
     * @throws ScmException throws if provider is unknown
     */
    public ScmUrlGenerator getScmUrlGenerator(@NotNull SCMProvider provider) throws ScmException {
        var generator = scmUrlProviders.get(provider);
        if (generator == null) {
            throw new ScmException("Unknown SCM provider: " + provider);
        }
        return generator;
    }

    /**
     * Simple heuristic logic for trying to determine what is the SCM provider out of url used to clone the build and
     * internal representation of scm repo from the SCMRepository entity.
     *
     * @param scmUrl url used to clone repo in a Build (URI only)
     * @param internalScmUrl internalUrl from SCMRepository of the Build (can be URI or SCP-like)
     * @return SCMProvider that is likely to have been used
     * @throws ScmException if the arguments are not appropriate URLs
     */
    public SCMProvider determineScmProvider(String scmUrl, String internalScmUrl) throws ScmException {
        URI scmUri;
        try {
            scmUri = new URI(scmUrl);
        } catch (URISyntaxException e) {
            throw new ScmException("Cannot parse url of " + scmUrl);
        }

        // simple logic based on host name
        if (scmUri.getHost().contains("gitlab")) {
            return SCMProvider.GITLAB;
        } else if (scmUri.getHost().contains("gerrit") || scmUri.getPath().startsWith("/gerrit/")) {
            return SCMProvider.GERRIT;
        }

        // try some logic from internalScmUrl
        if (isScpLike(internalScmUrl)) {
            return determineFromScpUri(internalScmUrl);
        } else {
            return determineFromUri(internalScmUrl);
        }
    }

    private static SCMProvider determineFromScpUri(String internalScmUrl) throws ScmException {
        try {
            GitSCPUrl scpUrl = GitSCPUrl.parse(internalScmUrl);

            if (scpUrl.getHost().contains("gitlab")) {
                return SCMProvider.GITLAB;
            } else if (scpUrl.getHost().contains("gerrit")) {
                return SCMProvider.GERRIT;
            }

            // gitlab urls tend to be SCP like
            return SCMProvider.GITLAB;
        } catch (MalformedURLException e) {
            throw new ScmException("Cannot parse url of " + internalScmUrl);
        }
    }

    private static SCMProvider determineFromUri(String internalScmUrl) throws ScmException {
        try {
            URI internalUri = new URI(internalScmUrl);

            if (internalUri.getHost().contains("gitlab")) {
                return SCMProvider.GITLAB;
            } else if (internalUri.getHost().contains("gerrit")) {
                return SCMProvider.GERRIT;
            }

            // gerrit urls tend to be regular uri format
            return SCMProvider.GERRIT;
        } catch (URISyntaxException e) {
            throw new ScmException("Cannot parse url of " + internalScmUrl);
        }
    }

    private boolean isScpLike(String internalScmUrl) {
        try {
            GitSCPUrl.parse(internalScmUrl);
        } catch (MalformedURLException e) {
            return false;
        }
        return true;
    }
}
