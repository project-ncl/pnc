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

  angular.module('pnc.products').component('pncCreateProductForm', {
    templateUrl: 'products/create/pnc-create-product-form.html',
    controller: ['$state', 'ProductResource', Controller]
  });

  function Controller($state, ProductResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.create = create;
    $ctrl.reset = reset;

    // --------------------


    $ctrl.$onInit = () => {
    };

    function create(product) {
      ProductResource.save(product).$promise.then(
        response => $state.go('products.detail', { productId: response.id }),
        error => console.error('Error creating product: %O', error)
      );
    }

    function reset(form) {
      $ctrl.formData = undefined;
      form.$setPristine();
      form.$setUntouched();
    }

  }

})();
