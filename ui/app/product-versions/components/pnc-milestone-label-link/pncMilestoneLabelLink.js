/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.product-versions').component('pncMilestoneLabelLink', {
    bindings: {
      productVersion: '<',
      milestone: '<'
    },
    templateUrl: 'product-versions/components/pnc-milestone-label-link/pnc-milestone-label-link.html',
    controller: [Controller]
  });


  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.isCurrentMilestone = isCurrentMilestone;

    // --------------------

    $ctrl.$onInit = () => {
    };

    function isCurrentMilestone() {
      return $ctrl.milestone.id === $ctrl.productVersion.currentProductMilestone.id;
    }
  }

})();
