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

  angular.module('pnc.product-versions').component('pncProductVersionLink', {
    bindings: {
      /**
       * The brief version of productVersion object from build config object
       */
      productVersion: '<'
    },
    templateUrl: 'product-versions/components/pnc-product-version-link/pnc-product-version-link.html',
    controller: ['$scope', 'ProductVersionResource', Controller]
  });

  function Controller($scope, ProductVersionResource) {
    var $ctrl = this;
    $ctrl.isProductVersionLoading = true;
    // -- Controller API --


    // --------------------
    $ctrl.$onInit = function () {
      $scope.$watch('$ctrl.productVersion', function () {
        if ($ctrl.productVersion) {
          ProductVersionResource.get({id: $ctrl.productVersion.id}).$promise.then(function (productVersionRes) {
            $ctrl.isProductVersionLoading = false;
            $ctrl.productVersionData = productVersionRes;
          });
        } else {
          $ctrl.isProductVersionLoading = false;
        }
      });
    };
  }

})();
