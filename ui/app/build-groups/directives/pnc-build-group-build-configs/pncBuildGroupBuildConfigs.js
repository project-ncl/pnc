/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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
    controller: ['$scope', 'modalEditService', 'BuildConfigurationSet', Controller]
  });


  function Controller($scope, modalEditService, BuildConfigurationSet) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.showTable = showTable;
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
        unregister();
      }
    });

    function showTable() {
      return doNotShowEmptyState || $ctrl.page && $ctrl.page.data && $ctrl.page.data.length > 0;
    }

    function edit() {
      modalEditService
        .editBuildGroupBuildConfigs($ctrl.buildGroup, $ctrl.page.data)
        .then(function () {
          $ctrl.page.reload();
        });
    }

    function remove(buildConfig) {
      BuildConfigurationSet
          .removeBuildConfiguration({ id: $ctrl.buildGroup.id, configId: buildConfig.id })
          .$promise
          .then(function () {
            $ctrl.page.reload();
          });
    }
  }

})();
