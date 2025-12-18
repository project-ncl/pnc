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
package org.jboss.pnc.spi.datastore.predicates;

import org.jboss.pnc.common.Strings;
import org.jboss.pnc.common.Urls;
import org.jboss.pnc.model.RepositoryConfiguration;
import org.jboss.pnc.model.RepositoryConfiguration_;
import org.jboss.pnc.spi.datastore.repositories.api.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RepositoryConfigurationPredicates {

    private static Logger logger = LoggerFactory.getLogger(RepositoryConfigurationPredicates.class);

    public static Predicate<RepositoryConfiguration> withExactInternalScmRepoUrl(String internalUrl) {
        return (root, query, cb) -> cb.equal(root.get(RepositoryConfiguration_.internalUrl), internalUrl);
    }

    /**
     * Queries against normalized version with stripped protocol and .git extension
     */
    public static Predicate<RepositoryConfiguration> withInternalScmRepoUrl(String internalUrl) {
        String urlStripped = Strings.stripSuffix(Urls.keepHostAndPathOnly(internalUrl), ".git");
        return (root, query, cb) -> cb.equal(root.get(RepositoryConfiguration_.internalUrlNormalized), urlStripped);
    }

    /**
     * Queries against normalized version with stripped protocol and .git extension
     */
    public static Predicate<RepositoryConfiguration> withExternalScmRepoUrl(String externalScmRepoUrl) {
        String urlStripped = Strings.stripSuffix(Urls.keepHostAndPathOnly(externalScmRepoUrl), ".git");
        return (root, query, cb) -> cb.equal(root.get(RepositoryConfiguration_.externalUrlNormalized), urlStripped);
    }

    public static Predicate<RepositoryConfiguration> searchByScmUrl(String scmUrl) {
        String urlStripped = Urls.keepHostAndPathOnly(scmUrl);
        urlStripped = Strings.stripSuffix(urlStripped, ".git");

        String pattern = "%" + urlStripped + "%";
        logger.trace("Searching for pattern: {}.", pattern);

        return (root, query, cb) -> cb.or(
                cb.like(root.get(RepositoryConfiguration_.internalUrlNormalized), pattern),
                cb.like(root.get(RepositoryConfiguration_.externalUrlNormalized), pattern));
    }

    public static Predicate<RepositoryConfiguration> matchByScmUrl(String scmUrl) {
        final String urlStripped = Strings.stripSuffix(Urls.keepHostAndPathOnly(scmUrl), ".git");

        logger.trace("Searching for pattern: {}.", urlStripped);

        return (root, query, cb) -> cb.or(
                cb.equal(root.get(RepositoryConfiguration_.internalUrlNormalized), urlStripped),
                cb.equal(root.get(RepositoryConfiguration_.externalUrlNormalized), urlStripped));
    }
}
