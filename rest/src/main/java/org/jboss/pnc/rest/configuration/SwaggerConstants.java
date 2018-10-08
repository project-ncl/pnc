/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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


/**
 * Constants for Swagger documentation API.
 *
 * @author Sebastian Laskawiec
 */
public interface SwaggerConstants {

    public static final String SUCCESS_DESCRIPTION = "Success with results";
    public static final int SUCCESS_CODE = 200;

    public static final String ENTITY_CREATED_DESCRIPTION = "Entity successfully created";
    public static final int ENTITY_CREATED_CODE = 200;

    public static final String NO_CONTENT_DESCRIPTION = "Success but no content provided";
    public static final int NO_CONTENT_CODE = 204;

    public static final String INVALID_DESCRIPTION = "Invalid input parameters or validation error";
    public static final int INVALID_CODE = 400;

    public static final String CONFLICTED_DESCRIPTION = "Conflict while saving an entity";
    public static final int CONFLICTED_CODE = 409;

    public static final String SERVER_ERROR_DESCRIPTION = "Server error";
    public static final int SERVER_ERROR_CODE = 500;

    public static final String FORBIDDEN_DESCRIPTION = "User must be logged in.";
    public static final int FORBIDDEN_CODE = 403;

    public static final String NOT_FOUND_DESCRIPTION = "Can not find specified result";
    public static final int NOT_FOUND_CODE = 404;

    public static final String PAGE_INDEX_DESCRIPTION = "Page Index";
    public static final String PAGE_INDEX_QUERY_PARAM = "pageIndex";
    public static final String PAGE_INDEX_DEFAULT_VALUE = "0";

    public static final String PAGE_SIZE_DESCRIPTION = "Pagination size";
    public static final String PAGE_SIZE_QUERY_PARAM = "pageSize";
    public static final String PAGE_SIZE_DEFAULT_VALUE = "50";

    public static final String SORTING_DESCRIPTION = "Sorting RSQL";
    public static final String SORTING_QUERY_PARAM = "sort";

    public static final String QUERY_DESCRIPTION = "RSQL Query";
    public static final String QUERY_QUERY_PARAM = "q";

    public static final String SEARCH_DESCRIPTION = "Since this endpoint does not support queries, " +
            "fulltext search is hard-coded for some predefined fields (record id, configuration name) " +
            "and performed using this argument. " +
            "Empty string leaves all data unfiltered.";
    public static final String SEARCH_QUERY_PARAM = "search";
    public static final String SEARCH_DEFAULT_VALUE = "";
}
