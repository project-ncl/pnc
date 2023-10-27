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
package org.jboss.pnc.facade.providers.api;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UserRoles {

    /** Role used by all PNC human admins */
    public static final String USERS_ADMIN = "pnc-users-admin";

    /** Role used by humans / service accounts that modify artifacts */
    public static final String USERS_ARTIFACT_ADMIN = "pnc-users-artifact-admin";

    /** Role used by humans / service accounts to delete temporary builds */
    public static final String USERS_BUILD_DELETE = "pnc-users-build-delete";

    /** Role used by humans / service accounts to create / change builds (including deletion) */
    public static final String USERS_BUILD_ADMIN = "pnc-users-build-admin";

    /** Role used by humans / service accounts to create / change environment */
    public static final String USERS_ENVIRONMENT_ADMIN = "pnc-users-environment-admin";

    /**
     * User's with this role are routed to new implementations that usually run in parallel to the old one (blue/green
     * testing).
     */
    public static final String WORK_WITH_TECH_PREVIEW = "work-with-tech-preview";
}
