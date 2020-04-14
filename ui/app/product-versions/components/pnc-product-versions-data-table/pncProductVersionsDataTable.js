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

  angular.module('pnc.product-versions').component('pncProductVersionsDataTable', {
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
    templateUrl: 'product-versions/components/pnc-product-versions-data-table/pnc-product-versions-data-table.html',
    controller: ['paginator', Controller]
  });


  function Controller(paginator) {
    const $ctrl = this;

    // -- Controller API --

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.paginator = paginator($ctrl.page);
    };

  }

})();
