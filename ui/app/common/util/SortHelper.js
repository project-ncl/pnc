/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

(function () {
  'use strict';

  var module = angular.module('pnc.common.util');


  module.factory('SortHelper', function () {

    /* default setting for sort configuration */
    const DEFAULT_SORT_CONFIG = {
      projectsList: { field: { id: 'name', title: 'Name' }, asc: true },
      productsList: { field: { id: 'name', title: 'Name' }, asc: true },
      buildConfigsList: { field: { id: 'name', title: 'Name' }, asc: true },
      groupConfigsList: { field: { id: 'name', title: 'Name' }, asc: true },
      groupConfigsDataTable: { field: { id: 'name', title: 'Name' }, asc: true },
      scmRepositoriesList: { field: { id: 'internalUrl', title: 'Internal URL' }, asc: true },
      artifactsList: { field: { id: 'identifier', title: 'Identifier' }, asc: true },
      buildsList: { field: { id: 'submitTime', title: 'Submit Time' }, asc: false },
      groupBuildsList: { field: { id: 'startTime', title: 'Start Time' }, asc: false },
    };

    var helper = {};

    /**
     * The helper that read the default config from DEFAULT_SORT_CONFIG and return it in json format
     * @pageName: The name of the sorting page;
     * returns: A JSON formatted object of sort config that should be used for current page
     */
    helper.getSortConfig = function (pageName) {
      return DEFAULT_SORT_CONFIG[pageName];
    };

    /**
     * The helper that read the default config from DEFAULT_SORT_CONFIG and return it as RSQL string
     * @pageName: The name of the sorting page;
     * returns: An RSQL string from sort config that sends to back end for initial query
     */
    helper.getSortQueryString = function (pageName) {
      let sortConfig = DEFAULT_SORT_CONFIG[pageName];
      return {
        sort: '=' + (sortConfig.asc ? 'asc' : 'desc') + '=' + sortConfig.field.id
      };
    };

    return helper;
  });

})();
