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

  angular.module('pnc.projects').component('pncProjectsListPage', {
    bindings: {
      /**
       * Promise: resolved Projects Page Promise.
       */
      projects: '<'
    },
    templateUrl: 'projects/list/pnc-projects-list-page.html',
    controller: ['filteringPaginator', Controller]
  });

  function Controller(filteringPaginator) {
    const $ctrl = this;

    const DEFAULT_SORTING_CONFIG = {
      'field': { 'id': 'name', 'title': 'Name' },
      'asc': true
    };
    const PAGE_NAME = 'projectList';

    // -- Controller API --
    $ctrl.projectsFilteringFields = [{
      id: 'name',
      title: 'Name',
      placeholder: 'Filter by Name',
      filterType: 'text'
    }, {
      id: 'description',
      title: 'Description',
      placeholder: 'Filter by Description',
      filterType: 'text'
    }];

    $ctrl.projectsSortingFields = [{
      id: 'name',
      title: 'Name'
    }, {
      id: 'description',
      title: 'Description'
    }];

    var sortingConfigJson = window.localStorage.getItem('projectListSortingConfig');
    $ctrl.projectsSortingConfigs = sortingConfigJson ? JSON.parse(sortingConfigJson) : DEFAULT_SORTING_CONFIG;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.projectsFilteringPage = filteringPaginator($ctrl.projects);

      $ctrl.projectsFilteringPage.onSortChange(currentSortConfig => {
        var storageKey = PAGE_NAME + 'SortingConfig';
        if (currentSortConfig) {
          window.localStorage.setItem(storageKey, JSON.stringify(currentSortConfig));
        } else {
          window.localStorage.setItem(storageKey, DEFAULT_SORTING_CONFIG);
        }
      });
    };

  }

})();
