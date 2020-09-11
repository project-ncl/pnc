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
   * The component representing build start buttons for given Build Config or Group Config
   */
  angular.module('pnc.common.components').component('pncBuildStart', {
    bindings: {
      /**
       * Object: The configuration representing Build Config
       */
      buildConfig: '<?',
      /**
       * Object: The configuration representing Group Config
       */
      groupConfig: '<?',
      /**
       * String: Value representing bootstrap button size: lg (default if empty), md, sm, xs
       */
      size: '@?'
    },
    templateUrl: 'common/components/pnc-build-start/pnc-build-start.html',
    controller: ['$log', 'BuildConfigResource', 'GroupConfigResource', Controller]
  });

  function Controller($log, BuildConfigResource, GroupConfigResource) {
    const $ctrl = this;

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
      if (!$ctrl.size) {
        $ctrl.size = 'lg';
      }

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
        $log.debug('pncBuildStart: Initiating build of: %O', $ctrl.buildConfig);
        $ctrl.params.id = $ctrl.buildConfig.id;
        BuildConfigResource.build($ctrl.params, {});

      } else if ($ctrl.groupConfig) {
        $log.debug('pncBuildStart: Initiating build of: %O', $ctrl.groupConfig);
        $ctrl.params.id = $ctrl.groupConfig.id;
        GroupConfigResource.build($ctrl.params, {});
      }

    };

  }
})();
