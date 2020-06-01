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

  angular.module('pnc.build-configs').component('pncCreateBuildConfigProductVersionForm', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-product-version-form/pnc-create-build-config-product-version-form.html',
    require: {
      ngModel: 'ngModel'
    },
    controller: [Controller]
  });

  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.ngModel.$render = function () {
        $ctrl.data = $ctrl.ngModel.$modelValue;
      };
    };

    $ctrl.$doCheck = function () {
      if ($ctrl.ngModel.$viewValue !== $ctrl.data) {
        $ctrl.ngModel.$setViewValue($ctrl.data);
      }
    };
    /*Check version data and remove it if product is changed or not selected*/
    $ctrl.checkVersionData = function () {
      if (!$ctrl.data.product || ($ctrl.data.version && $ctrl.data.product.id !== $ctrl.data.version.product.id)) {
        $ctrl.data.version = null;
      }
    };
  }
})();
