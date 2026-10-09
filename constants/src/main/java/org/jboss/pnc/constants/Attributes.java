/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.constants;

/**
 * Represents attribute keys.
 *
 * @author <a href="mailto:jmichalo@redhat.com">Jan Michalov</a>
 * @deprecated use pnc-api
 */
@Deprecated
public class Attributes {

    /**
     * Attribute key for org.jboss.pnc.dto.ProductVersion representing Brew tag prefix for a Version.
     */
    public static final String BREW_TAG_PREFIX = "BREW_TAG_PREFIX";

    /**
     * Attribute key for org.jboss.pnc.dto.Build representing Brew name of the build.
     */
    public static final String BUILD_BREW_NAME = "BREW_BUILD_NAME";

    /**
     * Attribute key for org.jboss.pnc.dto.Build representing Brew version of the build.
     */
    public static final String BUILD_BREW_VERSION = "BREW_BUILD_VERSION";

    /**
     * Attribute key for org.jboss.pnc.dto.Build representing the reason for the deletion of its built artifacts.
     */
    public static final String DELETE_REASON = "DELETE_REASON";

    /**
     * Attribute key for org.jboss.pnc.dto.Build representing the reason for the blacklist of its built artifacts.
     */
    public static final String BLACKLIST_REASON = "BLACKLIST_REASON";

}
