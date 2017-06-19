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

  var module = angular.module('pnc.common.directives');

  /**
   * @ngdoc directive
   * @restrict A
   * @example <tbody pnc-loading-placeholder columns="3" page="page"></tbody>
   * @author Martin Kelnar
   */
  module.directive('pncLoadingPlaceholder', function () {
    return {
      restrict: 'A',
      scope: {
        columns: '@',
        page: '='
      },
      templateUrl: 'common/directives/pnc-loading-placeholder/pnc-loading-placeholder.html',
      controller: [
        '$scope',
        function($scope) {
          $scope.columnsIterable = _.range($scope.columns);
        }
      ]
    };
  });

})();
