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
    controller: ['$log', Controller]
  });

  function Controller($log) {
    var $ctrl = this;

    var REBUILD_MODE_INDEX_DEFAULT = 1;

    $ctrl.dropdownMenu = false;

    /*
     * When used together with forceRebuild parameter (deprecated), forceRebuild will be ignored
     */
    $ctrl.rebuildModes = [{
      title: 'Explicit',
      value: 'EXPLICIT_DEPENDENCY_CHECK'
    }, {
      title: 'Implicit',
      value: 'IMPLICIT_DEPENDENCY_CHECK'
    }, {
      title: 'Force',
      value: 'FORCE'
    }];

    $ctrl.$onInit = function () {
      // default build values
      $ctrl.params = {
        temporaryBuild: false,
        rebuildMode: $ctrl.rebuildModes[REBUILD_MODE_INDEX_DEFAULT].value,
        timestampAlignment: false
      };

      $ctrl.refreshRebuildModes(REBUILD_MODE_INDEX_DEFAULT);

      if ($ctrl.buildConfig) {
        $ctrl.params.keepPodOnFailure = false;
        $ctrl.params.buildDependencies = true;
      }
    };

    $ctrl.refreshBuildTypes = function() {
      if (!$ctrl.params.temporaryBuild) {
        $ctrl.params.timestampAlignment = false;
      }
    };

    $ctrl.refreshRebuildModes = function(rebuildModeIndex) {
      $ctrl.currentRebuildModeTitle = $ctrl.rebuildModes[rebuildModeIndex].title;
    };

    $ctrl.build = function() {
      $ctrl.dropdownMenu = false;

      if ($ctrl.buildConfig) {
        $log.debug('pncBuildStart: Initiating build of BuildConfig: %O with params: %O', $ctrl.buildConfig, $ctrl.params);
        $ctrl.buildConfig.$build($ctrl.params);
      } else if ($ctrl.buildGroup) {
        $log.debug('pncBuildStart: Initiating build of GroupConfig: %O with params: %O', $ctrl.buildGroup, $ctrl.params);
        $ctrl.buildGroup.$build($ctrl.params);
      }

    };

  }
})();
