/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2020 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.build-configs').component('pncBuildConfigDetailMain', {
    bindings: {
      buildConfig: '<'
    },
    templateUrl: 'build-configs/detail/pnc-build-config-detail-main.html',
    controller: ['$scope', '$state', Controller]
  });


  function Controller($scope, $state) {
    var $ctrl = this,
        onEditFn;

    // -- Controller API --

    $ctrl.clone = clone;
    $ctrl.edit = edit;
    $ctrl.delete = deleteBc;
    $ctrl.registerOnEdit = registerOnEdit;
    $ctrl.updateBuildConfig = updateBuildConfig;

    // --------------------


    $ctrl.$onInit = function () {
    };

    function clone() {
      $ctrl.buildConfig.$clone().then(function (resp) {
        $state.go('projects.detail.build-configs.detail', {
          configurationId: resp.id,
          projectId: resp.project.id
        }, {
          reload: true
        });
      });
    }

    function edit() {
      onEditFn();
    }

    function deleteBc() {
      $ctrl.buildConfig.$delete().then(function (resp) {
        $state.go('projects.detail', {
          configurationId: resp.id        
        }, {
          reload: true
        });
      });
    }

    function registerOnEdit(func) {
      onEditFn = func;
    }

    function updateBuildConfig(buildConfig) {
      $scope.$applyAsync(function () { $ctrl.buildConfig = buildConfig; });
    }
  }

})();
