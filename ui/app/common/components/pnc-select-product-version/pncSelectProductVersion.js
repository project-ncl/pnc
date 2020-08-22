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

  /**
   * The component for selecting Products and Product Versions
   */
  angular.module('pnc.common.components').component('pncSelectProductVersion', {
    templateUrl: 'common/components/pnc-select-product-version/pnc-select-product-version.html',
    require: {
      ngModel: 'ngModel'
    },
    bindings: {
      /**
       * Object: The boolean value of whether it is for create Group Config page
       */
      createGroupConfigLayout: '<',
    },
    controller: ['$scope', 'ProductVersionResource', Controller]
  });

  function Controller($scope, ProductVersionResource) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.formData = {};

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.ngModel.$render = function () {
        $ctrl.formData.productVersion = $ctrl.ngModel.$modelValue;
        //Fetch full product version data from endpoint to get the product information by productVersion.id
        if($ctrl.formData.productVersion){
          $ctrl.isProductVersionLoading = true;
          ProductVersionResource.get({ id: $ctrl.formData.productVersion.id }).$promise.then(function (productVersionRes){
            $ctrl.formData.productVersion = productVersionRes;
            $ctrl.formData.product = productVersionRes ? productVersionRes.product : null;
            $ctrl.isProductVersionLoading = false;
          });
        }
      };
    };

    $ctrl.$doCheck = function () {
      if ($ctrl.ngModel.$viewValue !== $ctrl.formData.productVersion) {
        $ctrl.ngModel.$setViewValue($ctrl.formData.productVersion);
      }
    };
    /*Check version data and remove it if product is changed or not selected*/
    $ctrl.checkVersionData = function () {
      if (!$ctrl.formData.product || ($ctrl.formData.productVersion && $ctrl.formData.product.id !== $ctrl.formData.productVersion.product.id)) {
        $ctrl.formData.productVersion = null;
      }
    };
  }
})();
