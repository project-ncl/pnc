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

  angular.module('pnc.common.components').component('pncBuildTypeCombobox', {
    bindings: {
      /**
       * model-property {String} [Optional]: The property on the buildType object
       * to use as the modelValue for ng-model. If not present the buildType
       * object itself will be used. Example:
       * <pnc-build-type-combobox ng-model="$ctrl.buildType" model-value="id"></pnc-build-type-combobox>
       */
      modelProperty: '@'
    },
    require: {
      ngModel: '?ngModel'
    },
    templateUrl: 'common/components/pnc-build-type-combobox/pnc-build-type-combobox.html',
    controller: ['buildTypes', '$scope', '$element', '$timeout', Controller]
  });

  function Controller(buildTypes, $scope, $element, $timeout) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.buildTypes = buildTypes;

    // --------------------


    $ctrl.$onInit = function () {

      // Synchronise value from combobox with this component's ng-model
      $scope.$watch(function () {
        return $ctrl.input;
      }, function () {
        $ctrl.ngModel.$setViewValue($ctrl.input);
      });

      // Transform the combobox's value to the correct ng-model value.
      $ctrl.ngModel.$parsers.push(function (viewValue) {
        if (angular.isObject(viewValue) && angular.isDefined($ctrl.modelProperty)) {
          return viewValue[$ctrl.modelProperty];
        }

        return viewValue;
      });

      // Respond to programmatic changes to the ng-model value, to propagate
      // changes back to the combobox's displayed value.
      $ctrl.ngModel.$render = function () {
        if (!$ctrl.ngModel.$isEmpty($ctrl.ngModel.$viewValue) && angular.isDefined($ctrl.modelProperty)) {
          $ctrl.input = buildTypes.find(function (buildType) {
            return buildType[$ctrl.modelProperty] ===  $ctrl.ngModel.$viewValue;
          });
        } else {
          $ctrl.input = $ctrl.ngModel.$viewValue;
        }
      };

    };

    $ctrl.$postLink = function () {

      // $timeout used without an interval value to ensure the DOM element has
      // been rendered when we try to to find it.
      $timeout(function () {
        $element.find('input').on('blur', function () {
          $ctrl.ngModel.$setTouched();
        });
      });

    };

    $ctrl.$onDestroy = function () {
      $element.find('input').off('blur');
    };
  }

})();
