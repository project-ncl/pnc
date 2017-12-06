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

  /**
   * The component representing build start buttons for given Build Configuration or Build Group Configuration
   */
  angular.module('pnc.common.components').component('pncBuildStart', {
    bindings: {
      /**
       * Object: The configuration representing Build Configuration
       */
      buildConfig: '<?',
      /**
       * Object: The configuration representing Build Group Configuration
       */
      buildGroup: '<?',
      /**
       * String: Value representing bootstrap button size: lg (default if empty), md, sm, xs
       */
      size: '@?'
    },
    templateUrl: 'common/components/pnc-build-start/pnc-build-start.html',
    controller: ['$log', 'BuildConfigurationDAO', 'BuildConfigurationSetDAO', Controller]
  });

  function Controller($log, BuildConfigurationDAO, BuildConfigurationSetDAO) {
    var $ctrl = this;

    $ctrl.dropdownMenu = false;

    // default build values
    $ctrl.params = {
      temporaryBuild: false,
      forceRebuild: false,
      timestampAlignment: false
    };

    if ($ctrl.buildConfig) {
      $ctrl.params.keepPodOnFailure = false;
      $ctrl.params.buildDependencies = true;
    }

    $ctrl.refreshBuildModes = function() {
      if (!$ctrl.params.temporaryBuild) {
        $ctrl.params.timestampAlignment = false;
      }
    };

    $ctrl.build = function() {
      $ctrl.dropdownMenu = false;

      if ($ctrl.buildConfig) {
        $log.debug('pncBuildStart: Initiating build of: %O', $ctrl.buildConfig);
        $ctrl.params.configurationId = $ctrl.buildConfig.id;
        BuildConfigurationDAO.build($ctrl.params, {});

      } else if ($ctrl.buildGroup) {
        $log.debug('pncBuildStart: Initiating build of: %O', $ctrl.buildGroup);
        $ctrl.params.configurationSetId = $ctrl.buildGroup.id;
        BuildConfigurationSetDAO.build($ctrl.params, {});
      }

    };

  }
})();