/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.constants;

/**
 *
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public class Patterns {

    /**
     * Version that consists of a major, minor and micro numeric version followed by an alphanumeric qualifier. Micro
     * version can be left out in special cases. For example 1.0.0.ER1, 1.2.10.CR1, 1.0.0.CD1.CR1, 1.0.CR1 See
     * org.jboss.pnc.constants.PatternsTest for valid examples.
     */
    public static final String PRODUCT_MILESTONE_VERSION = "^[0-9]+\\.[0-9]+(\\.\\w[\\w-]*)+$";

    /**
     * Version that consists of a major and minor numeric version. For example 1.0, 1.2.
     */
    public static final String PRODUCT_STREAM_VERSION = "^[0-9]+\\.[0-9]+$";

    /**
     * See org.jboss.pnc.constants.PatternsTest for valid examples.
     */
    public static final String PRODUCT_RELEASE_VERSION = "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[\\w-]+$";

    /**
     * Product name abbreviation. May consists of letters, numbers and dash. For example AB-Foo
     */
    public static final String PRODUCT_ABBREVIATION = "[a-zA-Z0-9-]+";

    /**
     * Internal repository name pattern. The name is part following the SCM authority (hostname) in the repository URL.
     */
    public static final String INTERNAL_REPOSITORY_NAME = "([\\/:][\\w\\.:\\~_-]+)+(\\.git)(?:\\/?|\\#[\\d\\w\\.\\-_]+?)$";

}
