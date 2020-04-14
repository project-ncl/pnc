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

  angular.module('pnc.products').component('pncProductDetailPage', {
    bindings: {
      product: '<',
      productVersions: '<'
    },
    templateUrl: 'products/detail/pnc-product-detail-page.html',
    controller: ['ProductResource', Controller]
  });

  function Controller(ProductResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.update = update;

    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.displayFields = ['version', 'milestones', 'releases'];
    };

    function update($data) {
      return ProductResource.safePatch($ctrl.product, $data).$promise.then(
          resp => console.log(resp),
          err => err.data.errorMessage || 'Unrecognised error from PNC REST API');
    }
  }

})();
