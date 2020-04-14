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

  angular.module('pnc.projects').component('pncProjectsList', {
    bindings: {
      /**
       * array of Projects: The list of Projects to display.
       */
      projects: '<',
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
    templateUrl: 'projects/components/pnc-projects-list/pnc-projects-list.html',
    controller: [Controller]
  });

  function Controller() {
    const $ctrl = this;
    const DEFAULT_FIELDS = ['name', 'description'];

    // -- Controller API --

    $ctrl.showTable = showTable;
    $ctrl.showColumn = showColumn;

    // --------------------

    $ctrl.$onInit = () => {
      $ctrl.items = $ctrl.projects;  
      $ctrl.fields = $ctrl.displayFields || DEFAULT_FIELDS;
    };

    $ctrl.$onChanges = (changedBindings) => {
      if (changedBindings.projects) {
        $ctrl.items = $ctrl.projects;
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
