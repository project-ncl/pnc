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

    angular.module('pnc.common.components').component('pncProductCombobox', {
      bindings: {
        modelProperty: '@modelValue'
      },
      require: {
        ngModel: '?ngModel'
      },
      templateUrl: 'common/components/pnc-product-combobox/pnc-product-combobox.html',
      controller: ['$log', '$scope', '$element', 'ProductResource', 'utils', 'rsqlQuery', '$timeout', Controller]
    });

    function Controller($log, $scope, $element, ProductResource, utils, rsqlQuery, $timeout) {
      var $ctrl = this,
          initialValues;

      // -- Controller API --

      $ctrl.search = search;

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
            $log.warn('pnc-productg-combobox: programatic changing of the ng-model value is not fully supported when model-value parameter is used. The display value of the combobox will not be correctly mapped');
          }
          $ctrl.input = $ctrl.ngModel.$viewValue;
        };

        initialValues = ProductResource.query({ pageSize: 20 }).$promise.then(function (page) {
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
        var q = rsqlQuery().where('name').like('*' + $viewValue + '*').end();

        return ProductResource.query({ q: q }).$promise.then(function (page) {
          return page.data;
        });
      }

      function search($viewValue) {
        if (utils.isEmpty($viewValue)) {
          return initialValues;
        }

        return doSearch($viewValue);
      }
    }

  })();
