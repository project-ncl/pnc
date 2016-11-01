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

  angular.module('pnc.build-groups').component('pncBuildGroupBuildConfigs', {
    bindings: {
      page: '<',
      buildGroup: '<'
    },
    templateUrl: 'build-groups/directives/pnc-build-group-build-configs/pnc-build-group-build-configs.html',
    controller: ['modalSelectService', 'BuildConfigurationSet', Controller]
  });


  function Controller(modalSelectService, BuildConfigurationSet) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.edit = edit;

    // --------------------


    function edit() {
      console.log('EDIT BUILD CONFIGS!');
      var modal = modalSelectService.openForBuildConfigs({
        title: 'Add / Remove Build Configs from ' + $ctrl.buildGroup.name,
        buildConfigs: $ctrl.page.data
      });
      modal.result.then(function (result) {
        console.debug('Selected Build Configs: %O', result);
        BuildConfigurationSet.updateBuildConfigurations({ id: $ctrl.buildGroup.id }, result).$promise.then(function () {
          $ctrl.page.reload();
        });
      });
    }

  }

})();
