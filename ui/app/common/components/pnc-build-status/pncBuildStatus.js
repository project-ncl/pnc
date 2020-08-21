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
   * The dumb component representing Build Status displaying information like build status icon,
   * start / end time or started by user for given Build or Group Build.
   *
   * @example
   * <pnc-build-status build="build" is-loaded="isLoaded"></pnc-build-status>
   */
  angular.module('pnc.common.components').component('pncBuildStatus', {
    bindings: {
      /**
       * Object: The Build to display the status for.
       */
      build: '<?',
      /**
       * Object: The Group Build to display the status for.
       */
      groupBuild: '<?',
      /**
       * Object: Truthy or falsy object indicating whether data request is finished or not.
       */
      isLoaded: '<',
      /**
       * Object: Truthy of falsy object indicating whether click should be propagated or not.
       * Sometimes propagated clicks can cause side effects that should be stopped.
       */
      stopPropagation: '<'
    },
    templateUrl: 'common/components/pnc-build-status/pnc-build-status.html',
    controller: ['$scope', Controller]
  });

  function Controller($scope) {
    var $ctrl = this;

    $ctrl.$onInit = function() {
      $ctrl.item = $ctrl.build ? $ctrl.build : $ctrl.groupBuild;
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.build) {
        updateState($ctrl.build);
      } else if (changedBindings.groupBuild) {
        updateState($ctrl.groupBuild);
      }
    };

    function updateState(buildable) {
      $scope.$applyAsync(() => $ctrl.item = buildable);
    }

  }

})();
