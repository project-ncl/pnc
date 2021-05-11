/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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
    public static final String INTERNAL_REPOSITORY_NAME = "(\\/[\\w\\.:\\~_-]+)+(\\.git)(?:\\/?|\\#[\\d\\w\\.\\-_]+?)$";

}
