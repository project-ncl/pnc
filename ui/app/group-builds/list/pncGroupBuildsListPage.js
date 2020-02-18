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

  angular.module('pnc.group-builds').component('pncGroupBuildsListPage', {
    bindings: {
      groupBuilds: '<',
    },
    templateUrl: 'group-builds/list/pnc-group-builds-list-page.html',
    controller: ['filteringPaginator', 'SortHelper', Controller]
  });


  function Controller(filteringPaginator,sortHelper) {
    const $ctrl = this;

    const PAGE_NAME = 'groupBuildsList';

    // -- Controller API --
    $ctrl.groupBuildsFilteringFields = [{
      id: 'user.username',
      title: 'Username',
      placeholder: 'Filter by Username',
      filterType: 'text'
    }, {
      id: 'groupConfig.name',
      title: 'Group Config name',
      placeholder: 'Filter by Group Config name',
      filterType: 'text'
    }, {
      id: 'status',
      title: 'Status',
      placeholder: 'Filter by Status',
      filterType: 'select',
      filterValues: [
        'SUCCESS',
        'REJECTED',
        'FAILED',
        'CANCELLED',
        'BUILDING'
      ]
    }, {
      id: 'temporaryBuild',
      title: 'Temporary Build',
      placeholder: 'Filter by Temporary Build',
      filterType: 'select',
      filterValues: [
        'FALSE',
        'TRUE'
      ]
    }];

    $ctrl.groupBuildsSortingFields = [{
      id: 'status',
      title: 'Status',
    }, {
      id: 'groupConfig.name',
      title: 'Build Config',
    }, {
      id: 'startTime',
      title: 'Start Time'
    }, {
      id: 'endTime',
      title: 'End Time',
    }, {
      id: 'user.username',
      title: 'User',
    }];

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.groupBuildsFilteringPage = filteringPaginator($ctrl.groupBuilds);

      $ctrl.groupBuildsSortingConfigs = sortHelper.getSortConfig(PAGE_NAME);


      /* NCL-4433 group builds need to be updated
      function processEvent() {}
      $scope.$on(eventTypes.BUILD_SET_STARTED, processEvent);
      $scope.$on(eventTypes.BUILD_SET_FINISHED, processEvent);
      */
    };

  }

})();
