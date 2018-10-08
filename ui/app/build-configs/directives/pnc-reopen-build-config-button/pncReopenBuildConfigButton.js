/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2018 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.build-configs').component('pncReopenBuildConfigButton', {
    bindings: {
      initialValues: '<'
    },
    templateUrl: 'build-configs/directives/pnc-reopen-build-config-button/pnc-reopen-build-config-button.html',
    controller: ['$log', '$uibModal', Controller]
  });

  function Controller($log, $uibModal) {
    var $ctrl = this;

    // --- api ---

    $ctrl.reopenWizardModal = reopenWizardModal;

    // -----------

    function reopenWizardModal() {
      $uibModal.open({
        animation: true,
        backdrop: 'static',
        component: 'pncCreateBuildConfigWizard',
        size: 'lg',
        resolve: {
          project: function () {
            return $ctrl.initialValues.project;
          },
          initialValues: function() {
            return $ctrl.initialValues;
          }
        }
      });
    }
    
  }
})();
