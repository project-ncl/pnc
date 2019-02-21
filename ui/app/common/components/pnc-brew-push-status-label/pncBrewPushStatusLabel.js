-/*
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

  angular.module('pnc.common.components').component('pncBrewPushStatusLabel', {
    bindings: {
      /**
       * Object: The BuildRecord to show the brew push status of
       */
      buildRecord: '<?'
    },
    templateUrl: 'common/components/pnc-brew-push-status-label/pnc-brew-push-status-label.html',
    controller: ['BuildRecord', Controller]
  });

  function Controller(BuildRecord) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.pushStatus = {};
    $ctrl.loading = true;

    // --------------------


    $ctrl.$onInit = function () {
      if ($ctrl.buildRecord.$isSuccess()) {
        BuildRecord.getLatestPushStatus($ctrl.buildRecord.id)
          .then(function (pushStatus) {
            $ctrl.pushStatus = pushStatus;
          })
          .finally(function () {
            $ctrl.loading = false;
          });
      } else {
        $ctrl.loading = false;
      }
    };

  }

})(); // jshint ignore:line
