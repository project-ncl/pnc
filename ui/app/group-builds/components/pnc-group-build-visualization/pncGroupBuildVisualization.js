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

  angular.module('pnc.group-builds').component('pncGroupBuildVisualization', {
    bindings: {
      groupBuild: '<',
      dependencyGraph: '<',
      builds: '<',
      visualization: '<?'
    },
    templateUrl: 'group-builds/components/pnc-group-build-visualization/pnc-group-build-visualization.html',
    controller: ['$scope', '$state', '$stateParams', Controller]
  });


  function Controller($scope, $state, $stateParams) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.buildColumns = ['status', 'id', 'configurationName', 'startTime', 'endTime', 'username', 'pushStatus'];
    $ctrl.changeVisualization = changeVisualization;

    // --------------------


    $ctrl.$onInit = function () {
      $ctrl.visualization = $stateParams.visualization;
    };

    function changeVisualization(visualization) {
      $ctrl.visualization = visualization;
      $state.go('.', { visualization: visualization });
    }
  }

})();
