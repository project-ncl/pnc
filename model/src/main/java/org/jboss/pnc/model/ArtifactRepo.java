/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
package org.jboss.pnc.model;

/**
 * Contains information related to a repository of build artifacts (i.e. Maven, NPM, etc)
 */
public class ArtifactRepo {

    /**
     * Types of artifact repositories
     */
    public enum Type {
        /**
         * Maven artifact repository such as Maven central (http://central.maven.org/maven2/)
         */
        MAVEN,

        /**
         * Node.js package repository such as https://registry.npmjs.org/
         */
        NPM,

        /**
         * CocoaPod repository for managing Swift and Objective-C Cocoa dependencies
         */
        COCOA_POD,

        /**
         * Generic HTTP proxy that captures artifacts with an unsupported, or no specific, repository type.
         */
        GENERIC_PROXY
    }

    /**
     * List of trusted repository URLs.  These repositories contain only artifacts which have been built
     * from source in a trusted build environment.
     */
    public static final String [] TRUSTED_REPOSITORY_URLS = {
            "http://mead/eap6",
            "http://mead/eap7"
    };

    /**
     * Check if a given artifact originates from a trusted source.  Compares the given artifactOriginUrl
     * to the list of trusted repositories.
     *
     * @param artifactOriginUrl The URL from which the artifact was downloaded
     * @return true if the artifact url came from a trusted repo, false otherwise
     */
    public static boolean isTrusted(String artifactOriginUrl) {
        if (artifactOriginUrl == null || artifactOriginUrl.isEmpty()) {
            return false;
        }
        for (String trustedRepoUrl : TRUSTED_REPOSITORY_URLS) {
            if (artifactOriginUrl.startsWith(trustedRepoUrl)) {
                return true;
            }
        }
        return false;
    }

}
