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
    controller: [Controller]
  });


  function Controller() {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.working = false;

    $ctrl.submit = submit;
    $ctrl.cancel = cancel;
    $ctrl.numberOfBuildParameters = numberOfBuildParameters;

    // --------------------


    $ctrl.$onInit = function () {
      // Ensure this components copy of the BC can't be updated from outside.
      $ctrl.buildConfig = angular.copy($ctrl.buildConfig);
      $ctrl.formData = fromBuildConfig($ctrl.buildConfig);
    };

    function submit(formData) {
      console.log('SUBMIT!!! formData = %O', formData);
      $ctrl.working = true;
      var buildConfig = toBuildConfig($ctrl.formData, $ctrl.buildConfig);

      console.log('buildConfig = %O', $ctrl.buildConfig);
      buildConfig.$update().then(function (response) {
        console.log('updated bc === %O', response);
        $ctrl.onSuccess({ buildConfig: response });
      })
        .finally(function () { $ctrl.working = false; });
    }

    function cancel() {
      console.log('CANCEL!');
      $ctrl.onCancel();
    }

    function numberOfBuildParameters() {
      return Object.keys($ctrl.formData.buildParameters).length;
    }

    function fromBuildConfig(buildConfig) {
      var formData = {
        general: {},
        buildParameters: {},
        repositoryConfiguration: {}
      };

      formData.general.name = buildConfig.name;
      formData.general.description = buildConfig.description;
      formData.general.environment = buildConfig.environment;
      formData.general.buildType = buildConfig.buildType;
      formData.general.buildScript = buildConfig.buildScript;
      formData.general.scmRevision = buildConfig.scmRevision;

      formData.repositoryConfiguration = buildConfig.repositoryConfiguration;
      formData.buildParameters = buildConfig.genericParameters;

      return formData;
    }

    function toBuildConfig(formData, buildConfig) {
      var newBc = angular.extend(angular.copy(buildConfig), formData.general);

      newBc.buildType =  formData.general.buildType.id;

      newBc.repositoryConfiguration = formData.repositoryConfiguration;
      newBc.genericParameters = formData.buildParameters;

      return newBc;
    }

  }

})();

//
