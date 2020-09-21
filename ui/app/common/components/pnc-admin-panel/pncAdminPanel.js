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
   * The component for Admin panel for generic settings
   */
  angular.module('pnc.common.components').component('pncAdminPanel', {
    bindings: {
    },
    templateUrl: 'common/components/pnc-admin-panel/pnc-admin-panel.html',
    controller: ['GenericSetting', '$scope', 'events', Controller]
  });

  function Controller(GenericSetting, $scope, events) {
    var $ctrl = this;
    $ctrl.isInMaintenanceMode = false;
    $ctrl.message = null;

    $ctrl.data = {
      /* Reason for activating Maintenance Mode */
      reason: null
    };


    /* Functions for field validation */
    var setError = function (targetId) {
      $('#' + targetId).addClass('has-error');
      $('#' + targetId).find('.help-block').removeClass('ng-hide');
    };

    var removeError = function (targetId) {
      $('#' + targetId).removeClass('has-error');
      $('#' + targetId).find('.help-block').addClass('ng-hide');
    };

    $ctrl.validateActivateFormGroup = function () {
      if (!$ctrl.data.reason || $ctrl.data.reason === '') {
        setError('confirmActivateFormGroup');
        return false;
      } else {
        removeError('confirmActivateFormGroup');
        return true;
      }
    };

    $ctrl.clearMaintenanceValidation = function () {
      removeError('confirmActivateFormGroup');
    };

    /**
     * Validate and activate maintenance mode, if returns 200 set the switch to on
     */
    $ctrl.activateMaintenanceMode = function () {
      if ($ctrl.validateActivateFormGroup()) {
        GenericSetting.activateMaintenanceMode($ctrl.data.reason).then(function (res) {
          if (res.status === 204) {
            changeMaintenanceSwitch(true);
            $ctrl.data.reason = null;
            $('#activateMaintenance').modal('hide');
          }
        });
      }
    };

    /**
     * Update the Announcement banner
     */
    $ctrl.updateAnnouncement = function () {
        GenericSetting.setAnnouncementBanner($ctrl.message);
    };

    /**
     * Deactivate maintenance mode, if returns 200 set the switch to off
     */
    var deactivateMaintenanceMode = function () {
      GenericSetting.deactivateMaintenanceMode().then(function (res) {
        if (res.status === 204) {
          changeMaintenanceSwitch(false);
        }
      });
    };

    /**
     * Change the status of the maintenance switch button according to the state passed in
     */
    var changeMaintenanceSwitch = function (state) {
      $ctrl.isInMaintenanceMode = state;
      $('#maintenance-switch').bootstrapSwitch('state', state, true);
    };

    /**
     * JQuery listener to be triggered when maintenance switch is toggled.
     * - If attempted to switch on, show confirmation modal.
     * - If attempted to switch off, run deactivateMaintenanceMode().
     */
    $('#maintenance-switch').on('switchChange.bootstrapSwitch', function (event, state) {
      changeMaintenanceSwitch(!state);
      if (state) {
        $('#activateMaintenance').modal();
      } else {
        deactivateMaintenanceMode();
      }
    });

    /**
     * JQuery listener for users to submit for by press enter button
     */
    document.getElementById('activateReason').addEventListener('keyup', function (event) {
      if (event.keyCode === 13) {
        event.preventDefault();
        $ctrl.clearMaintenanceValidation();
        document.getElementById('confirm-maintenance-mode').click();
      }
    });


    // -- Controller API --

    // --------------------


    $ctrl.$onInit = function () {
      GenericSetting.inMaintenanceMode().then(res => {
        changeMaintenanceSwitch(res.data);
      });

      GenericSetting.getAnnouncementBanner().then(res => {
        const message = res.data.banner;
        if(message !== ''){
          $ctrl.message = message;
        }
      });

      $scope.$on(events.MAINTENANCE_MODE_ON, () => {
        changeMaintenanceSwitch(true);
      });

      $scope.$on(events.MAINTENANCE_MODE_OFF, () => {
        changeMaintenanceSwitch(false);
      });

      $scope.$on(events.NEW_ANNOUNCEMENT, (event, msg) => {
        if(msg.banner !== ''){
          $ctrl.message = msg.banner;
        }else{
          $ctrl.message = null;
        }
        $scope.$apply();
      });

    };
  }
})();
