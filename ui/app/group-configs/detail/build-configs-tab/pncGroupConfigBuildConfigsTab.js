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

  angular.module('pnc.group-configs').component('pncGroupConfigBuildConfigsTab', {
    bindings: {
      groupConfig: '<',
      buildConfigs: '<'
    },
    templateUrl: 'group-configs/detail/build-configs-tab/pnc-group-config-build-configs-tab.html',
    controller: ['$log', 'paginator', 'GroupConfigResource', Controller]
  });

  function Controller($log, paginator, GroupConfigResource) {
    const $ctrl = this;

    // -- Controller API --

    $ctrl.onEdit = onEdit;
    $ctrl.onRemove = onRemove;
    $ctrl.refreshBuildConfigs = refreshBuildConfigs;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.paginator = paginator($ctrl.buildConfigs);
    };

    function onEdit(buildConfigs) {
      $log.info('Edit Build Configs: %O', buildConfigs);
      return GroupConfigResource.arrayPatch($ctrl.buildConfigs.data, buildConfigs,'buildConfigs', $ctrl.groupConfig.id).$promise;
    }

    function onRemove(buildConfig) {
      $log.info('remove BuildConfig: %O', buildConfig);
      return $ctrl.groupConfig.$removeBuildConfig({ buildConfigId: buildConfig.id });
    }

    function refreshBuildConfigs(buildConfigs) {
      $ctrl.buildConfigs.data = buildConfigs;
    }


  }

})();
