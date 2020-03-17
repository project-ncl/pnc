/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.product-versions').component('pncProductVersionDetailPage', {
    bindings: {
      productVersion: '<'
    },
    templateUrl: 'product-versions/detail/pnc-product-version-detail-page.html',
    controller: ['ProductVersionResource', Controller]
  });


  function Controller(ProductVersionResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.getFullName = getFullName;
    $ctrl.save = save;

    // --------------------

    $ctrl.$onInit = () => {
    };

    function getFullName() {
      return `${$ctrl.productVersion.product.name} ${$ctrl.productVersion.version}`;
    }

    function save($data) {
      $data.attributes = {};
      $data.attributes.BREW_TAG_PREFIX = $data.brewTagPrefix;
      delete $data.brewTagPrefix;

      return ProductVersionResource.safePatch($ctrl.productVersion, $data).$promise
          .catch(err => err.data.errorMessage || 'Unrecognised error from PNC REST API');
    }
  }

})();
