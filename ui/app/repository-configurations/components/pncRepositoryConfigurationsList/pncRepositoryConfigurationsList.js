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

  angular.module('pnc.repository-configurations').component('pncRepositoryConfigurationsList', {
    bindings: {
      /**
       * array of Repository Configurations: The list of Repository Configurations to display.
       */
      repositoryConfigurations: '<',
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
    templateUrl: 'repository-configurations/components/pncRepositoryConfigurationsList/pnc-repository-configurations-list.html',
    controller: [Controller]
  });

  function Controller() {
    var $ctrl = this;
    var DEFAULT_FIELDS = ['id', 'internalScmUrl', 'externalScmUrl', 'preBuildSync'];

    // -- Controller API --

    $ctrl.showTable = showTable;
    $ctrl.showColumn = showColumn;

    // --------------------

    $ctrl.$onInit = function() {
      $ctrl.items = $ctrl.repositoryConfigurations;  
      $ctrl.fields = $ctrl.displayFields || DEFAULT_FIELDS;
    };

    $ctrl.$onChanges = function(changedBindings) {
      if (changedBindings.repositoryConfigurations) {
        $ctrl.items = $ctrl.repositoryConfigurations;
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
