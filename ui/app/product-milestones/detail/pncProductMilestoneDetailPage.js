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

  angular.module('pnc.product-milestones').component('pncProductMilestoneDetailPage', {
    bindings: {
      productMilestone: '<',
      performedBuilds: '<',
      closeResults: '<',
      product: '<',
      productVersion: '<'
    },
    templateUrl: 'product-milestones/detail/pnc-product-milestone-detail-page.html',
    controller: ['ProductMilestoneHelper', Controller]
  });

  function Controller(ProductMilestoneHelper) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.isCurrent = isCurrent;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.closeStatus = $ctrl.productMilestone.endDate ? 'CLOSED' : 'OPEN';
      $ctrl.latestCloseResult = $ctrl.closeResults.data[0];
    };

    function isCurrent() {
      return ProductMilestoneHelper.isCurrentProductMilestone($ctrl.productVersion, $ctrl.productMilestone.id);
    }

  }

})();
