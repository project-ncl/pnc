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

    //Todo: use regular expression to validate every Json object that stores in Local Storage, if not match just drop it

    const DEFAULT_SORT_CONFIG = {
      projectList: { field: { id: 'name', title: 'Name' }, asc: true }
    };

    var helper = {};

    /**
     * The helper that read specific sort configuration from localStorage according to the page name that passed in(the
     * first parameter). If there is no such config in the local storage, it will use the default config (the second
     * parameter) passed in.
     * @pageName: The name of the sorting page;
     * returns: An object of sort config that should be used for current page
     */
    helper.getSortConfigFromLocalStorage = function (pageName) {
      const STORAGE_KEY = pageName + 'SortingConfig';
      let sortConfigJson = window.localStorage.getItem(STORAGE_KEY);
      return sortConfigJson ? JSON.parse(sortConfigJson) : DEFAULT_SORT_CONFIG[pageName];
    };

    /**
     * The helper that set specific sort configuration to localStorage according to the page name that passed in(the
     * first parameter).
     * @pageName: The name of the sorting page;
     * @currentSortConfig: The current sort config to be saved into local storage.
     */
    helper.setSortConfigToLocalStorage = function (pageName, currentSortConfig) {
      const STORAGE_KEY = pageName + 'SortingConfig';
      if (currentSortConfig) {
        window.localStorage.setItem(STORAGE_KEY, JSON.stringify(currentSortConfig));
      }
    };

    return helper;
  });

})();
