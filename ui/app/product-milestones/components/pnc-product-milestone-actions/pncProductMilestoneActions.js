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

  /**
   * The component representing milestone actions for given Milestone
   */
  angular.module('pnc.product-milestones').component('pncProductMilestoneActions', {
    bindings: {
      /**
       * Object: The object representing Product Milestone
       */
      productMilestone: '<',
      /**
       * Object: The object representing Product
       */
      product: '<',
      /**
       * Object: The object representing Product Version
       */
      productVersion: '<',
      /**
       * Boolean: Value telling if this milestone is the current for the product version
       */
      isCurrent: '<',
      /**
       * String: Value representing bootstrap button size: lg (default if empty), md, sm, xs
       */
      size: '@?'
    },
    templateUrl: 'product-milestones/components/pnc-product-milestone-actions/pnc-product-milestone-actions.html',
    controller: ['$state', Controller]
  });

  function Controller($state) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.markMilestoneAsCurrent = markMilestoneAsCurrent;

    // --------------------

    function markMilestoneAsCurrent (productMilestone) {
      const newProductVersion = angular.copy($ctrl.productVersion);

      newProductVersion.currentProductMilestone = {
        id: productMilestone.id
      };

      // Mark Milestone as current in Product Version
      newProductVersion.$update({
        productId: $ctrl.product.id,
        versionId: $ctrl.productVersion.id
      }).then(function () {
        $state.go('products.detail.product-versions.detail', {
          productId: $ctrl.product.id,
          productVersionId: $ctrl.productVersion.id
        }, {
          reload: true
        });
      });
    }

  }
})();
