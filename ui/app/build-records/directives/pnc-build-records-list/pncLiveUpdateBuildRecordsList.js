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

  angular.module('pnc.build-records').component('pncLiveUpdateBuildRecordsList', {
    bindings: {
      /**
       * array of Build Records: The list of Build Records to display.
       */
      buildRecords: '<?',
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
    templateUrl: 'build-records/directives/pnc-build-records-list/pnc-live-update-build-records-list.html',
    controller: ['$scope', 'eventTypes', Controller]
  });

  function Controller($scope, eventTypes) {
    var $ctrl = this;

    // -- Controller API --



    // --------------------

    $ctrl.$onInit = function () {
      $scope.$on(eventTypes.BUILD_STATUS_CHANGED, onUpdate);
    };

    function onUpdate(event, payload) {
      var buildRecord;

      if ($ctrl.buildRecords) {
        buildRecord = $ctrl.buildRecords.find(function (item) {
          return item.id === payload.id;
        });

        if (buildRecord) {
          $scope.$applyAsync(function () {
            Object.assign(buildRecord, payload);
          });
        }
      }
    }
  }

})();
