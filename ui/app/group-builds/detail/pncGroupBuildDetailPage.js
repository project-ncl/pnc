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

  angular.module('pnc.group-builds').component('pncGroupBuildDetailPage', {
    bindings: {
     groupBuild: '<',
     dependencyGraph: '<',
     builds: '<'
    },
    templateUrl: 'group-builds/detail/pnc-group-build-detail-page.html',
    controller: ['$scope', 'events', Controller]
  });


  function Controller($scope, events) {
    var $ctrl = this;

    // -- Controller API --

     $ctrl.hasBuilds = hasBuilds;

    // --------------------


    $ctrl.$onInit = function () {
      $scope.$on(events.GROUP_BUILD_STATUS_CHANGED, function (event, payload) {
        if (payload.id === $ctrl.groupBuild.id) {
          $scope.$applyAsync(function () {
            Object.assign($ctrl.groupBuild, payload);
          });
        }
      });
    };

    function hasBuilds() {
      return $ctrl.builds && $ctrl.builds.length > 0;
    }

  }

})();
