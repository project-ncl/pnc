/*
 * SPDX-FileCopyrightText: Copyright © 2014 Red Hat, Inc., and individual contributors as indicated by the @author tags.
 * SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.pnc.facade.providers.api;

/**
 * @author <a href="mailto:matejonnet@gmail.com">Matej Lazar</a>
 */
public class UserRoles {

    /** Role for regular PNC users (required for creation/update operations) */
    public static final String USERS = "pnc-users";

    /** Role used by all PNC human admins */
    public static final String USERS_ADMIN = "pnc-users-admin";

    /** Role used by humans / service accounts that modify artifacts */
    public static final String USERS_ARTIFACT_ADMIN = "pnc-app-artifact-user";

    /** Role used by service accounts to read artifacts */
    public static final String USERS_ARTIFACT_READ = "pnc-app-orch-artifact-read";

    /** Role used by humans / service accounts to delete temporary builds */
    public static final String USERS_BUILD_DELETE = "pnc-app-orch-build-delete";

    /** Role used by service accounts to read builds */
    public static final String USERS_BUILD_READ = "pnc-app-orch-build-read";

    /** Role used by humans / service accounts to create / change builds (including deletion) */
    public static final String USERS_BUILD_ADMIN = "pnc-app-build-user";

    /** Role used by humans / service accounts to create / change environment */
    public static final String USERS_ENVIRONMENT_ADMIN = "pnc-app-environment-user";

    /** Role used by humans / service accounts to create / change attachment */
    public static final String USERS_ATTACHMENT_ADMIN = "pnc-app-attachment-user";

    /**
     * Role used for service accounts. Temporarily using it to create attachment
     */
    public static final String USERS_REX = "pnc-app-rex-user";

    /** Role used by service accounts that interact with build-task endpoints (Rex) */
    public static final String USERS_BUILD_TASKS = "pnc-app-build-tasks-user";

    /** Role used by orchestrator service accounts to process commits (BPM, operation completion) */
    public static final String USERS_ORCH_PROCESS_COMMIT = "pnc-app-orch-process-commit";

    /**
     * User's with this role are routed to new implementations that usually run in parallel to the old one (blue/green
     * testing).
     */
    public static final String WORK_WITH_TECH_PREVIEW = "work-with-tech-preview";
}
