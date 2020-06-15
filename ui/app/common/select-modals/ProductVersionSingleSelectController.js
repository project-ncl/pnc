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

  angular.module('pnc.common.select-modals').controller('ProductVersionSingleSelectController', [
    '$log',
    'modalConfig',
    'ProductResource',
    'rsqlQuery',
    function ($log, modalConfig, ProductResource, rsqlQuery) {
      var ctrl = this;

      ctrl.title = modalConfig.title;
      ctrl.selectedProduct = angular.copy(modalConfig.selected);

      ctrl.save = function () {
        ctrl.$close(ctrl.selectedVersion);
      };

      ctrl.close = function () {
        ctrl.$dismiss();
      };

      ctrl.onSelectProduct = function ($item) {
        ctrl.selectedProduct = $item;

        // convert to plain array and add product name property
        ctrl.selectedProduct._productVersions = Object.values(ctrl.selectedProduct.productVersions);
        ctrl.selectedProduct._productVersions.forEach(item => { item._productName = ctrl.selectedProduct.name; });
        
        ctrl.input = undefined;
        console.log('selected product: %O', $item);
      };

      ctrl.onSelectVersion = function ($item) {
        ctrl.selectedVersion = $item;
        console.log('selected version: %O', $item);
      };

      ctrl.fetchProducts = function ($viewValue) {
        return ProductResource.query({
          q: rsqlQuery().where('name').like($viewValue + '%').end()
        }).$promise.then(page => page.data);
      };


      ctrl.config = {
       selectItems: true,
       multiSelect: false,
       dblClick: false,
       selectionMatchProp: 'id',
       selectedItems: [],
      //  checkDisabled: checkDisabledItem,
       showSelectBox: false,
       onSelect: ctrl.onSelectVersion,
      //  onSelectionChange: handleSelectionChange,
      //  onCheckBoxChange: handleCheckBoxChange,
      //  onClick: handleClick,
      //  onDblClick: handleDblClick
      };
    }
  ]);

})();
