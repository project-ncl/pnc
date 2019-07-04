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

  angular.module('pnc.group-configs').component('pncGroupConfigDetailPage', {
    bindings: {
      originalGroupConfig: '<groupConfig',
      originalProductVersion: '<productVersion'
    },
    templateUrl: 'group-configs/detail/pnc-group-config-detail-page.html',
    controller: [Controller]
  });

  function Controller() {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.update = update;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.groupConfig = angular.copy($ctrl.originalGroupConfig);
      $ctrl.productVersion = angular.copy($ctrl.originalProductVersion);

      console.log('$ctrl.groupConfig == %O', $ctrl.groupConfig);
      console.log('$ctrl.productVersion == %O', $ctrl.productVersion);
    };


    function update() {
      $ctrl.groupConfig.$update()
          .catch(() => $ctrl.groupConfig = angular.copy($ctrl.originalGroupConfig));

      // update = function() {
      //   $log.debug('Updating BuildConfigurationSet: %s', JSON.stringify(self.set));
      //   self.set.$update(
      //   ).then(
      //     function() {
      //       $state.go('build-groups.detail.build-configs', {
      //         configurationSetId: self.set.id
      //       }, {
      //         reload: true
      //       });
      //     }
      //   )
    }

  }

})();
