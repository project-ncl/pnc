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
    $ctrl.announcement = null;

    // -- Controller API --

    // --------------------


    $ctrl.$onInit = function () {

      var announcementPromise = GenericSetting.getAnnouncementBanner().then(res => {
        return res.data;
      });

      var maintenanceStatusPromise = GenericSetting.inMaintenanceMode().then(res =>  {
        return res.data;
      });

      $q.all([maintenanceStatusPromise, announcementPromise]).then(result => {
        let inMaintenanceMode = result[0];
        let bannerMessage = result[1].banner;

        if (inMaintenanceMode) {
          $ctrl.isInMaintenanceMode = true;
          $ctrl.message = bannerMessage !== '' ? bannerMessage : null;
        } else if(bannerMessage !== ''){
          $ctrl.announcement = bannerMessage;
        } else {
          $ctrl.isInMaintenanceMode = false;
          $ctrl.message = null;
          $ctrl.announcement = null;
        }
      });

      $scope.$on(events.MAINTENANCE_MODE_ON, () => {
        $ctrl.isInMaintenanceMode = true;
        GenericSetting.getAnnouncementBanner().then(bannerMessage => {
          $ctrl.message = bannerMessage && bannerMessage.data.banner && bannerMessage.data.banner !== '' ? bannerMessage.data.banner : null;
          $ctrl.announcement = null;
        });
      });

      $scope.$on(events.MAINTENANCE_MODE_OFF, () => {
        $ctrl.isInMaintenanceMode = false;
        $ctrl.message = null;
        $ctrl.announcement = null;
        $scope.$apply();
      });


      $scope.$on(events.NEW_ANNOUNCEMENT, (event, msg) => {
        if(!$ctrl.isInMaintenanceMode){
          $ctrl.announcement = msg.banner === '' ? null : msg.banner;
        }else{
          $ctrl.message = msg.banner === '' ? null : msg.banner;
        }
      });

    };
  }
})();
