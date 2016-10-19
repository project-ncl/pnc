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

  var module = angular.module('pnc.build-records');
  /**
   * @ngdoc directive
   * @name pnc.common.eventbus:pncBuildDetail
   * @restrict EA
   * @description
   * @example
   * @author Alex Creasy
  */
  module.directive('pncBuildDetails', [
    function() {

    var DEFAULT_TEMPLATE = 'build-records/directives/pnc-build-details/pnc-build-details.html';

    return {
      restrict: 'EA',
      templateUrl: function(elem, attrs) {
        return attrs.pncTemplate || DEFAULT_TEMPLATE;
      },
      scope: {
        'pncBuildRecord': '='
      },
      controllerAs: 'ctrl',
      controller: ['$scope',
        function ($scope) {
          var self = this;

          self.record = $scope.pncBuildRecord;

          self.refresh = function () {
            self.record.$get();
          };
        }
      ]
    };
  }]);

})();
