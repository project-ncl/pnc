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

  angular.module('pnc.builds').component('pncBuildsListPage', {
    bindings: {
      builds: '<'
    },
    templateUrl: 'builds/list/pnc-builds-list-page.html',
    controller: ['$scope', 'events', 'filteringPaginator', Controller]
  });

  function Controller($scope, events, filteringPaginator) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.buildsFilteringFields = [{
      id: 'user.username',
      title: 'Username',
      placeholder: 'Filter by Username',
      filterType: 'text'
    }, {
      id: 'buildConfig.name',
      title: 'Build Config name',
      placeholder: 'Filter by Build Config name',
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
        'BUILDING',
        'NO_REBUILD_REQUIRED',
        'SYSTEM_ERROR'
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


    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.buildsFilteringPage = filteringPaginator($ctrl.builds);

      $scope.$on(events.BUILD_PROGRESS_CHANGED, () => $ctrl.buildsFilteringPage.refresh());
    };

  }

})();
