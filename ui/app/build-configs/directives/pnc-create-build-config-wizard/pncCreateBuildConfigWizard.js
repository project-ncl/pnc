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

  angular.module('pnc.build-configs').component('pncCreateBuildConfigWizard', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-wizard/pnc-create-build-config-wizard.html',
    controller: ['$log', '$uibModal', '$scope', '$timeout', 'eventTypes', 'ScmRepositoryResource', 'BuildConfiguration', Controller],
    bindings: {
      modalInstance: '<',
      project: '<',
      resolve: '<',
      onClose: '&close'
    }
  });

  function Controller($log, $uibModal, $scope, $timeout, eventTypes, ScmRepositoryResource, BuildConfiguration) {
    var $ctrl = this,
        emptyWizardData = {
          general: {},
          buildParameters: {},
          dependencies: [],
          repoConfig: {}
        };

    // -- Controller API --

    $ctrl.generalForm = {};
    $ctrl.repoForm = {};

    $ctrl.nextButtonTitle = 'Next >';
    $ctrl.reviewPageShown = false;

    $ctrl.createStatusMessages = [];
    $ctrl.wizardDone = false;
    $ctrl.createError = false;

    $ctrl.onStepChange = onStepChange;
    $ctrl.onShowReviewSummary = onShowReviewSummary;
    $ctrl.create = create;

    $ctrl.closePreviousWizardModal = closePreviousWizardModal;

    // --------------------

    $ctrl.$onInit = function () {
      $ctrl.wizardData = angular.extend({}, emptyWizardData, $ctrl.resolve.initialValues);
      $ctrl.wizardData.project = $ctrl.resolve.project;
    };

    function onStepChange(step) {
      switch (step.stepId) {
        case 'review-summary':
          $ctrl.nextButtonTitle = 'Create';
          break;
        case 'review-create':
          $ctrl.nextButtonTitle = 'Close';
          break;
        default:
          $ctrl.nextButtonTitle = 'Next >';
          break;
      }
    }

    function closePreviousWizardModal() {
      $ctrl.modalInstance.close();
    }

    function onShowReviewSummary() {
      $ctrl.reviewPageShown = true;
      $timeout(function () {
        $ctrl.reviewPageShown = false;  // done so the next time the page is shown it updates
      });
    }

    function parseBuildConfig(wizardData) {
      var bc = angular.copy(wizardData.general);
      bc.parameters = angular.copy(wizardData.buildParameters);
      /*
       * New REST requires dependencies attribute instead of dependencyIds
       *
       * When new Build Configuration and SCM Repository is being created
       *  - new rest endpoints are already used and this option is fully working
       * 
       * When new Build Configuration is created with already existing SCM repository
       *  - this option won't temporarily work until Build Configuration rest refactor is implemented
       */
      bc.dependencies = wizardData.dependencies.map(function (d) { 
        return { 
          id: d.id 
        }; 
      });
      bc.scmRevision = wizardData.repoConfig.revision;
      bc.project = {
        id: $ctrl.wizardData.project.id
      };
      bc.groupConfigs = [];

      if ($ctrl.wizardData.productVersion) {
        bc.productVersion = {
          id: $ctrl.wizardData.productVersion.id
        };
      }

      return bc;
    }

    function create() {
      var bc = new BuildConfiguration(parseBuildConfig($ctrl.wizardData));

      if ($ctrl.wizardData.repoConfig.useExistingRepoConfig) {
        bc.scmRepository = { id: $ctrl.wizardData.repoConfig.repoConfig.id };
        bc.$save()
          .then(function (result) {
            $ctrl.createdBuildConfigId = result.id;
          })
          .catch(function (error) {
            $ctrl.createStatusMessages.push(error.status + ': ' + error.statusText);
            $ctrl.createError = true;
          })
          .finally(function () {
            $ctrl.wizardDone = true;
          });
      } else {

        $scope.$on(eventTypes.RC_BPM_NOTIFICATION, function (event, payload) {
          switch (payload.eventType) {
            case 'RC_REPO_CREATION_SUCCESS':
              $ctrl.createStatusMessages.push(payload.data.message);
              break;
            case 'RC_REPO_CREATION_ERROR':
              $ctrl.createStatusMessages.push('Error creating repository.');
              $ctrl.createStatusMessages.push(payload.data.message);
              $ctrl.wizardDone = true;
              $ctrl.createError = true;
              break;

            case 'RC_REPO_CLONE_SUCCESS':
              $ctrl.createStatusMessages.push('SCM Repository successfully cloned.');
              break;
            case 'RC_REPO_CLONE_ERROR':
              $ctrl.createStatusMessages.push('Error cloning repository.');
              $ctrl.createStatusMessages.push(payload.data.message);
              $ctrl.wizardDone = true;
              $ctrl.createError = true;
              break;

            case 'RC_CREATION_SUCCESS':
              $ctrl.createStatusMessages.push('SCM Repository successfully created.');
              $ctrl.createdRepoConfigId = payload.repositoryConfigurationId;
              break;
            case 'RC_CREATION_ERROR':
              $ctrl.wizardDone = true;
              $ctrl.createError = true;
              break;
          }

        });

        $scope.$on(eventTypes.BC_NOTIFICATION, function (event, payload) {
          switch (payload.eventType) {
            case 'BC_CREATION_SUCCESS':
              $ctrl.createStatusMessages.push('Build Config successfully created.');
              $ctrl.createdBuildConfigId = payload.buildConfigurationId;
              $ctrl.wizardDone = true;
              break;
            case 'BC_CREATION_ERROR':
              $ctrl.createStatusMessages.push('Error creating Build Config.');
              $ctrl.createStatusMessages.push(payload.data.message);
              $ctrl.wizardDone = true;
              $ctrl.createError = true;
              break;
          }

        });

        BuildConfiguration.createWithScm({
          url: $ctrl.wizardData.repoConfig.scmUrl,
          preBuildSync: $ctrl.wizardData.repoConfig.preBuildSyncEnabled,
          buildConfiguration: bc
        }).catch(function () {
          $ctrl.wizardDone = true;
          $ctrl.createError = true;
        });

      }
    }
  }
})();
