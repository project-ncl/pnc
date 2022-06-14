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
 * Enum describing asynchonous job types in notifications.
 * 
 * @author Honza Brázdil &lt;jbrazdil@redhat.com&gt;
 * @deprecated use pnc-api
 */
@Deprecated
public enum JobNotificationType {
    /**
     * Job type representing single build.
     */
    BUILD,
    /**
     * Job type representing group build.
     */
    GROUP_BUILD,
    /**
     * Job type representing import of a build into Brew.
     */
    BREW_PUSH,
    /**
     * Job type representing asynchronous creation of SCM Repository.
     */
    SCM_REPOSITORY_CREATION,
    /**
     * Job type representing asynchronous creation of Build Config together with SCM Repository.
     */
    BUILD_CONFIG_CREATION,
    /**
     * Job type representing generic operation-type job.
     */
    OPERATION,
    /**
     * Job type representing a change in the generic setting.
     */
    GENERIC_SETTING,
    /**
     * Job type representing closing of milestone
     */
    PRODUCT_MILESTONE_CLOSE,
}
