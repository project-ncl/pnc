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
