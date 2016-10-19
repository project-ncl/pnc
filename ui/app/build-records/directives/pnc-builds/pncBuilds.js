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
   * @name pnc.configuration:pncBuilds
   * @restrict E
   * @description
   * Displays a paginated panel showing running and completed builds.
   * @example
      <pnc-builds></pnc-builds>
   * @author Alex Creasy
   */
  module.directive('pncBuilds', [
    function () {

      function PncBuildsCtrl($log, $scope, BuildsDAO, eventTypes) {
        $scope.page = BuildsDAO.getPaged();

        $scope.$on(eventTypes.BUILD_STARTED, $scope.page.reload);
        $scope.$on(eventTypes.BUILD_FINISHED, $scope.page.reload);
      }

      return {
        restrict: 'E',
        templateUrl: 'build-records/directives/pnc-builds/pnc-builds.html',
        scope: {},
        controller: PncBuildsCtrl
      };
    }
  ]);

})();
