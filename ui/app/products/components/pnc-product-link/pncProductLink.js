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

  angular.module('pnc.products').component('pncProductLink', {
    bindings: {
      /**
       * ProductResource resource object to link to, or alternatively pass an object literal
       * with the id of the Product to link to... e.g. { id: 5 }
       */
      product: '<'
    },
    transclude: true,
    templateUrl: 'products/components/pnc-product-link/pnc-product-link.html',
    controller: [Controller]
  });

  function Controller() {
    const $ctrl = this;

    // -- Controller API --


    // --------------------


    $ctrl.$onInit = () => {
    };
  }

})();
