/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
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
    DISTRIBUTION_ARCHIVE,
    /**
     * RPM artifacts repository.
     */
    RPM
}
