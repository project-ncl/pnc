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
package org.jboss.pnc.enums;

/**
 * Types of artifact repositories.
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum RepositoryType {
    /**
     * Maven artifact repository such as Maven central (http://central.maven.org/maven2/).
     */
    MAVEN,
    /**
     * Node.js package repository such as https://registry.npmjs.org/.
     */
    NPM,
    /**
     * CocoaPod repository for managing Swift and Objective-C Cocoa dependencies.
     */
    COCOA_POD,
    /**
     * Generic HTTP proxy that captures artifacts with an unsupported, or no specific, repository type.
     */
    GENERIC_PROXY,
    /**
     * Artifacts which are not found in other repositories but are present in a distribution archive.
     */
    DISTRIBUTION_ARCHIVE

}
