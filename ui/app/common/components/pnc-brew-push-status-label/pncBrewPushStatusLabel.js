-/*
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

  angular.module('pnc.common.components').component('pncBrewPushStatusLabel', {
    bindings: {
      /**
       * Object: The Build to show the brew push status of
       */
      build: '<?'
    },
    templateUrl: 'common/components/pnc-brew-push-status-label/pnc-brew-push-status-label.html',
    controller: ['BuildResource', Controller]
  });

  function Controller(BuildResource) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.pushStatus = {};
    $ctrl.loading = true;

    // --------------------


    $ctrl.$onInit = function () {
      if ($ctrl.build.$isSuccess()) {
        BuildResource.getBrewPushResult({ id: $ctrl.build.id }).$promise
          .then(function (pushStatus) {
            $ctrl.pushStatus = pushStatus;
          })
          .catch(function(error) {
            // Response Code 404 is valid when there is no result available, see NCL-5336
            if (error.status === 404) {
              console.log('No Brew Push Result is available for Build#' + $ctrl.build.id);
            } else {
              throw error;
            }
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
