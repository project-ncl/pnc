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
package org.jboss.pnc.enums;

/**
 * Status of the process of milestone closing.
 *
 * Author: Michal Szynkiewicz, michal.l.szynkiewicz@gmail.com Date: 8/30/16 Time: 1:16 PM
 * 
 * @deprecated use pnc-api
 */
@Deprecated
public enum MilestoneCloseStatus {

    /**
     * Milestone close is in progress.
     */
    IN_PROGRESS,
    /**
     * Milestone close failed because of user-side issue.
     */
    FAILED,
    /**
     * Milestone close finished successfully and builds were pushed to Koji.
     */
    SUCCEEDED,
    /**
     * Milestone close was canceled.
     */
    CANCELED,
    /**
     * Milestone close failed because of server-side issue.
     */
    SYSTEM_ERROR
}
