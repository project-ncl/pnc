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

  angular.module('pnc.build-groups').component('pncBuildGroupBuildConfigs', {
    bindings: {
      page: '<',
      buildGroup: '<'
    },
    templateUrl: 'build-groups/directives/pnc-build-group-build-configs/pnc-build-group-build-configs.html',
    controller: ['$scope', 'modalEditService', 'BuildConfigurationSet', '$q', Controller]
  });


  function Controller($scope, modalEditService, BuildConfigurationSet, $q) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.edit = edit;
    $ctrl.remove = remove;
    $ctrl.displayFields = ['name', 'project', 'buildStatus'];
    $ctrl.actionsData = { remove: remove };

    // --------------------


    var doNotShowEmptyState = false;

    // Make sure we don't show the empty state accidentally when the
    // Build Group is not empty. This can happen for example when
    // searching and no results are found.
    var unregister = $scope.$watch('$ctrl.page.data', function () {
      if ($ctrl.page.data && $ctrl.page.data.length > 0) {
        doNotShowEmptyState = true;
        $ctrl.showTable = true;
        unregister();
      }
    });

    function tableReload() {
      $ctrl.page.reload().then(function (result) {
        $ctrl.showTable = !!result.data.length;
      });
    }

    function showTable() {
      return doNotShowEmptyState || $ctrl.page && $ctrl.page.data && $ctrl.page.data.length > 0;
    }

    function edit() {
      var buildConfigs;

      if ($ctrl.page.getPageCount() === 1) {
        buildConfigs = $ctrl.page.data;
      } else {
        buildConfigs = BuildConfigurationSet.queryBuildConfigurations({
          id: $ctrl.buildGroup.id
        }, {
          pageSize: $ctrl.page.getPageCount() * $ctrl.page.getPageSize()
        }).$promise.then(function (response) {
          return response.data;
        });
      }

      $q.when(buildConfigs).then(function (buildConfigs) {
        modalEditService
          .editBuildGroupBuildConfigs($ctrl.buildGroup, buildConfigs)
          .then(function () {
            tableReload();
          });
      });
    }

    function remove(buildConfig) {
      BuildConfigurationSet.removeBuildConfiguration({ id: $ctrl.buildGroup.id, configId: buildConfig.id })
        .$promise
        .then(function () {
          tableReload();
        });
    }

    $ctrl.$onInit = function () {
      $ctrl.showTable = showTable();
    };
  }

})();
