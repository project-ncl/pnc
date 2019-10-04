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

  angular.module('pnc.build-records').component('pncBuildRecordsList', {
    bindings: {
      /**
       * array of Build Records: The list of Build Records to display.
       */
      buildRecords: '<?',
      /**
       * array of Group Builds: The list of Group Builds to display.
       */
      groupBuilds: '<?',
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
    templateUrl: 'build-records/directives/pnc-build-records-list/pnc-build-records-list.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;
    var DEFAULT_FIELDS = ['status', 'id', 'configurationName', 'startTime', 'endTime', 'username'];

    // -- Controller API --

    $ctrl.showTable = showTable;
    $ctrl.showColumn = showColumn;

    // --------------------

    $ctrl.$onInit = function() {
      $ctrl.items = $ctrl.buildRecords ? $ctrl.buildRecords : $ctrl.groupBuilds;  
      $ctrl.fields = $ctrl.displayFields || DEFAULT_FIELDS;
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.buildRecords) {
        $ctrl.items = $ctrl.buildRecords;
      } else if (changedBindings.groupBuilds) {
        $ctrl.items = $ctrl.groupBuilds;
      }
    };

    function showTable() {
      return $ctrl.items && $ctrl.items.length;
    }

    function showColumn(property) {
      return $ctrl.fields.includes(property);
    }
  }

})();
