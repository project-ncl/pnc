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
(function() {
  'use strict';

  angular.module('pnc.product-releases').component('pncProductReleaseCreateUpdatePage', {
    bindings: {
      product: '<',
      productRelease: '<',
      productVersion: '<',
    },
    templateUrl: 'product-releases/create-update/pnc-product-release-create-update-page.html',
    controller: ['ProductVersionResource', 'ProductReleaseResource', 'dateUtilConverter', '$state', Controller]
  });

  function Controller(ProductVersionResource, ProductReleaseResource, dateUtilConverter, $state) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.isUpdating = false;
    $ctrl.isLoaded = false;

    $ctrl.productMilestonesWithoutProductRelease = [];
    $ctrl.supportLevels = [];
    $ctrl.releaseDate = null;

    $ctrl.data = new ProductReleaseResource();



    // --------------------

    $ctrl.$onInit = () => {

      // updating existing Product Release
      if ($ctrl.productRelease !== null) {
        $ctrl.isUpdating = true;
        $ctrl.data = $ctrl.productRelease;
        $ctrl.productMilestoneId = $ctrl.productRelease.productMilestone.id;

        // Remove the prefix
        $ctrl.version = $ctrl.data.version.substring($ctrl.productVersion.version.length + 1);
        
        // date component <- timestamp
        $ctrl.releaseDate = new Date($ctrl.data.releaseDate);
      }


      ProductVersionResource.queryMilestones({
        id: $ctrl.productVersion.id,
        pageSize: 200
      }).$promise.then((productMilestonesResult => {
        // I need to gather the existing Releases, as Milestone can be associated with only one Release at the most
        $ctrl.productMilestonesWithoutProductRelease = productMilestonesResult.data.filter(productMilestone => !productMilestone.productRelease);
        $ctrl.isLoaded = true;
      }));

      ProductReleaseResource.querySupportLevels().$promise.then((supportLevelsResult) => {
        $ctrl.supportLevels = supportLevelsResult;
      });

    };


    $ctrl.submit = () => {

      // add the prefix
      $ctrl.data.version = $ctrl.productVersion.version + '.' + $ctrl.version;
      
      // timestamp <- date component
      $ctrl.data.releaseDate = dateUtilConverter.convertToUTCNoon($ctrl.releaseDate);
      
      $ctrl.data.productVersion = {
        id: $ctrl.productVersion.id
      };
      $ctrl.data.productMilestone = {
        id: parseInt($ctrl.productMilestoneId)
      };

      // updating existing Product Release
      if ($ctrl.isUpdating) {
        $ctrl.data.$update().then(() => {
          reloadPage($ctrl.product.id, $ctrl.productVersion.id);
        });

      // creating new Product Release
      } else {
        $ctrl.data.$save().then(() => {
          reloadPage($ctrl.product.id, $ctrl.productVersion.id);
        });
      }
    };

    function reloadPage(productId, productVersionId) {
      $state.go('products.detail.product-versions.detail', {
        productId: productId,
        productVersionId: productVersionId
      }, {
        reload: true
      });
    }
  }

})();
