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

  angular.module('pnc.build-configs').component('pncDisplayBuildParameters', {
    bindings: {
      params: '<',
      buildType: '<',
      originalBuildType: '=',
      isEditForm: '<',
      onEdit: '&',
      onRemove: '&'
    },
    templateUrl: 'build-configs/directives/pnc-select-build-parameters/pnc-display-build-parameters.html',
    controller: ['$scope', 'BuildConfigResource', Controller]
  });

  function Controller($scope, BuildConfigResource) {
    var $ctrl = this,
        editMap = {};

    let paramsLoadingFailed = false;
    let alignmentParametersKey = 'ALIGNMENT_PARAMETERS';

    // -- Controller API --

    $ctrl.newAlignmentParameters = undefined;

    // --------------------

    function copyParam(key) {
      $ctrl.currentParams[key] = angular.copy($ctrl.params[key]);
    }

    function copyParams() {
      $ctrl.currentParams = angular.copy($ctrl.params);
    }

    function isBuildTypeChanged() {
      return $ctrl.buildType !== $ctrl.originalBuildType;
    }

    function addAlignmentIfNotPresent(){
      if (isBuildTypeChanged() && !(alignmentParametersKey in $ctrl.currentParams)){
        // if no ALIGNMENT_PARAMETERS present add empty ones so the proposal of new ones works
        $ctrl.currentParams[alignmentParametersKey] = '';
      }
    }

    $ctrl.$onInit = function(){
      $scope.$watchCollection('$ctrl.params', function () {
        copyParams();
      });
    };

    $ctrl.$onChanges = function (changesObj) {
      let buildTypeObj = changesObj.buildType;

      if (buildTypeObj){
        $ctrl.updateAlignmentParams(buildTypeObj.currentValue);
        addAlignmentIfNotPresent();
      }
    };

    $ctrl.$doCheck = function () {
      if (angular.isUndefined($ctrl.currentParams)) {
        return;
      }

      Object.keys($ctrl.params).forEach(function (key) {
        if (!$ctrl.currentParams.hasOwnProperty(key)) {
          copyParam(key);
        }
      });
    };

    $ctrl.updateAlignmentParams = function(newBuildType){
      BuildConfigResource.getAlignmentParameters(newBuildType).then(function (params){
        $ctrl.newAlignmentParameters = params;
        paramsLoadingFailed = false;

        if (!$ctrl.isEditForm){
          // Build configuration is being created so apply new parameters immediately
          $ctrl.applyNewParameters();
        }

      }).catch(function () {
        paramsLoadingFailed = true;
      });
    };

    $ctrl.cancel = function (key) {
      copyParam(key);
      $ctrl.setEditOff(key);
    };

    $ctrl.update = function (key, value) {
      $ctrl.onEdit({ key: key, value: value });
      $ctrl.setEditOff(key);
    };

    $ctrl.remove = function (key) {
      $ctrl.onRemove({ key: key });
      delete $ctrl.currentParams[key];
      delete editMap[key];
    };

    $ctrl.setEditOn = function (key) {
      editMap[key] = true;
    };

    $ctrl.setEditOff = function (key) {
      editMap[key] = false;
    };

    $ctrl.toggleEdit = function (key) {
      editMap[key] = !!!editMap[key];
    };

    $ctrl.isEditOn = function (key) {
      return !!editMap[key];
    };

    $ctrl.isParamsRequestFailed = function (key) {
      return key === alignmentParametersKey && paramsLoadingFailed && isBuildTypeChanged();
    };

    $ctrl.isProposedAlignmentParams = function (key) {
      return key === alignmentParametersKey && isBuildTypeChanged() && $ctrl.isEditForm && !paramsLoadingFailed;
    };

    $ctrl.applyNewParameters = function () {
      $ctrl.onEdit({ key: alignmentParametersKey, value: $ctrl.newAlignmentParameters});
      $ctrl.originalBuildType = $ctrl.buildType;
    };

  }
})();
