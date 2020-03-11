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

  /**
   * The component for global-screen alert message
   */
  angular.module('pnc.common.components').component('pncGlobalAlert', {
    bindings: {
    },
    templateUrl: 'common/components/pnc-global-alert/pnc-global-alert.html',
    controller: ['$q', '$scope', 'events', 'GenericSetting', Controller]
  });

  function Controller($q, $scope, events, GenericSetting) {
    var $ctrl = this;

    $ctrl.isInMaintenanceMode = false;
    $ctrl.message = null;

    // -- Controller API --

    // --------------------


    $ctrl.$onInit = function () {

      var announcementPromise = GenericSetting.getAnnouncementBanner().then(function (res) {
        return res.data;
      });

      var maintenanceStatusPromise = GenericSetting.inMaintenanceMode().then(function (res) {
        return res.data;
      });

      $q.all([maintenanceStatusPromise, announcementPromise]).then(function (result) {
        let inMaintenanceMode = result[0];
        let bannerMessage = result[1];

        if (inMaintenanceMode) {
          $ctrl.isInMaintenanceMode = true;
          $ctrl.message = bannerMessage && bannerMessage.banner && bannerMessage.banner !== '' ? ' Reason: ' + result[1].banner : null;
        } else {
          $ctrl.isInMaintenanceMode = false;
          $ctrl.message = null;
        }
      });

      $scope.$on(events.MAINTENANCE_MODE_ON, () => {
        $ctrl.isInMaintenanceMode = true;
        GenericSetting.getAnnouncementBanner().then(function (bannerMessage) {
          $ctrl.message = bannerMessage && bannerMessage.data.banner && bannerMessage.data.banner !== '' ? ' Reason: ' + bannerMessage.data.banner : null;
        });
      });

      $scope.$on(events.MAINTENANCE_MODE_OFF, () => {
        $ctrl.isInMaintenanceMode = false;
        $ctrl.message = null;
      });

    };
  }
})();
