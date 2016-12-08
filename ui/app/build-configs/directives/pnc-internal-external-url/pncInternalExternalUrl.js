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

  var module = angular.module('pnc.build-configs');

  module.directive('pncInternalExternalUrl', [
    function () {

      function PncInternalExternalUrlController($scope, pncProperties) {
        var COLUMS_CLASS = 'col-sm-'; // bootstrap prefix for column
        var COLUMS_MAX   = 12;        // bootstrap max columns

        $scope.sidebarClass = COLUMS_CLASS + $scope.sidebarCols;
        $scope.contentClass = COLUMS_CLASS + (COLUMS_MAX - $scope.sidebarCols);

        $scope.exactHost = pncProperties.internalScmAuthority;
      }

      return {
        restrict: 'E',
        templateUrl: 'build-configs/directives/pnc-internal-external-url/pnc-internal-external-url.html',
        scope: {
          data: '=',
          form: '=',
          sidebarCols: '@'
        },
        controller: PncInternalExternalUrlController
      };

    }
  ]);
})();
