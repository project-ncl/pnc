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

  angular.module('pnc.product-versions').component('pncCreateProductVersionForm', {
    bindings: {
      product: '<'
    },
    templateUrl: 'product-versions/create/pnc-create-product-version-form.html',
    controller: ['$state', 'ProductVersionResource', Controller]
  });


  function Controller($state, ProductVersionResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.submit = submit;
    $ctrl.reset = reset;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.formData = {};
    };

    function submit() {
      const productVersion = new ProductVersionResource($ctrl.formData);
      productVersion.product = $ctrl.product;

      productVersion.$save().then(
        resp => $state.go('products.detail.product-versions.detail', {
            productId: resp.product.id,
            productVersionId: resp.id
        }));
    }

    function reset(form) {
      $ctrl.formData = {};
      form.$setPristine();
      form.$setUntouched();
    }
  }

})();
