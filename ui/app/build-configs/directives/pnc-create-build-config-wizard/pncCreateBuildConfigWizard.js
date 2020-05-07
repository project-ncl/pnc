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

  angular.module('pnc.build-configs').component('pncCreateBuildConfigWizard', {
    templateUrl: 'build-configs/directives/pnc-create-build-config-wizard/pnc-create-build-config-wizard.html',
    controller: ['$timeout','BuildConfigResource', 'buildConfigCreator', 'utils', Controller],
    bindings: {
      modalInstance: '<',
      project: '<',
      resolve: '<',
      onClose: '&close'
    }
  });

  function Controller($timeout, BuildConfigResource, buildConfigCreator, utils) {
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
    $ctrl.isEditForm = false;

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

    /*
     * Translates an Array of PNC entities into an object,
     * This is the format the PNC REST API takes relations in.
     */
    function translateEntityArrayToMap(array) {
      return array.reduce((map, entity) => {
        map[entity.id] = { id: entity.id };
        return map;
      }, {});
    }

    function create() {
      const buildConfig = parseBuildConfig($ctrl.wizardData);

      if ($ctrl.wizardData.repoConfig.useExistingRepoConfig) {
        createBuildConfigOnly(buildConfig);
      } else {
        createBuildConfigAndScmr(buildConfig);
      }
    }

   /*
    * If the user has specified an SCM URL that does not have an associated SCMRepository Entity
    * in PNC, call the async endpoint to create an SCMR and BC and link the two.
    */
    function createBuildConfigAndScmr(buildConfig) {

      buildConfigCreator
          .createWithScm({
            buildConfig,
            scmUrl: $ctrl.wizardData.repoConfig.scmUrl,
            preBuildSyncEnabled: $ctrl.wizardData.repoConfig.preBuildSyncEnabled
          })
          .then(
            result => {
              console.debug('Received: Create BC success => %O', result);
              onSuccess(result.buildConfig);
            },
            error => {
              console.debug('Received: Create BC error => %O', error);
              onError(error.message);
            },
            update => {
              console.debug('Received: Create BC progress update => %O', update);
            });

    }

    /*
     * If the SCM URL the user provided has an existing SCMR Entity in PNC, we can simply create the BC.
     */
    function createBuildConfigOnly(buildConfig) {
      buildConfig.scmRepository = { id: $ctrl.wizardData.repoConfig.repoConfig.id };

      BuildConfigResource
          .save(buildConfig)
          .$promise
          .then(
            result => onSuccess(result),
            error => onError(error));
    }


    function onSuccess(buildConfig) {
      $ctrl.createdBuildConfigId = buildConfig.id;
      $ctrl.wizardDone = true;
    }

    function onError(message) {
      $ctrl.createStatusMessages.push(message);
      $ctrl.createError = true;
      $ctrl.wizardDone = true;
    }

    /**
     * Creates a Build Config DTO from the internal state of the wizard.
     */
    function parseBuildConfig(wizardData) {
      const bc = angular.copy(wizardData.general);

      bc.environment = { id: wizardData.general.environment.id.toString() };
      bc.scmRevision = wizardData.repoConfig.revision;
      bc.project = { id: wizardData.project.id.toString() };

      // Optional properties
      if (utils.isNotEmpty(wizardData.productVersion)) {
        bc.productVersion = { id: wizardData.productVersion.version.id.toString() };
      }

      if (utils.isNotEmpty(wizardData.buildParameters)) {
        bc.parameters = angular.copy(wizardData.buildParameters);
      }

      if (utils.isNotEmpty(wizardData.dependencies)) {
        bc.dependencies = translateEntityArrayToMap(wizardData.dependencies);
      }

      return bc;
    }

  }
})();
