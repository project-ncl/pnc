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

  angular.module('pnc.common.components').component('pncEnvironmentCombobox', {
    bindings: {
      /**
       * model-value {String} [Optional]: The property on the environment object
       * to use as the modelValue for ng-model. If not present the environment
       * object itself will be used. Example:
       * <pnc-environment-combobox ng-model="env" model-value="id"></pnc-environment-combobox>
       */
      modelProperty: '@modelValue'
    },
    require: {
      ngModel: '?ngModel'
    },
    templateUrl: 'common/components/pnc-environment-combobox/pnc-environment-combobox.html',
    controller: ['$log', '$scope', '$element', 'EnvironmentResource', 'utils', 'rsqlQuery', '$timeout', Controller]
  });

  function Controller($log, $scope, $element, EnvironmentResource, utils, rsqlQuery, $timeout) {
    var $ctrl = this,
        activeInitialValues,
        allInitialValues;

    // -- Controller API --

    $ctrl.search = search;
    $ctrl.showDeprecated = false;

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
          $log.warn('pnc-environment-combobox: programatic changing of the ng-model value is not fully supported when model-value parameter is used. The display value of the combobox will not be correctly mapped');
        }
        $ctrl.input = $ctrl.ngModel.$viewValue;
      };

      activeInitialValues = EnvironmentResource.query({
        pageSize: 200,
        q: rsqlQuery().where('hidden').eq(false).and().where('deprecated').eq(false).end(),
        sort: '=asc=description'
      }).$promise.then(function (page) {
        return page.data;
      });

      allInitialValues = EnvironmentResource.query({
        pageSize: 200,
        q: rsqlQuery().where('hidden').eq(false).end(),
        sort: '=asc=description'
      }).$promise.then(function (page) {
        return page.data;
      });
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

    function doSearch($viewValue) {
      let q;

      if ($ctrl.showDeprecated) {
        q = rsqlQuery().where('hidden').eq(false).and().where('name').like('*' + $viewValue + '*').end();
      } else {
        q = rsqlQuery().where('hidden').eq(false).and().where('deprecated').eq(false).and().where('name').like('*' + $viewValue + '*').end();
      }

      return EnvironmentResource.query({
        pageSize: 200,
        q: q,
        sort: '=asc=description'
      }).$promise.then(function (page) {
        return page.data;
      });
    }

    function search($viewValue) {
      if (utils.isEmpty($viewValue)) {

        // If the checkbox is selected
        if ($ctrl.showDeprecated) {
          return allInitialValues;
        } else {
          return activeInitialValues;
        }
      }

      return doSearch($viewValue);
    }
  }
})();
