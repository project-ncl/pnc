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

  angular.module('pnc.product-milestones').component('pncMilestoneCloseStatusLabel', {
    bindings: {
      closeResult: '<'
    },
    templateUrl: 'product-milestones/components/pnc-milestone-close-status-label/pnc-milestone-close-status-label.html',
    controller: [Controller]
  });

  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.getStatus = getStatus;

    // --------------------

    const CLOSE_STATUSES = {
      IN_PROGRESS: {
        text: 'IN PROGRESS',
        class: 'label label-primary'
      },
      FAILED: {
        text: 'FAILED',
        class: 'label label-danger'
      },
      SUCCEEDED: {
        text: 'SUCCEEDED',
        class: 'label label-success'
      },
      CANCELED: {
        text: 'CANCELLED',
        class: 'label label-default'
      },
      SYSTEM_ERROR: {
        text: 'SYSTEM ERROR',
        class: 'label label-danger'
      }
    };

    $ctrl.$onInit = () => {
      $ctrl.closeStatus = getStatus();
    };

    function getStatus() {
      if (angular.isUndefined($ctrl.closeResult)) {
        return null;
      }

      return CLOSE_STATUSES[$ctrl.closeResult.status];
    }

  }

})();
