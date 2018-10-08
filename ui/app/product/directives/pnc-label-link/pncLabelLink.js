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

  /**
   * Component representing label box suitable for 
   * displaying items like Milestone or Release.
   * 
   * Example Milestone:
   * <pnc-label-link item="milestone" product-id="productId" current-id="currentId"></pnc-label-link>
   * 
   * Example Release:
   * <pnc-label-link item="release"></pnc-label-link>
   */
  angular.module('pnc.product').component('pncLabelLink', {
    bindings: {
      /**
       * Object: The item (like Milestone or Release) to be displayed
       */
      item: '<',
      /**
       * Number: Optional id representing current product
       */
      productId: '<?',
      /**
       * Number: Optional id representing current item (like current Milestone).
       */
      currentId: '<?'
    },
    templateUrl: 'product/directives/pnc-label-link/pnc-label-link.html',
    controller: ['ProductMilestoneDAO', Controller]
  });

  function Controller(ProductMilestoneDAO) {
    var $ctrl = this;

    $ctrl.$onInit = function() {
      $ctrl.isMilestone = _.has($ctrl.item, 'productReleaseId');
      $ctrl.isRelease   = _.has($ctrl.item, 'productMilestoneId');
      $ctrl.isPrimary   = $ctrl.item.id === $ctrl.currentId;

      if ($ctrl.isRelease) {
        ProductMilestoneDAO.get({milestoneId: $ctrl.item.productMilestoneId}).$promise.then(function(data) {
          $ctrl.milestone = data;
        });
      }
    };

  }

})();
