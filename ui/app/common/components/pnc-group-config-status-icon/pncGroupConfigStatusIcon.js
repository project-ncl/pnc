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
   * A widget that shows a combined icon / text that indicates the status of the
   * latest Build of the provided Group Config. The component listens for websocket
   * notifations and updates the status display automatically if it has changed.
   */
  angular.module('pnc.common.components').component('pncGroupConfigStatusIcon', {
    bindings: {
      /**
       * Object: GroupConfig object to display the status of
       */
      groupConfig: '<'
    },
    templateUrl: 'common/components/pnc-group-config-status-icon/pnc-group-config-status-icon.html',
    controller: ['$scope', 'events', 'GroupConfigResource', Controller]
  });


  function Controller($scope, events, GroupConfigResource) {
    const $ctrl = this;

    // -- Controller API --

    // --------------------


    $ctrl.$onInit = () => {
      $ctrl.isLoaded = false;
      GroupConfigResource.getLatestBuild({ id: $ctrl.groupConfig.id }).$promise
          .then(groupBuild => $ctrl.groupBuild = groupBuild)
          .finally(() => $ctrl.isLoaded = true);

      $scope.$on(events.GROUP_BUILD_STATUS_CHANGED, (event, groupBuild) => {
        if ($ctrl.groupConfig.id === groupBuild.groupConfig.id) {
          $ctrl.groupBuild = groupBuild;
        }
      });
    };
  }
})();
