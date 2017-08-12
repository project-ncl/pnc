/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.build-configs').component('pncCreateBuildConfigButton', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-button/pnc-create-build-config-button.html',
    controller: Controller
  });

  function Controller($log, $uibModal) {
    var $ctrl = this;

    // --- api ---

    $ctrl.openWizardModal = openWizardModal;

    // -----------

    function openWizardModal() {
      var
          modalInstance = $uibModal.open({
            animation: true,
            backdrop: 'static',
            component: 'pncCreateBuildConfigWizard',
            size: 'lg'
          });

      // var closeWizard = function (e, reason) {
      //   modalInstance.dismiss(reason);
      //   wizardDoneListener();
      // };

      modalInstance.result.then(function () { }, function () { });

      // wizardDoneListener = $scope.$on('create-build-config.done', closeWizard);
    }
  }
})();
