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

  angular.module('pnc.build-configs').component('pncBuildConfigDetailsEditor', {
    bindings: {
      /**
       * Object: The buildConfig object to be edited
       */
      buildConfig: '<',
      /**
       * Function: Callback function when the buildConfig is successfully
       * updated. The function is passed the updated buildConfig.
       */
      onSuccess: '&',
      /**
       * Function: Callback function executed when the user clicks the
       * cancel button.
       */
      onCancel: '&'
    },
    templateUrl: 'build-configs/detail/details-tab/pnc-build-config-details-editor.html',
    controller: ['BuildConfigResource', Controller]
  });


  function Controller(BuildConfigResource) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.working = false;
    $ctrl.isEditForm = true;

    $ctrl.submit = submit;
    $ctrl.cancel = cancel;
    $ctrl.numberOfBuildParameters = numberOfBuildParameters;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.formData = fromBuildConfig($ctrl.buildConfig);
      // Ensure this components copy of the BC can't be updated from outside.
      $ctrl.buildConfig = angular.copy($ctrl.buildConfig);
    };

    function submit() {
      $ctrl.working = true;
      var buildConfig = toBuildConfig($ctrl.formData, $ctrl.buildConfig);

      BuildConfigResource.safePatchRemovingParameters($ctrl.buildConfig, buildConfig).$promise
          .then(resp => $ctrl.onSuccess({ buildConfig: resp}))
          .finally(() => $ctrl.working = false);
      console.log('UPDATE BC -> formData: %O | buildConfig: %O', $ctrl.formData, buildConfig);
    }

    function cancel() {
      $ctrl.onCancel();
    }

    function numberOfBuildParameters() {
      return Object.keys($ctrl.formData.parameters).length;
    }

    function fromBuildConfig(buildConfig) {
      var formData = {
        general: {},
        parameters: {},
        scmRepository: {}
      };

      formData.general.name = buildConfig.name;
      formData.general.description = buildConfig.description;
      formData.general.environment = buildConfig.environment;
      formData.general.buildType = buildConfig.buildType;
      formData.general.buildScript = buildConfig.buildScript;
      formData.general.scmRevision = buildConfig.scmRevision;

      formData.scmRepository = buildConfig.scmRepository;

      // Make copy so form changes do not affect this component's BC
      formData.parameters = angular.copy(buildConfig.parameters);

      return formData;
    }

    function toBuildConfig(formData) {
      var newBc = angular.extend({}, formData.general);

      newBc.scmRepository = formData.scmRepository;
      newBc.parameters = formData.parameters;

      return newBc;
    }

  }

})();

//
