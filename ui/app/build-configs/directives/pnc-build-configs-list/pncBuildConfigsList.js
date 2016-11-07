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

  angular.module('pnc.build-configs').component('pncBuildConfigsList', {
    bindings: {
      /**
       * array of Build Configs: The list of Build Configs to display.
       */
      buildConfigs: '<',
      /**
       * array of strings: Names of table columns to display (see template for possible options)
       */
      displayFields: '<',
      /**
       * string URL: template to display in actions column, column will not be shown if omitted.
       * The specific BuildConfig will be available to the template on the scope as `buildConfig`
       */
      actionsTemplateUrl: '@',
      /**
       * object: properties or callbacks to be accessed from the actions template.
       * all properties on the object will be made available to the template on the scope
       * under the `actions` object.
       */
      actionsData: '<'
    },
    templateUrl: 'build-configs/directives/pnc-build-configs-list/pnc-build-configs-list.html',
    controller: ['$scope', Controller]
  });


  function Controller($scope) {
    var $ctrl = this;

    // -- Controller API --

    $ctrl.showTable = showTable;
    $ctrl.showColumn = showColumn;

    // --------------------


    $ctrl.$onInit = function () {
      $scope.actions = $ctrl.actionsData;
    };

    function showTable() {
      // return $ctrl.page && $ctrl.page.data && $ctrl.page.data.length > 0;
      return $ctrl.buildConfigs && $ctrl.buildConfigs.length > 0;
    }

    function showColumn(property) {
      return $ctrl.displayFields.includes(property);
    }
  }

})();
