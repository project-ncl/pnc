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

  angular.module('pnc.build-configs').component('pncSelectBuildParameters', {
    require: {
      ngModel: '?ngModel'
    },
    bindings: {
      buildType: '<'
    },
    templateUrl: 'build-configs/directives/pnc-select-build-parameters/pnc-select-build-parameters.html',
    controller: ['$scope', 'utils', Controller]
  });

  function Controller($scope, utils) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.knownKeys = undefined;
    $ctrl.params = {};

    $ctrl.addParam = addParam;
    $ctrl.removeParam = removeParam;
    $ctrl.hasParams = hasParams;

    // --------------------


    $ctrl.$onInit = function () {

      $ctrl.knownKeys = BuildConfigResource.getSupportedGenericParameters().then(function (params) {
        return formatSupportedParams(params);
      });

      $scope.$watch(function () {
        return utils.concatStrings($ctrl.params);
      }, function () {
        $ctrl.ngModel.$setViewValue($ctrl.params);
      });

      $ctrl.ngModel.$render = function () {
        $ctrl.params = angular.isDefined($ctrl.ngModel.$viewValue) ? $ctrl.ngModel.$viewValue : {};
      };
    };

    function addParam(key, value) {
      $ctrl.params[key] = value;
    }

    function removeParam(key) {
      delete $ctrl.params[key];
    }

    function hasParams() {
      return Object.keys($ctrl.params).length > 0;
    }

    function formatSupportedParams(params) {
      var result = [];
      params.forEach(param => {
        result.push({
          name: param.name,
          description: param.description
        });
      });

      return result;
    }

  }
})();
