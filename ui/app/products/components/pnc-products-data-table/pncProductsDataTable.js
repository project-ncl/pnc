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

  angular.module('pnc.products').component('pncProductsDataTable', {
    bindings: {
      /**
       * A Page<ProductResource> object
       */
      products: '<'
    },
    templateUrl: 'products/components/pnc-products-data-table/pnc-products-data-table.html',
    controller: ['filteringPaginator', 'SortHelper', Controller]
  });

  function Controller(filteringPaginator, SortHelper) {
    const $ctrl = this;

    const PAGE_NAME = 'productsList';

    // -- Controller API --


    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.filterPage = filteringPaginator($ctrl.products);

      $ctrl.filterFields = [
        {
          id: 'name',
          title: 'Name',
          placeholder: 'Filter by Name',
          filterType: 'text'
        },
        {
          id: 'abbreviation',
          title: 'Abbreviation',
          placeholder: 'Filter by abbreviation',
          filterType: 'text'
        }
      ];

      $ctrl.productsSortingFields = [{
        id: 'name',
        title: 'Name'
      },
      {
        id: 'abbreviation',
        title: 'Abbreviation'
      }];

      $ctrl.productsSortingConfigs = SortHelper.getSortConfig(PAGE_NAME);
    };
  }

})();
