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

  angular.module('pnc.build-configs').component('pncBuildConfigDetailsTab', {
    bindings: {
      buildConfig: '<'
    },
    require : {
      mainCtrl: '^^pncBuildConfigDetailMain'
    },
    templateUrl: 'build-configs/detail/details-tab/pnc-build-config-details-tab.html',
    controller: ['notifyInline', Controller]
  });


  function Controller(notifyInline) {
    var $ctrl = this,
        editMode = false,
        notify;

    // -- Controller API --

    $ctrl.isEditModeActive = isEditModeActive;
    $ctrl.onCancelEdit = onCancelEdit;
    $ctrl.onSuccess = onSuccess;
    $ctrl.toggleEdit = toggleEdit;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.mainCtrl.registerOnEdit(toggleEdit);
      notify = notifyInline('edit-build-config');
    };

    function toggleEdit() {
      editMode = !editMode;
    }

    function isEditModeActive() {
      return editMode;
    }

    function onCancelEdit() {
      toggleEdit();
    }

    function onSuccess(buildConfig) {
      $ctrl.buildConfig = buildConfig;
      $ctrl.mainCtrl.updateBuildConfig(buildConfig);
      toggleEdit();
      notify({
        type: 'success',
        message: 'Update Successful',
        persistent: true
      });
    }
  }

})();
