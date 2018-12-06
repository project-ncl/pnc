/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.product').component('pncProductVersionsDataTable', {
    bindings: {
      /**
       * page object: the page of ProductVersions to display in the table.
       */
      page: '<',
      /**
       * array of strings: Names of table columns to display
       */
      displayFields: '<',
      /**
       * 
       */
      onEdit: '&',
      /**
       * Callback function: called when the remove action button (shown on each table row) is clicked. 
       * The function will be passed the ProductVersion the action was called on. 
       * You will need to take some action upon the returned object to notify the backend to remove the object. 
       * The callback function should return a promise that is resolved when the remove operation is completed, the
       * page object will be automatically refreshed when this promise resolves.
       */
      onRemove: '&'
    },
    templateUrl: 'product/directives/pnc-product-versions-data-table/pnc-product-versions-data-table.html',
    controller: ['$log', '$q', 'modalSelectService', Controller]
  });


  function Controller($log, $q, modalSelectService) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.edit = edit;

    $ctrl.actions = {
      remove: remove
    };

    // --------------------


    $ctrl.$onInit = function () {
    };


    function edit() {
      modalSelectService.openForProductVersions({
        title: 'Insert / Remove Product Versions',
        productVersions: $ctrl.page.data
      });
      // $q.when()
      //   .then(function () {
      //     if ($ctrl.page.total === 1) {
      //       return $ctrl.page.data;
      //     } else {
      //       return $ctrl.page.getWithNewSize($ctrl.page.total * $ctrl.page.count).then(resp => resp.data);
      //     }
      //   })
      //   .then(function (productVersions) {
      //     return openForProductVersions.openForBuildConfigs({
      //       title: 'Insert / Remove Product Versions',
      //       productVersions: productVersions
      //     }).result;
      //   })
      //   .then(function (editedProductVersions) {
      //     $q.when($ctrl.onEdit()(editedProductVersions)).then(function () {
      //       $ctrl.page.refresh();
      //     });
      //   });
    }
    
    function remove(productVersion) {
      $log.debug('Table action: remove Product Version: %O', productVersion);
      $q.when($ctrl.onRemove()(productVersion)).then(function () {
        $ctrl.page.refresh();
      });
    }
  }

})();
