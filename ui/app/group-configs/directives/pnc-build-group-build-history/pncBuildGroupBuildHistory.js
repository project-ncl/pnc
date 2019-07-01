/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2014-2019 Red Hat, Inc., and individual contributors
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

  angular.module('pnc.build-groups').component('pncBuildGroupBuildHistory', {
    bindings: {
      buildGroup: '<',
      buildGroupRecords: '<'
    },
    templateUrl: 'build-groups/directives/pnc-build-group-build-history/pnc-build-group-build-history.html',
    controller: ['$scope', 'eventTypes', 'paginator', Controller]
  });


  function Controller($scope, eventTypes, paginator) {
    var $ctrl = this;

    // -- Controller API --


    // --------------------

    
    $ctrl.$onInit = function () {
      $ctrl.page = paginator($ctrl.buildGroupRecords);
      $scope.$on(eventTypes.BUILD_SET_STARTED, handleEvent);
      $scope.$on(eventTypes.BUILD_SET_FINISHED, handleEvent);
    };

    function handleEvent(event, payload) {
      if (payload.buildSetConfigurationId === $ctrl.buildGroup.id) {
        $scope.$applyAsync(function () {
          $ctrl.page.refresh();
        });
      }
    }

  }

})();
