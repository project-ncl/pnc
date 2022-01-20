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
package org.jboss.pnc.indyrepositorymanager;

/**
 * Constants used by the maven repository driver.
 */
public class IndyRepositoryConstants {

    /** ID used to distinguish this repository driver from other types to the rest of the PNC system. */
    public static final String DRIVER_ID = "indy-repo-driver";

    /** Name of group used to manage external artifact sources. */
    public static final String PUBLIC_GROUP_ID = "public";

    /** Name of group used to access all previous, marginally validated, build outputs. */
    public static final String UNTESTED_BUILDS_GROUP = "builds-untested";

    /** Name of group that contains common builds groups' constituents. */
    public static final String COMMON_BUILD_GROUP_CONSTITUENTS_GROUP = "builds-untested+shared-imports+public";

    /** Name of group used to access all previous temporary build outputs. */
    public static final String TEMPORARY_BUILDS_GROUP = "temporary-builds";

    /** Name of hosted repository used to store artifacts from external sources. */
    public static final String SHARED_IMPORTS_ID = "shared-imports";

}
