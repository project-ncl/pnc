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

  angular.module('pnc.builds').component('pncBuildsList', {
    bindings: {
      /**
       * array of Builds: The list of Builds to display.
       */
      builds: '<?',
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
    templateUrl: 'builds/directives/pnc-builds-list/pnc-builds-list.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;
    var DEFAULT_FIELDS = ['status', 'id', 'configurationName', 'submitTime', 'startTime', 'endTime', 'username'];

    // -- Controller API --

    $ctrl.showTable = showTable;
    $ctrl.showColumn = showColumn;

    // --------------------

    $ctrl.$onInit = function() {
      $ctrl.items = $ctrl.builds ? $ctrl.builds : $ctrl.groupBuilds;
      $ctrl.fields = $ctrl.displayFields || DEFAULT_FIELDS;
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.builds) {
        $ctrl.items = $ctrl.builds;
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
