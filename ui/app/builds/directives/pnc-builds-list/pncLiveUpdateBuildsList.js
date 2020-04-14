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

  angular.module('pnc.builds').component('pncLiveUpdateBuildsList', {
    bindings: {
      /**
       * array of Builds: The list of Builds to display.
       */
      builds: '<?',
      /**
       * array of strings: Names of table columns to display (see template for possible options).
       * Default fields will be used if omitted.
       */
      displayFields: '<?',
      /**
       * object representing whether table head should be displayed or not.
       */
      hideHead: '<?'
    },
    templateUrl: 'builds/directives/pnc-builds-list/pnc-live-update-builds-list.html',
    controller: ['$scope', 'events', Controller]
  });

  function Controller($scope, events) {
    var $ctrl = this;

    // -- Controller API --



    // --------------------

    $ctrl.$onInit = function () {
      $scope.$on(events.BUILD_STATUS_CHANGED, onUpdate);
    };

    function onUpdate(event, payload) {
      var build;

      if ($ctrl.builds) {
        build = $ctrl.builds.find(function (item) {
          return item.id === payload.id;
        });

        if (build) {
          $scope.$applyAsync(function () {
            Object.assign(build, payload);
          });
        }
      }
    }
  }

})();
