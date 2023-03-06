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
package org.jboss.pnc.rest.configuration;

import static org.jboss.pnc.rest.configuration.Constants.MAX_PAGE_SIZE;

/**
 * Constants for Swagger documentation API.
 *
 * @author Sebastian Laskawiec
 */
public interface SwaggerConstants {
    public static final String TAG_INTERNAL = "Internal";

    public static final String SUCCESS_DESCRIPTION = "Success with results";
    public static final String SUCCESS_CODE = "200";
    public static final String ENTITY_CREATED_DESCRIPTION = "Entity successfully created";
    public static final String ENTITY_CREATED_CODE = "201";
    public static final String ACCEPTED_DESCRIPTION = "Request was accepted for processing";
    public static final String ACCEPTED_CODE = "202";
    public static final String ENTITY_UPDATED_DESCRIPTION = "Entity successfully updated";
    public static final String ENTITY_UPDATED_CODE = "204";
    public static final String ENTITY_DELETED_DESCRIPTION = "Entity deleted";
    public static final String ENTITY_DELETED_CODE = "204";
    public static final String NO_CONTENT_DESCRIPTION = "Success but no content provided";
    public static final String NO_CONTENT_CODE = "204";
    public static final String INVALID_DESCRIPTION = "Invalid input parameters or validation error";
    public static final String INVALID_CODE = "400";
    public static final String FORBIDDEN_DESCRIPTION = "User must be logged in.";
    public static final String FORBIDDEN_CODE = "403";
    public static final String FORBIDDEN_PUSH_DESCRIPTION = "Build contains artifacts of insufficient quality";
    public static final String MOVED_TEMPORARILY_DESCRIPTION = "Redirected to resource";
    public static final String MOVED_TEMPORARILY_CODE = "302";
    public static final String NOT_FOUND_DESCRIPTION = "Can not find specified result";
    public static final String NOT_FOUND_CODE = "404";
    public static final String CONFLICTED_DESCRIPTION = "Conflict while saving an entity";
    public static final String CONFLICTED_CODE = "409";
    public static final String TOO_EARLY_CODE = "425";
    public static final String TOO_EARLY_DESCRIPTION = "Request arrived earlier than expected";
    public static final String SERVER_ERROR_DESCRIPTION = "Server error";
    public static final String SERVER_ERROR_CODE = "500";
    public static final String PAGE_INDEX_DESCRIPTION = "Index of the page to return. Index starts with 0.";
    public static final String PAGE_INDEX_QUERY_PARAM = "pageIndex";
    public static final String PAGE_INDEX_DEFAULT_VALUE = "0";
    public static final String PAGE_SIZE_DESCRIPTION = "Number of entries that should be included in a page. Maximum page size is "
            + MAX_PAGE_SIZE + ".";
    public static final String PAGE_SIZE_QUERY_PARAM = "pageSize";
    public static final String PAGE_SIZE_DEFAULT_VALUE = "50";
    public static final String SORTING_DESCRIPTION = "Sorting RSQL. Format: sort=asc=path.to.field (or =desc=).";
    public static final String SORTING_QUERY_PARAM = "sort";
    public static final String QUERY_DESCRIPTION = "RSQL Query.";
    public static final String QUERY_QUERY_PARAM = "q";
    public static final String SEARCH_QUERY_PARAM = "search-url";
    public static final String MATCH_QUERY_PARAM = "url";
    public static final String SEARCH_DEFAULT_VALUE = "";

    public static final String SCM_REPOSITORY_CREATED = "SCM Repository was created. The 'repository' key is populated with "
            + "the details of the SCM Repository ('taskId' key is null)";
    public static final String SCM_REPOSITORY_CREATING = "SCM Repository is being created. The 'taskId' key has the id of "
            + "the Maitai process instance creating the repository ('repository' key is null)";
    public static final String BUILD_CONFIG_CREATED = "Build Config was created. The 'buildConfig' key is populated with "
            + "the details of the Build Config ('taskId' key is null)";
    public static final String BUILD_CONFIG_CREATING = "Build Config is being created. The 'taskId' key has the id of "
            + "the Maitai process instance creating the repository ('buildConfig' key is null)";

    public static final String TEMPORARY_BUILD_DESC = "Is it a temporary build or a standard build?";
    public static final String TIMESTAMP_ALIGNMENT_DESC = "This feature was disabled. Setting this value has no effect on the build.";
    public static final String REBUILD_MODE_DESC = "What should varant rebuild?";
    public static final String DEFAULT_REBUILD_MODE = "IMPLICIT_DEPENDENCY_CHECK";
    public static final String BUILD_DEPENDENCIES_DESC = "Should we build also dependencies of this Build Config?";
    public static final String KEEP_POD_ON_FAIL_DESC = "Should we keep the build container running, if the build fails?";
    public static final String LATEST_BUILD_DESC = "Should return only latest build?";
    public static final String RUNNING_BUILDS_DESC = "Should return only running builds?";
    public static final String LATEST_MILESTONE_CLOSE_DESC = "Should return only latest milestone close result?";
    public static final String RUNNING_MILESTONE_CLOSE_DESC = "Should return only running milestone close result?";
    public static final String BC_NAME_FILTER_DESC = "Filters builds by BuildConfig name in a revision used to trigger the build. Supports LIKE queries in style *name-part*";
    public static final String REQUIRES_ADMIN = "Requires user to have admin role.";
    public static final String CALLBACK_URL = "Optional Callback URL";
    public static final String ALIGNMENT_PREFERENCE_DESC = "Defines temporary build dependency alignment preferences. Default: PREFER_TEMPORARY.";
}
