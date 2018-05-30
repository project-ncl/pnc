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
      buildConfig: '<',
      onSubmit: '&',
      onCancel: '&'
    },
    templateUrl: 'build-configs/detail/details-tab/pnc-build-config-details-editor.html',
    controller: [Controller]
  });


  function Controller() {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.submit = submit;
    $ctrl.cancel = cancel;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.formData = fromBuildConfig($ctrl.buildConfig);
    };

    function submit(data) {
      $ctrl.onSubmit(data);
    }

    function cancel() {
      $ctrl.onCancel();
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

      formData.repositoryConfiguration = buildConfig.repositoryConfiguration;
      formData.buildParameters = buildConfig.genericParameters;

      return formData;
    }

    function toBuildConfig(formData) {

    }

  }

})();
