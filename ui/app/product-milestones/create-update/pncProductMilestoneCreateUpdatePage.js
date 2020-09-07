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

  angular.module('pnc.product-milestones').component('pncProductMilestoneCreateUpdatePage', {
    bindings: {
      product: '<',
      productVersion: '<',
      productMilestone: '<'
    },
    templateUrl: 'product-milestones/create-update/pnc-product-milestone-create-update-page.html',
    controller: ['$state', 'ProductMilestoneResource', 'dateUtilConverter', Controller]
  });

  function Controller($state, ProductMilestoneResource, dateUtilConverter) {
    const $ctrl = this;

    // -- Controller API --
    $ctrl.isUpdating = false;
    $ctrl.data = new ProductMilestoneResource();

    $ctrl.startingDate = null;
    $ctrl.plannedEndDate = null;

    $ctrl.invalidStartingPlannedEndDates = invalidStartingPlannedEndDates;
    $ctrl.submit = submit;

    $ctrl.productMilestoneVersionErrorMessages = [];


    // --------------------

    $ctrl.$onInit = () => {
      if ($ctrl.productMilestone !== null) {
        $ctrl.isUpdating = true;
        $ctrl.data = $ctrl.productMilestone;
  
        // Remove the prefix
        $ctrl.version = $ctrl.data.version.substring($ctrl.productVersion.version.length + 1);
  
        // date component <- timestamp
        $ctrl.startingDate = new Date($ctrl.data.startingDate);
        $ctrl.plannedEndDate = new Date($ctrl.data.plannedEndDate);
      }
  
      $ctrl.setCurrentMilestone = $ctrl.productVersion.currentProductMilestone.id === $ctrl.data.id;
  
      // milestone can be only marked as current, not unmarked
      $ctrl.setCurrentMilestoneDisabled = $ctrl.setCurrentMilestone;
    };

    function invalidStartingPlannedEndDates(sDate, prDate) {
      if (sDate === undefined || prDate === undefined) {
        return false;
      }
      return sDate >= prDate;
    }

    function submit() {
      $ctrl.data.version = $ctrl.productVersion.version + '.' + $ctrl.version; // add the prefix

      // timestamp <- date component
      $ctrl.data.startingDate = dateUtilConverter.convertToUTCNoon($ctrl.startingDate);
      $ctrl.data.plannedEndDate = dateUtilConverter.convertToUTCNoon($ctrl.plannedEndDate);

      $ctrl.data.productVersion = {
        id: $ctrl.productVersion.id
      };

      // updating existing Product Milestone
      if ($ctrl.isUpdating) {
        if ($ctrl.setCurrentMilestone) {
          $ctrl.productVersion.currentProductMilestone = {
            id: $ctrl.data.id
          };
        }

        $ctrl.productVersion.$update().then(() => {
          $ctrl.data.$update().then(() => {
            reloadPage($ctrl.product.id, $ctrl.productVersion.id);
          });
        });

      // creating new Product Milestone
      } else {
        $ctrl.data.$save().then(() => {
          if ($ctrl.setCurrentMilestone) {
            $ctrl.productVersion.currentProductMilestone = {
              id: $ctrl.data.id
            };

            $ctrl.productVersion.$update().finally(() => {
              displayProductMilestoneDetail($ctrl.productVersion.id, $ctrl.data.id);
            });
          } else {
            displayProductMilestoneDetail($ctrl.productVersion.id, $ctrl.data.id);
          }
        });
      }
    }

    function reloadPage(productId, productVersionId) {
      $state.go('products.detail.product-versions.detail', {
        productId: productId,
        productVersionId: productVersionId
      }, {
        reload: true
      });
    }

    function displayProductMilestoneDetail(productVersionId, productMilestoneId) {
      $state.go('products.detail.product-versions.detail.milestone.detail', {
        productVersionId: productVersionId,
        productMilestoneId: productMilestoneId
      }, {
        reload: true
      });
    }

  }

})();
